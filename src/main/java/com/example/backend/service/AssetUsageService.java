package com.example.backend.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.assetusage.ReqCreateClientAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqCreateAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqUpdateClientAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqUpdateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageDetailDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageListDTO;
import com.example.backend.domain.response.assetusage.ResCreateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResUpdateAssetUsageDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * CRUD đăng ký sử dụng tài sản — kiểm tra trùng khung giờ theo db.md (asset + date + start/end).
 */
@Service
public class AssetUsageService {

    private static final Set<AssetUsageStatus> OVERLAP_BLOCKING = EnumSet.of(
            AssetUsageStatus.PENDING,
            AssetUsageStatus.APPROVED,
            AssetUsageStatus.IN_PROGRESS);

    private final AssetUsageRepository assetUsageRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public AssetUsageService(
            AssetUsageRepository assetUsageRepository,
            UserRepository userRepository,
            AssetRepository assetRepository,
            UserService userService,
            NotificationService notificationService) {
        this.assetUsageRepository = assetUsageRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Tạo đăng ký sử dụng phòng cho user client (tự lấy user theo token, luôn tạo PENDING).
     */
    @Transactional
    public ResCreateAssetUsageDTO createClientAssetUsage(
            @NonNull ReqCreateClientAssetUsageDTO req,
            @NonNull String email) {
        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        User user = userService.handleGetUserByUsername(email);
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        assertNoOverlap(req.getAssetId(), req.getDate(), req.getStartTime(), req.getEndTime(), 0L);

        AssetUsage u = new AssetUsage();
        u.setUser(user);
        u.setAsset(asset);
        // Nếu user có chọn thiết bị mượn (borrowDevicesJson != null) thì đánh dấu booking là “BORROW”
        // để admin/history hiển thị đúng luồng “mượn - trả thiết bị”.
        boolean hasBorrowDevices = req.getBorrowDevicesJson() != null && !req.getBorrowDevicesJson().trim().isEmpty();
        u.setUsageType(hasBorrowDevices ? AssetUsageType.BORROW : AssetUsageType.RENT);
        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        // Bắt buộc có số điện thoại:
        // - Ưu tiên user nhập ở ô contactPhone
        // - Nếu user không nhập (null/rỗng) thì lấy từ profile (User.phoneNumber)
        // - Nếu cả hai đều rỗng thì báo lỗi
        u.setContactPhone(resolveContactPhone(user, req.getContactPhone()));
        u.setBookingNote(req.getBookingNote() != null ? req.getBookingNote().trim() : null);
        u.setBorrowDevicesJson(req.getBorrowDevicesJson() != null ? req.getBorrowDevicesJson().trim() : null);
        u.setBorrowNote(req.getBorrowNote() != null ? req.getBorrowNote().trim() : null);
        u.setBorrowConditionAcknowledged(req.getBorrowConditionAcknowledged() != null ? req.getBorrowConditionAcknowledged() : false);
        u.setBorrowReportPrintOptIn(req.getBorrowReportPrintOptIn() != null ? req.getBorrowReportPrintOptIn() : false);
        u.setStatus(AssetUsageStatus.PENDING);

        AssetUsage saved = assetUsageRepository.save(u);

        String roomName = asset.getAssetName() != null ? asset.getAssetName() : "phòng";
        String bookingTime = String.format("%s %s-%s",
                req.getDate(),
                req.getStartTime(),
                req.getEndTime());
        String userMsg = String.format(
                "Yêu cầu đặt phòng đã được gửi! RoomBooking #%d – %s lúc %s đang chờ admin xác nhận.",
                saved.getId(),
                roomName,
                bookingTime);
        notificationService.createAndPush(user, NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION, userMsg);

        String requesterName = user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getName();
        String adminMsg = String.format(
                "Có yêu cầu đặt phòng mới cần xác nhận. RoomBooking #%d – %s đặt phòng %s lúc %s.",
                saved.getId(),
                requesterName,
                roomName,
                bookingTime);
        notificationService.notifyAdmins(NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION, adminMsg, user.getEmail());

        return convertToResCreateAssetUsageDTO(saved);
    }

    @Transactional
    public ResCreateAssetUsageDTO createAssetUsage(@NonNull ReqCreateAssetUsageDTO req) {
        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user với ID = " + req.getUserId()));
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        assertNoOverlap(req.getAssetId(), req.getDate(), req.getStartTime(), req.getEndTime(), 0L);

        AssetUsage u = new AssetUsage();

        u.setUser(user);
        u.setAsset(asset);
        u.setUsageType(req.getUsageType());
        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        u.setStatus(req.getStatus() != null ? req.getStatus() : AssetUsageStatus.PENDING);

        AssetUsage saved = assetUsageRepository.save(u);
        return convertToResCreateAssetUsageDTO(saved);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllAssetUsages(Specification<AssetUsage> spec, @NonNull Pageable pageable) {
        Page<AssetUsage> page = assetUsageRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResAssetUsageListDTO> list = new ArrayList<>();
        for (AssetUsage u : page.getContent()) {
            list.add(convertToResAssetUsageListDTO(u));
        }
        rs.setResult(list);
        return rs;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllAssetUsagesOfCurrentUser(@NonNull String email, @NonNull Pageable pageable) {
        User user = userService.handleGetUserByUsername(email);
        // Khi client xóa khỏi lịch sử (soft delete) thì admin vẫn thấy, còn client sẽ không nhìn thấy lại.
        Specification<AssetUsage> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), user.getId()),
                cb.isFalse(root.get("deletedByUser")));
        return getAllAssetUsages(spec, pageable);
    }

    public AssetUsage getAssetUsageById(@NonNull Long id) throws IdInvalidException {
        Optional<AssetUsage> opt = assetUsageRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy AssetUsage với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResAssetUsageDetailDTO getAssetUsageDetailById(@NonNull Long id) throws IdInvalidException {
        AssetUsage u = getAssetUsageById(id);
        return convertToResAssetUsageDetailDTO(u);
    }

    @Transactional(readOnly = true)
    public ResAssetUsageDetailDTO getAssetUsageDetailByIdForCurrentUser(@NonNull Long id, @NonNull String email)
            throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        AssetUsage u = getAssetUsageById(id);
        if (u.getUser() == null || !u.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền truy cập đăng ký phòng này");
        }
        return convertToResAssetUsageDetailDTO(u);
    }

    @Transactional
    public ResUpdateAssetUsageDTO updateAssetUsage(@NonNull Long id, @NonNull ReqUpdateAssetUsageDTO req)
            throws IdInvalidException {
        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        AssetUsage u = getAssetUsageById(id);
        AssetUsageStatus oldStatus = u.getStatus();
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user với ID = " + req.getUserId()));
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        assertNoOverlap(req.getAssetId(), req.getDate(), req.getStartTime(), req.getEndTime(), id);

        u.setUser(user);
        u.setAsset(asset);
        u.setUsageType(req.getUsageType());
        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        u.setStatus(req.getStatus());

        AssetUsage saved = assetUsageRepository.save(u);

        if (saved.getUser() != null && oldStatus != saved.getStatus()) {
            String roomName = asset.getAssetName() != null ? asset.getAssetName() : "phòng";
            String bookingTime = String.format("%s %s-%s",
                    req.getDate(),
                    req.getStartTime(),
                    req.getEndTime());

            if (saved.getStatus() == AssetUsageStatus.APPROVED) {
                String msg = String.format(
                        "RoomBooking #%d – %s lúc %s đã được admin xác nhận.",
                        saved.getId(),
                        roomName,
                        bookingTime);
                notificationService.createAndPush(saved.getUser(), NotificationTypeEnum.BOOKING_APPROVED, msg);
            } else if (saved.getStatus() == AssetUsageStatus.REJECTED || saved.getStatus() == AssetUsageStatus.CANCELLED) {
                String msg = String.format(
                        "RoomBooking #%d – %s lúc %s đã bị admin từ chối.",
                        saved.getId(),
                        roomName,
                        bookingTime);
                notificationService.createAndPush(saved.getUser(), NotificationTypeEnum.BOOKING_REJECTED, msg);
            }
        }

        return convertToResUpdateAssetUsageDTO(saved);
    }

    @Transactional
    public ResUpdateAssetUsageDTO updateClientAssetUsage(
            @NonNull Long id,
            @NonNull ReqUpdateClientAssetUsageDTO req,
            @NonNull String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        AssetUsage u = getAssetUsageById(id);
        if (u.getUser() == null || !u.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền cập nhật đăng ký phòng này");
        }
        if (u.isDeletedByUser()) {
            throw new BadRequestException("Đăng ký đã bị xóa khỏi lịch sử");
        }
        if (u.getStatus() == AssetUsageStatus.CANCELLED
                || u.getStatus() == AssetUsageStatus.REJECTED
                || u.getStatus() == AssetUsageStatus.COMPLETED) {
            throw new BadRequestException("Không thể cập nhật đăng ký phòng ở trạng thái hiện tại");
        }

        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        assertNoOverlap(u.getAsset().getId(), req.getDate(), req.getStartTime(), req.getEndTime(), id);

        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        // Bắt buộc có số điện thoại với 2 luồng giống booking sân.
        u.setContactPhone(resolveContactPhone(user, req.getContactPhone()));
        u.setBookingNote(req.getBookingNote() != null ? req.getBookingNote().trim() : null);
        u.setBorrowDevicesJson(req.getBorrowDevicesJson() != null ? req.getBorrowDevicesJson().trim() : null);
        u.setBorrowNote(req.getBorrowNote() != null ? req.getBorrowNote().trim() : null);
        u.setBorrowConditionAcknowledged(req.getBorrowConditionAcknowledged() != null ? req.getBorrowConditionAcknowledged() : false);
        u.setBorrowReportPrintOptIn(req.getBorrowReportPrintOptIn() != null ? req.getBorrowReportPrintOptIn() : false);

        AssetUsage saved = assetUsageRepository.save(u);
        return convertToResUpdateAssetUsageDTO(saved);
    }

    @Transactional
    public void cancelClientAssetUsage(@NonNull Long id, @NonNull String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        AssetUsage u = getAssetUsageById(id);
        if (u.getUser() == null || !u.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền hủy đăng ký phòng này");
        }
        if (u.isDeletedByUser()) {
            throw new BadRequestException("Đăng ký đã bị xóa khỏi lịch sử");
        }
        if (u.getStatus() == AssetUsageStatus.IN_PROGRESS || u.getStatus() == AssetUsageStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy khi phòng đang sử dụng hoặc đã hoàn tất");
        }
        if (u.getStatus() == AssetUsageStatus.CANCELLED) {
            throw new BadRequestException("Đăng ký đã hủy trước đó");
        }
        u.setStatus(AssetUsageStatus.CANCELLED);
        assetUsageRepository.save(u);
    }

    /**
     * Soft delete: ẩn đăng ký khỏi lịch sử của user.
     * - Không xóa khỏi DB để admin vẫn dùng cho thống kê / in biên bản.
     */
    @Transactional
    public void deleteAssetUsageForCurrentUser(@NonNull Long id, @NonNull String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        AssetUsage u = getAssetUsageById(id);
        if (u.getUser() == null || !u.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền xóa đăng ký phòng này");
        }
        if (u.isDeletedByUser()) {
            throw new BadRequestException("Đăng ký đã bị xóa khỏi lịch sử");
        }
        // Giống luồng booking sân: chỉ cho xóa khi booking đã kết thúc / hủy / từ chối.
        if (!(u.getStatus() == AssetUsageStatus.COMPLETED
                || u.getStatus() == AssetUsageStatus.CANCELLED
                || u.getStatus() == AssetUsageStatus.REJECTED)) {
            throw new BadRequestException("Chỉ có thể xóa khỏi lịch sử khi booking đã kết thúc");
        }
        u.setDeletedByUser(true);
        assetUsageRepository.save(u);
    }

    @Transactional
    public void deleteAssetUsage(@NonNull Long id) throws IdInvalidException {
        getAssetUsageById(id);
        assetUsageRepository.deleteById(id);
    }

    public ResAssetUsageDetailDTO convertToResAssetUsageDetailDTO(AssetUsage u) {
        ResAssetUsageDetailDTO res = new ResAssetUsageDetailDTO();
        res.setId(u.getId());
        if (u.getUser() != null) {
            res.setUserId(u.getUser().getId());
            res.setUserName(u.getUser().getName());
            res.setUserEmail(u.getUser().getEmail());
        }
        if (u.getAsset() != null) {
            res.setAssetId(u.getAsset().getId());
            res.setAssetName(u.getAsset().getAssetName());
        }
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setContactPhone(u.getContactPhone());
        res.setBookingNote(u.getBookingNote());
        res.setBorrowDevicesJson(u.getBorrowDevicesJson());
        res.setBorrowNote(u.getBorrowNote());
        res.setBorrowConditionAcknowledged(u.isBorrowConditionAcknowledged());
        res.setBorrowReportPrintOptIn(u.isBorrowReportPrintOptIn());
        res.setStatus(u.getStatus());
        res.setAssetResponsibleName(u.getAsset() != null ? u.getAsset().getResponsibleName() : null);
        res.setAssetAssetsUrl(u.getAsset() != null ? u.getAsset().getAssetsUrl() : null);
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setCreatedBy(u.getCreatedBy());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private ResAssetUsageListDTO convertToResAssetUsageListDTO(AssetUsage u) {
        ResAssetUsageListDTO res = new ResAssetUsageListDTO();
        res.setId(u.getId());
        if (u.getUser() != null) {
            res.setUserId(u.getUser().getId());
            res.setUserName(u.getUser().getName());
            res.setUserEmail(u.getUser().getEmail());
        }
        if (u.getAsset() != null) {
            res.setAssetId(u.getAsset().getId());
            res.setAssetName(u.getAsset().getAssetName());
        }
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setContactPhone(u.getContactPhone());
        res.setBookingNote(u.getBookingNote());
        res.setBorrowDevicesJson(u.getBorrowDevicesJson());
        res.setBorrowNote(u.getBorrowNote());
        res.setBorrowConditionAcknowledged(u.isBorrowConditionAcknowledged());
        res.setBorrowReportPrintOptIn(u.isBorrowReportPrintOptIn());
        res.setStatus(u.getStatus());
        res.setAssetResponsibleName(u.getAsset() != null ? u.getAsset().getResponsibleName() : null);
        res.setAssetAssetsUrl(u.getAsset() != null ? u.getAsset().getAssetsUrl() : null);
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setCreatedBy(u.getCreatedBy());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private ResCreateAssetUsageDTO convertToResCreateAssetUsageDTO(AssetUsage u) {
        ResCreateAssetUsageDTO res = new ResCreateAssetUsageDTO();
        res.setId(u.getId());
        res.setUserId(u.getUser() != null ? u.getUser().getId() : null);
        res.setAssetId(u.getAsset() != null ? u.getAsset().getId() : null);
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setCreatedAt(u.getCreatedAt());
        return res;
    }

    private ResUpdateAssetUsageDTO convertToResUpdateAssetUsageDTO(AssetUsage u) {
        ResUpdateAssetUsageDTO res = new ResUpdateAssetUsageDTO();
        res.setId(u.getId());
        res.setUserId(u.getUser() != null ? u.getUser().getId() : null);
        res.setAssetId(u.getAsset() != null ? u.getAsset().getId() : null);
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private void assertValidTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc");
        }
    }

    private void assertNoOverlap(Long assetId, java.time.LocalDate date, java.time.LocalTime start,
            java.time.LocalTime end, long excludeId) {
        long n = assetUsageRepository.countTimeOverlap(assetId, date, start, end, excludeId, OVERLAP_BLOCKING);
        if (n > 0) {
            throw new BadRequestException("Khung giờ trùng với đăng ký khác trên cùng tài sản và ngày");
        }
    }

    /**
     * Resolve số điện thoại liên hệ cho rooms booking (khớp logic booking sân).
     *
     * Luồng:
     * 1) Nếu user nhập contactPhone không rỗng => ưu tiên contactPhone đó
     * 2) Nếu user nhập rỗng/null => lấy User.phoneNumber từ profile
     * 3) Nếu cả hai đều rỗng => ném BadRequestException để FE hiển thị message
     */
    private String resolveContactPhone(@NonNull User user, String phone) {
        if (phone != null) {
            String trimmed = phone.trim();
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }
        if (user.getPhoneNumber() != null) {
            String trimmed = user.getPhoneNumber().trim();
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }
        throw new BadRequestException("Vui lòng nhập số điện thoại");
    }
}
