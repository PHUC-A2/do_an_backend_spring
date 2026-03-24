package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.entity.Checkout;
import com.example.backend.domain.request.checkout.ReqCreateCheckoutDTO;
import com.example.backend.domain.request.checkout.ReqUpdateCheckoutDTO;
import com.example.backend.domain.response.checkout.ResCheckoutDetailDTO;
import com.example.backend.domain.response.checkout.ResCheckoutListDTO;
import com.example.backend.domain.response.checkout.ResCreateCheckoutDTO;
import com.example.backend.domain.response.checkout.ResUpdateCheckoutDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.CheckoutRepository;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * Nhận tài sản theo đăng ký đã duyệt — 1 {@link AssetUsage} chỉ 1 {@link Checkout}; chỉ tạo khi status APPROVED.
 */
@Service
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final AssetUsageRepository assetUsageRepository;
    private final UserService userService;
    private final RoomBookingDeviceService roomBookingDeviceService;

    public CheckoutService(
            CheckoutRepository checkoutRepository,
            AssetUsageRepository assetUsageRepository,
            UserService userService,
            RoomBookingDeviceService roomBookingDeviceService) {
        this.checkoutRepository = checkoutRepository;
        this.assetUsageRepository = assetUsageRepository;
        this.userService = userService;
        this.roomBookingDeviceService = roomBookingDeviceService;
    }

    @Transactional
    public ResCreateCheckoutDTO createCheckout(@NonNull ReqCreateCheckoutDTO req) {
        AssetUsage usage = assetUsageRepository.findById(req.getAssetUsageId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đăng ký sử dụng ID = " + req.getAssetUsageId()));

        if (usage.getStatus() != AssetUsageStatus.APPROVED) {
            throw new BadRequestException("Chỉ được nhận tài sản khi đăng ký đã ở trạng thái Đã duyệt (APPROVED)");
        }

        if (checkoutRepository.existsByAssetUsageId(req.getAssetUsageId())) {
            throw new BadRequestException("Đăng ký này đã có phiếu nhận (checkout)");
        }

        Checkout c = new Checkout();
        c.setAssetUsage(usage);
        c.setReceiveTime(req.getReceiveTime() != null ? req.getReceiveTime() : java.time.Instant.now());
        c.setConditionNote(req.getConditionNote() != null ? req.getConditionNote().trim() : null);

        Checkout saved = checkoutRepository.save(c);

        usage.setStatus(AssetUsageStatus.IN_PROGRESS);
        assetUsageRepository.save(usage);

        // Clone flow booking sân: khi đã nhận phòng (checkout tạo xong), tạo dòng mượn theo booking phòng
        // để admin có thể quản lý mượn/trả theo từng thiết bị và thống kê theo thiết bị/phòng.
        roomBookingDeviceService.createBorrowLinesForAssetUsageIfNeeded(usage);

        return convertToResCreateCheckoutDTO(saved);
    }

    @Transactional
    public ResCreateCheckoutDTO createCheckoutForCurrentUser(
            @NonNull Long assetUsageId,
            java.time.Instant receiveTime,
            String conditionNote,
            @NonNull String email) {
        var user = userService.handleGetUserByUsername(email);
        AssetUsage usage = assetUsageRepository.findById(assetUsageId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đăng ký sử dụng ID = " + assetUsageId));
        if (usage.getUser() == null || !usage.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền tạo biên bản nhận cho đăng ký này");
        }
        ReqCreateCheckoutDTO req = new ReqCreateCheckoutDTO();
        req.setAssetUsageId(assetUsageId);
        req.setReceiveTime(receiveTime);
        req.setConditionNote(conditionNote);
        return createCheckout(req);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllCheckouts(Specification<Checkout> spec, @NonNull Pageable pageable) {
        Page<Checkout> page = checkoutRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResCheckoutListDTO> list = new ArrayList<>();
        for (Checkout c : page.getContent()) {
            list.add(convertToResCheckoutListDTO(c));
        }
        rs.setResult(list);
        return rs;
    }

    public Checkout getCheckoutById(@NonNull Long id) throws IdInvalidException {
        Optional<Checkout> opt = checkoutRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy Checkout với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResCheckoutDetailDTO getCheckoutDetailById(@NonNull Long id) throws IdInvalidException {
        return convertToResCheckoutDetailDTO(getCheckoutById(id));
    }

    @Transactional(readOnly = true)
    public ResCheckoutDetailDTO getCheckoutDetailByAssetUsageIdForCurrentUser(@NonNull Long assetUsageId, @NonNull String email)
            throws IdInvalidException {
        Checkout checkout = checkoutRepository.findByAssetUsageId(assetUsageId)
                .orElseThrow(() -> new IdInvalidException("Đăng ký phòng này chưa có biên bản nhận tài sản"));
        AssetUsage usage = checkout.getAssetUsage();
        if (usage == null || usage.getUser() == null || !usage.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Không có quyền xem biên bản nhận tài sản này");
        }
        return convertToResCheckoutDetailDTO(checkout);
    }

    @Transactional
    public ResUpdateCheckoutDTO updateCheckout(@NonNull Long id, @NonNull ReqUpdateCheckoutDTO req) throws IdInvalidException {
        Checkout c = getCheckoutById(id);
        c.setReceiveTime(req.getReceiveTime());
        c.setConditionNote(req.getConditionNote() != null ? req.getConditionNote().trim() : null);
        Checkout saved = checkoutRepository.save(c);
        return convertToResUpdateCheckoutDTO(saved);
    }

    @Transactional
    public void deleteCheckout(@NonNull Long id) throws IdInvalidException {
        Checkout c = getCheckoutById(id);
        AssetUsage usage = c.getAssetUsage();
        if (usage != null && usage.getStatus() == AssetUsageStatus.IN_PROGRESS) {
            usage.setStatus(AssetUsageStatus.APPROVED);
            assetUsageRepository.save(usage);
        }
        checkoutRepository.delete(c);
    }

    private ResCheckoutListDTO convertToResCheckoutListDTO(Checkout c) {
        ResCheckoutListDTO res = new ResCheckoutListDTO();
        res.setId(c.getId());
        res.setReceiveTime(c.getReceiveTime());
        res.setConditionNote(c.getConditionNote());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        res.setCreatedBy(c.getCreatedBy());
        res.setUpdatedBy(c.getUpdatedBy());
        AssetUsage u = c.getAssetUsage();
        if (u != null) {
            res.setAssetUsageId(u.getId());
            res.setUsageType(u.getUsageType());
            res.setUsageDate(u.getUsageDate());
            res.setStartTime(u.getStartTime());
            res.setEndTime(u.getEndTime());
            res.setSubject(u.getSubject());
            res.setAssetUsageStatus(u.getStatus());
            if (u.getUser() != null) {
                res.setUserId(u.getUser().getId());
                res.setUserName(u.getUser().getName());
                res.setUserEmail(u.getUser().getEmail());
            }
            if (u.getAsset() != null) {
                res.setAssetId(u.getAsset().getId());
                res.setAssetName(u.getAsset().getAssetName());
            }
        }
        return res;
    }

    private ResCheckoutDetailDTO convertToResCheckoutDetailDTO(Checkout c) {
        ResCheckoutDetailDTO res = new ResCheckoutDetailDTO();
        res.setId(c.getId());
        res.setReceiveTime(c.getReceiveTime());
        res.setConditionNote(c.getConditionNote());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        res.setCreatedBy(c.getCreatedBy());
        res.setUpdatedBy(c.getUpdatedBy());
        AssetUsage u = c.getAssetUsage();
        if (u != null) {
            res.setAssetUsageId(u.getId());
            res.setUsageType(u.getUsageType());
            res.setUsageDate(u.getUsageDate());
            res.setStartTime(u.getStartTime());
            res.setEndTime(u.getEndTime());
            res.setSubject(u.getSubject());
            res.setAssetUsageStatus(u.getStatus());
            if (u.getUser() != null) {
                res.setUserId(u.getUser().getId());
                res.setUserName(u.getUser().getName());
                res.setUserEmail(u.getUser().getEmail());
            }
            if (u.getAsset() != null) {
                res.setAssetId(u.getAsset().getId());
                res.setAssetName(u.getAsset().getAssetName());
            }
        }
        return res;
    }

    private ResCreateCheckoutDTO convertToResCreateCheckoutDTO(Checkout c) {
        ResCreateCheckoutDTO res = new ResCreateCheckoutDTO();
        res.setId(c.getId());
        res.setAssetUsageId(c.getAssetUsage() != null ? c.getAssetUsage().getId() : null);
        res.setReceiveTime(c.getReceiveTime());
        res.setConditionNote(c.getConditionNote());
        res.setCreatedAt(c.getCreatedAt());
        return res;
    }

    private ResUpdateCheckoutDTO convertToResUpdateCheckoutDTO(Checkout c) {
        ResUpdateCheckoutDTO res = new ResUpdateCheckoutDTO();
        res.setId(c.getId());
        res.setAssetUsageId(c.getAssetUsage() != null ? c.getAssetUsage().getId() : null);
        res.setReceiveTime(c.getReceiveTime());
        res.setConditionNote(c.getConditionNote());
        res.setUpdatedAt(c.getUpdatedAt());
        res.setUpdatedBy(c.getUpdatedBy());
        return res;
    }
}
