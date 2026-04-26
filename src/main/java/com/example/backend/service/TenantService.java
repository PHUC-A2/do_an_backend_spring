package com.example.backend.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Tenant;
import com.example.backend.domain.entity.TenantUser;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.tenant.ReqCreateTenantAdminDTO;
import com.example.backend.domain.request.tenant.ReqOwnerTenantRequestDTO;
import com.example.backend.domain.request.tenant.ReqUpdateTenantAdminDTO;
import com.example.backend.domain.response.tenant.ResTenantAdminDTO;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.TenantRepository;
import com.example.backend.repository.TenantUserRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.tenant.TenantStatusEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {

    public static final long DEFAULT_TENANT_ID = 1L;

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Tenant mặc định khi đăng nhập / refresh: ưu tiên cửa hàng đã duyệt (id &gt; 1), tránh gắn
     * chủ sân / nhân viên vào ngữ cảnh hệ thống (1) nếu họ còn thuộc shop thật.
     */
    @Transactional(readOnly = true)
    public Long resolveTenantIdForLogin(@NonNull com.example.backend.domain.entity.User user) {
        List<TenantUser> links = tenantUserRepository.findByUser_IdOrderByIdDesc(user.getId());
        for (TenantUser link : links) {
            Tenant t = link.getTenant();
            if (t.getId() != DEFAULT_TENANT_ID
                    && t.getStatus() == TenantStatusEnum.APPROVED
                    && link.isActive()) {
                return t.getId();
            }
        }
        for (TenantUser link : links) {
            Tenant t = link.getTenant();
            if (t.getId() != DEFAULT_TENANT_ID
                    && t.getStatus() == TenantStatusEnum.APPROVED) {
                return t.getId();
            }
        }
        for (TenantUser link : links) {
            Tenant t = link.getTenant();
            if (t.getStatus() == TenantStatusEnum.APPROVED
                    && link.isActive()) {
                return t.getId();
            }
        }
        for (TenantUser link : links) {
            Tenant t = link.getTenant();
            if (t.getStatus() == TenantStatusEnum.APPROVED) {
                return t.getId();
            }
        }
        return DEFAULT_TENANT_ID;
    }

    /**
     * Cửa hàng đã duyệt đầu tiên (id &gt; 1) của user, hoặc null nếu chỉ thuộc hệ thống / chưa có shop.
     */
    @Transactional(readOnly = true)
    public Optional<Long> preferredNonSystemShopTenantId(@NonNull Long userId) {
        return tenantUserRepository.findByUser_IdOrderByIdDesc(userId).stream()
                .filter(link -> {
                    Tenant t = link.getTenant();
                    return t.getId() != DEFAULT_TENANT_ID && t.getStatus() == TenantStatusEnum.APPROVED;
                })
                .map(l -> l.getTenant().getId())
                .distinct()
                .min(Long::compareTo);
    }

    @Transactional(readOnly = true)
    public List<Tenant> listTenantsForUser(@NonNull Long userId) {
        return tenantUserRepository.findByUser_IdOrderByIdDesc(userId).stream()
                .map(TenantUser::getTenant)
                .distinct()
                .toList();
    }

    /**
     * Yêu cầu đăng ký chủ sân (tenant PENDING, OWNER chưa kích hoạt).
     */
    @Transactional
    public Tenant requestOwnerTenant(@NonNull Long userId, @NonNull ReqOwnerTenantRequestDTO req) {
        boolean hasPending = tenantUserRepository.findByUser_IdOrderByIdDesc(userId).stream()
                .anyMatch(tu -> tu.getTenant().getStatus() == TenantStatusEnum.PENDING);
        if (hasPending) {
            throw new BadRequestException("Bạn đã có yêu cầu chủ sân đang chờ duyệt");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));
        String submitted = req.getContactEmail().trim();
        User emailUser = userRepository.findByEmailIgnoreCase(submitted);
        if (emailUser == null) {
            throw new BadRequestException("Email này chưa đăng ký trên hệ thống. Hãy tạo tài khoản bằng email sau đó gửi lại.");
        }
        if (emailUser.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BadRequestException(
                    "Tài khoản gắn với email này hiện không dùng được (chờ kích hoạt, đã khóa, hoặc đã tắt).");
        }
        String canonical = emailUser.getEmail() != null && !emailUser.getEmail().isBlank()
                ? emailUser.getEmail().trim()
                : submitted;
        return createPendingOwnerTenant(userId, req.getShopName().trim(), blankToNull(req.getContactPhone()), canonical,
                blankToNull(req.getDescription()));
    }

    /**
     * Đăng ký kèm shop (từ form register): tạo tenant PENDING.
     */
    @Transactional
    public Tenant createOwnerTenantForUser(@NonNull Long userId, @NonNull String shopName) {
        return createPendingOwnerTenant(userId, shopName.trim(), null, null, null);
    }

    private Tenant createPendingOwnerTenant(
            Long userId,
            String name,
            String contactPhone,
            String contactEmail,
            String description) {
        if (name.isEmpty()) {
            throw new BadRequestException("Tên cửa hàng không hợp lệ");
        }
        String slug = ensureUniqueSlug(slugify(name));
        Tenant t = new Tenant();
        t.setSlug(slug);
        t.setName(name);
        t.setStatus(TenantStatusEnum.PENDING);
        t.setContactPhone(contactPhone);
        t.setContactEmail(contactEmail);
        t.setDescription(description);
        t = tenantRepository.save(t);
        TenantUser link = new TenantUser();
        link.setTenant(t);
        link.setUser(userRepository.getReferenceById(userId));
        link.setRole("OWNER");
        link.setActive(false);
        tenantUserRepository.save(link);
        return t;
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    @Transactional(readOnly = true)
    public List<ResTenantAdminDTO> listAllTenantsForSystemAdmin() {
        List<Tenant> all = tenantRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return all.stream().map(this::toAdminDto).toList();
    }

    private ResTenantAdminDTO toAdminDto(Tenant t) {
        var ownerOpt = tenantUserRepository.findFirstByTenant_IdAndRoleOrderByIdAsc(t.getId(), "OWNER");
        Long ownerId = null;
        String ownerEmail = null;
        String ownerName = null;
        if (ownerOpt.isPresent()) {
            var u = ownerOpt.get().getUser();
            ownerId = u.getId();
            ownerEmail = u.getEmail();
            ownerName = u.getName();
        }
        return ResTenantAdminDTO.builder()
                .id(t.getId())
                .slug(t.getSlug())
                .name(t.getName())
                .status(t.getStatus().name())
                .contactPhone(t.getContactPhone())
                .contactEmail(t.getContactEmail())
                .description(t.getDescription())
                .ownerUserId(ownerId)
                .ownerEmail(ownerEmail)
                .ownerName(ownerName)
                .build();
    }

    @Transactional
    public void approveTenant(@NonNull Long tenantId) {
        Tenant t = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        if (t.getStatus() != TenantStatusEnum.PENDING) {
            throw new BadRequestException("Chỉ duyệt được yêu cầu đang ở trạng thái PENDING");
        }
        t.setStatus(TenantStatusEnum.APPROVED);
        tenantRepository.save(t);
        for (TenantUser tu : tenantUserRepository.findByTenant_Id(tenantId)) {
            if ("OWNER".equals(tu.getRole())) {
                tu.setActive(true);
                tenantUserRepository.save(tu);
            }
        }
    }

    @Transactional
    public void rejectTenant(@NonNull Long tenantId) {
        Tenant t = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        if (t.getStatus() != TenantStatusEnum.PENDING) {
            throw new BadRequestException("Chỉ từ chối được yêu cầu đang ở trạng thái PENDING");
        }
        t.setStatus(TenantStatusEnum.REJECTED);
        tenantRepository.save(t);
        for (TenantUser tu : tenantUserRepository.findByTenant_Id(tenantId)) {
            tu.setActive(false);
            tenantUserRepository.save(tu);
        }
    }

    @Transactional(readOnly = true)
    public ResTenantAdminDTO getTenantByIdForAdmin(@NonNull Long tenantId) {
        Tenant t = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        return toAdminDto(t);
    }

    @Transactional
    public ResTenantAdminDTO createTenantByAdmin(@NonNull ReqCreateTenantAdminDTO req) {
        userRepository.findById(req.getOwnerUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản chủ sân (user id)"));
        TenantStatusEnum st = parseAdminStatusOrDefault(req.getStatus(), TenantStatusEnum.PENDING);
        String name = req.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Tên tenant không hợp lệ");
        }
        String slugIn = blankToNull(req.getSlug());
        String slug = ensureUniqueSlug(slugify(slugIn != null ? slugIn : name));
        Tenant t = new Tenant();
        t.setName(name);
        t.setSlug(slug);
        t.setStatus(st);
        t.setContactPhone(blankToNull(req.getContactPhone()));
        t.setContactEmail(blankToNull(req.getContactEmail()));
        t.setDescription(blankToNull(req.getDescription()));
        t = tenantRepository.save(t);
        TenantUser link = new TenantUser();
        link.setTenant(t);
        link.setUser(userRepository.getReferenceById(req.getOwnerUserId()));
        link.setRole("OWNER");
        link.setActive(st == TenantStatusEnum.APPROVED);
        tenantUserRepository.save(link);
        return toAdminDto(t);
    }

    @Transactional
    public ResTenantAdminDTO updateTenantByAdmin(@NonNull Long tenantId, @NonNull ReqUpdateTenantAdminDTO req) {
        Tenant t = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        String newSlug = req.getSlug().trim();
        if (newSlug.isEmpty()) {
            throw new BadRequestException("Slug không hợp lệ");
        }
        tenantRepository.findBySlug(newSlug).ifPresent(other -> {
            if (!other.getId().equals(tenantId)) {
                throw new BadRequestException("Slug đã được sử dụng bởi tenant khác");
            }
        });
        TenantStatusEnum st = parseAdminStatusOrDefault(req.getStatus(), null);
        if (st == null) {
            throw new BadRequestException("Trạng thái không hợp lệ");
        }
        t.setName(req.getName().trim());
        t.setSlug(newSlug);
        t.setContactPhone(blankToNull(req.getContactPhone()));
        t.setContactEmail(blankToNull(req.getContactEmail()));
        t.setDescription(blankToNull(req.getDescription()));
        t.setStatus(st);
        tenantRepository.save(t);
        for (TenantUser tu : tenantUserRepository.findByTenant_Id(tenantId)) {
            boolean active = st == TenantStatusEnum.APPROVED;
            tu.setActive(active);
            tenantUserRepository.save(tu);
        }
        return toAdminDto(t);
    }

    @Transactional
    public void deleteTenantByAdmin(@NonNull Long tenantId) {
        if (tenantId.equals(DEFAULT_TENANT_ID)) {
            throw new BadRequestException("Không được xóa tenant mặc định (hệ thống)");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new BadRequestException("Tenant không tồn tại");
        }
        if (pitchRepository.countByTenantId(tenantId) > 0) {
            throw new BadRequestException(
                    "Không thể xóa tenant đã có sân (pitch). Xóa hoặc gán lại sân trước khi xóa tenant.");
        }
        subscriptionService.deleteAllByTenantId(tenantId);
        tenantUserRepository.deleteByTenant_Id(tenantId);
        tenantRepository.deleteById(tenantId);
    }

    private static TenantStatusEnum parseAdminStatusOrDefault(String raw, TenantStatusEnum defaultIfNull) {
        if (raw == null || raw.isBlank()) {
            return defaultIfNull;
        }
        try {
            return TenantStatusEnum.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + raw);
        }
    }

    @Transactional(readOnly = true)
    public boolean isUserInTenant(@NonNull Long userId, @NonNull Long tenantId) {
        return tenantUserRepository.existsByUser_IdAndTenant_Id(userId, tenantId);
    }

    @Transactional(readOnly = true)
    public boolean isTenantShopApproved(@NonNull Long tenantId) {
        return tenantRepository.findById(tenantId)
                .map(t -> t.getStatus() == TenantStatusEnum.APPROVED)
                .orElse(false);
    }

    public long resolveEffectiveTenantId(@NonNull Long userId, Long jwtTenantId, String headerTenantId) {
        if (headerTenantId != null && !headerTenantId.isBlank()) {
            long requested;
            try {
                requested = Long.parseLong(headerTenantId.trim());
            } catch (NumberFormatException ex) {
                throw new BadRequestException("X-Tenant-Id không hợp lệ");
            }
            if (isUserInTenant(userId, requested)) {
                return requested;
            }
            // Client gửi X-Tenant-Id cũ / lệch (localStorage) — không ném 400, dùng JWT rồi mặc định
        }
        if (jwtTenantId != null) {
            return jwtTenantId;
        }
        return DEFAULT_TENANT_ID;
    }

    @Transactional
    public void ensureMembership(@NonNull Long userId, @NonNull Long tenantId) {
        if (tenantUserRepository.existsByUser_IdAndTenant_Id(userId, tenantId)) {
            return;
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        TenantUser link = new TenantUser();
        link.setTenant(tenant);
        link.setUser(userRepository.getReferenceById(userId));
        link.setRole("MEMBER");
        link.setActive(tenant.getStatus() == TenantStatusEnum.APPROVED);
        tenantUserRepository.save(link);
    }

    private static String slugify(String raw) {
        if (raw == null || raw.isBlank()) {
            return "shop";
        }
        String s = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (s.isBlank()) {
            return "shop";
        }
        return s.length() > 120 ? s.substring(0, 120) : s;
    }

    private String ensureUniqueSlug(String base) {
        for (int attempt = 0; attempt < 40; attempt++) {
            String candidate = attempt == 0 ? base : base + "-" + ThreadLocalRandom.current().nextInt(100, 99999);
            if (candidate.length() > 120) {
                candidate = candidate.substring(0, 120);
            }
            if (tenantRepository.findBySlug(candidate).isEmpty()) {
                return candidate;
            }
        }
        return "t-" + System.currentTimeMillis();
    }
}
