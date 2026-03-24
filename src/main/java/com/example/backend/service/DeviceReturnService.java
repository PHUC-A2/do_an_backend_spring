package com.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.example.backend.domain.entity.DeviceReturn;
import com.example.backend.domain.entity.RoomBookingDevice;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.request.assetusage.ReqCreateClientReturnDTO;
import com.example.backend.domain.request.devicereturn.ReqCreateDeviceReturnDTO;
import com.example.backend.domain.request.devicereturn.ReqUpdateDeviceReturnDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.devicereturn.ResCreateDeviceReturnDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnDetailDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnListDTO;
import com.example.backend.domain.response.devicereturn.ResUpdateDeviceReturnDTO;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.CheckoutRepository;
import com.example.backend.repository.DeviceReturnRepository;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * Phiếu trả — 1 checkout 1 return; chỉ tạo khi {@link AssetUsage} đang IN_PROGRESS (đã nhận).
 */
@Service
public class DeviceReturnService {

    private final DeviceReturnRepository deviceReturnRepository;
    private final CheckoutRepository checkoutRepository;
    private final AssetUsageRepository assetUsageRepository;
    private final UserService userService;
    private final RoomBookingDeviceService roomBookingDeviceService;

    /** ObjectMapper dùng cho parsing borrowDevicesJson. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceReturnService(
            DeviceReturnRepository deviceReturnRepository,
            CheckoutRepository checkoutRepository,
            AssetUsageRepository assetUsageRepository,
            UserService userService,
            RoomBookingDeviceService roomBookingDeviceService) {
        this.deviceReturnRepository = deviceReturnRepository;
        this.checkoutRepository = checkoutRepository;
        this.assetUsageRepository = assetUsageRepository;
        this.userService = userService;
        this.roomBookingDeviceService = roomBookingDeviceService;
    }

    @Transactional
    public ResCreateDeviceReturnDTO createDeviceReturn(@NonNull ReqCreateDeviceReturnDTO req) {
        Checkout checkout = checkoutRepository.findById(req.getCheckoutId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy checkout ID = " + req.getCheckoutId()));

        if (deviceReturnRepository.existsByCheckoutId(req.getCheckoutId())) {
            throw new BadRequestException("Checkout này đã có phiếu trả");
        }

        AssetUsage usage = checkout.getAssetUsage();
        if (usage == null) {
            throw new BadRequestException("Checkout không gắn đăng ký sử dụng");
        }
        if (usage.getStatus() != AssetUsageStatus.IN_PROGRESS) {
            throw new BadRequestException("Chỉ được trả khi đăng ký đang ở trạng thái Đang dùng (đã nhận tài sản)");
        }

        // Tính tổng số lượng "thiết bị phòng" để validate trả tốt / mất / hỏng.
        // - Nếu borrowDevicesJson rỗng => coi như 1 "đơn vị" (phòng/tài sản).
        // - Nếu borrowDevicesJson có dữ liệu => cộng theo field quantity.
        int qTotal = resolveReturnTotalQuantity(usage);

        int g = req.getQuantityReturnedGood() != null ? req.getQuantityReturnedGood() : 0;
        int l = req.getQuantityLost() != null ? req.getQuantityLost() : 0;
        int d = req.getQuantityDamaged() != null ? req.getQuantityDamaged() : 0;

        // Nếu FE chưa gửi breakdown (cả g/l/d đều = 0) => tự động fill theo deviceStatus.
        if (g + l + d == 0) {
            switch (req.getDeviceStatus()) {
                case GOOD -> g = qTotal;
                case LOST -> l = qTotal;
                case DAMAGED -> d = qTotal;
                case BROKEN -> d = qTotal;
            }
        }

        if (g < 0 || l < 0 || d < 0) {
            throw new BadRequestException("Số lượng trả (tốt/mất/hỏng) không hợp lệ");
        }
        if (g + l + d != qTotal) {
            throw new BadRequestException(
                    String.format("Tổng trả tốt + mất + hỏng (%d) phải bằng tổng mượn (%d).", g + l + d, qTotal));
        }

        // Cho phép breakdown linh hoạt giống nghiệp vụ thực tế:
        // ví dụ mượn 20 có thể trả 9 tốt + 10 hỏng + 1 mất.
        // Chỉ cần ràng buộc tổng và số âm (đã validate ở trên).

        // Validate người ký xác nhận khi có mất / hỏng.
        boolean hasDrop = l + d > 0;
        String borrowerSignName = trimToNull(req.getBorrowerSignName());
        String staffSignName = trimToNull(req.getStaffSignName());
        if (hasDrop) {
            if (borrowerSignName == null || staffSignName == null) {
                throw new BadRequestException("Vui lòng ghi họ tên người mượn và nhân viên ký xác nhận khi có mất/hỏng");
            }
        }

        // validate thông tin người nhận (bắt buộc) để in biên bản.
        String receiverNameSnapshot = trimToNull(req.getReceiverName());
        String receiverPhoneSnapshot = trimToNull(req.getReceiverPhone());
        if (receiverNameSnapshot == null || receiverPhoneSnapshot == null) {
            throw new BadRequestException("Vui lòng nhập họ tên và số điện thoại người nhận thiết bị tại sân");
        }

        // Resolve snapshot tên người trả / SĐT người trả (fallback từ user/contactPhone).
        User u = usage.getUser();
        String fallbackReturnerName = userDisplayName(u);
        String fallbackReturnerPhone = safePhone(usage.getContactPhone(), u);

        String returnerNameSnapshot = trimToNull(req.getReturnerName());
        if (returnerNameSnapshot == null) {
            returnerNameSnapshot = fallbackReturnerName;
        }

        String returnerPhoneSnapshot = trimToNull(req.getReturnerPhone());
        if (returnerPhoneSnapshot == null) {
            returnerPhoneSnapshot = fallbackReturnerPhone;
        }

        // Suy ra trạng thái tổng quát từ breakdown để dữ liệu nhất quán.
        com.example.backend.util.constant.devicereturn.DeviceCondition derivedStatus;
        if (l == 0 && d == 0) {
            derivedStatus = com.example.backend.util.constant.devicereturn.DeviceCondition.GOOD;
        } else if (l > 0 && d == 0 && g == 0) {
            derivedStatus = com.example.backend.util.constant.devicereturn.DeviceCondition.LOST;
        } else if (d > 0 && l == 0 && g == 0) {
            derivedStatus = com.example.backend.util.constant.devicereturn.DeviceCondition.DAMAGED;
        } else {
            // Mixed case hoặc vừa hỏng/vừa mất => dùng BROKEN để biểu diễn tình trạng không bình thường tổng hợp.
            derivedStatus = com.example.backend.util.constant.devicereturn.DeviceCondition.BROKEN;
        }

        DeviceReturn r = new DeviceReturn();
        r.setCheckout(checkout);
        r.setReturnTime(req.getReturnTime() != null ? req.getReturnTime() : java.time.Instant.now());
        r.setDeviceStatus(derivedStatus);

        // Lưu breakdown + chữ ký/snapshot để in biên bản.
        r.setQuantityReturnedGood(g);
        r.setQuantityLost(l);
        r.setQuantityDamaged(d);
        r.setBorrowerSignName(borrowerSignName);
        r.setStaffSignName(staffSignName);
        r.setReturnerNameSnapshot(returnerNameSnapshot);
        r.setReturnerPhoneSnapshot(returnerPhoneSnapshot);
        r.setReceiverNameSnapshot(receiverNameSnapshot);
        r.setReceiverPhoneSnapshot(receiverPhoneSnapshot);
        r.setReturnConditionNote(trimToNull(req.getReturnConditionNote()));
        r.setReturnReportPrintOptIn(req.getReturnReportPrintOptIn() != null ? req.getReturnReportPrintOptIn() : false);

        DeviceReturn saved = deviceReturnRepository.save(r);

        // Clone flow booking sân: client tạo biên bản trả => đồng bộ trạng thái dòng mượn/trả theo từng thiết bị.
        // Lý do: admin rooms cần tab quản lý mượn/trả + nhật ký + thống kê theo dòng giống booking sân.
        roomBookingDeviceService.createBorrowLinesForAssetUsageIfNeeded(usage);
        List<RoomBookingDevice> lines = roomBookingDeviceService.getLinesByAssetUsageId(usage.getId());
        if (lines != null) {
            int remainGood = g;
            int remainLost = l;
            int remainDamaged = d;
            for (RoomBookingDevice line : lines) {
                if (line == null) {
                    // Bỏ qua dòng không hợp lệ để tránh crash (đảm bảo transaction).
                    continue;
                }

                // Tạo payload cập nhật trạng thái theo preset rooms hiện tại (toàn bộ thiết bị cùng chung deviceStatus).
                ReqUpdateBookingEquipmentStatusDTO perLine = new ReqUpdateBookingEquipmentStatusDTO();
                perLine.setReturnConditionNote(trimToNull(req.getReturnConditionNote()));
                perLine.setReturnerName(returnerNameSnapshot);
                perLine.setReturnerPhone(returnerPhoneSnapshot);
                perLine.setReceiverName(receiverNameSnapshot);
                perLine.setReceiverPhone(receiverPhoneSnapshot);
                perLine.setReturnReportPrintOptIn(req.getReturnReportPrintOptIn() != null ? req.getReturnReportPrintOptIn() : false);
                perLine.setBorrowerSignName(hasDrop ? borrowerSignName : null);
                perLine.setStaffSignName(hasDrop ? staffSignName : null);

                int qLine = line.getQuantity() != null ? line.getQuantity() : 0;
                int lineGood = Math.min(remainGood, qLine);
                int left = qLine - lineGood;
                int lineLost = Math.min(remainLost, left);
                left -= lineLost;
                int lineDamaged = Math.min(remainDamaged, left);
                left -= lineDamaged;
                // Safety: nếu còn dư do làm tròn/edge case, dồn về trả tốt để luôn khớp tổng dòng.
                if (left > 0) {
                    lineGood += left;
                }

                remainGood -= lineGood;
                remainLost -= lineLost;
                remainDamaged -= lineDamaged;

                perLine.setStatus(BookingEquipmentStatusEnum.RETURNED);
                perLine.setQuantityReturnedGood(lineGood);
                perLine.setQuantityLost(lineLost);
                perLine.setQuantityDamaged(lineDamaged);

                // Chỉ cập nhật khi dòng đang BORROWED (đúng pattern booking sân).
                if (line.getStatus() == BookingEquipmentStatusEnum.BORROWED) {
                    try {
                        roomBookingDeviceService.updateStatusByClient(line.getId(), perLine);
                    } catch (IdInvalidException ex) {
                        // Không để lộ checked-exception ra method hiện tại; convert về lỗi business dễ hiểu.
                        throw new BadRequestException("Không tìm thấy dòng thiết bị mượn theo booking phòng");
                    }
                }
            }
        }

        usage.setStatus(AssetUsageStatus.COMPLETED);
        assetUsageRepository.save(usage);

        return convertToResCreateDeviceReturnDTO(saved);
    }

    @Transactional
    public ResCreateDeviceReturnDTO createDeviceReturnForCurrentUser(
            @NonNull Long assetUsageId,
            @NonNull ReqCreateClientReturnDTO clientReq,
            @NonNull String email) {
        var user = userService.handleGetUserByUsername(email);
        Checkout checkout = checkoutRepository.findByAssetUsageId(assetUsageId)
                .orElseThrow(() -> new BadRequestException("Đăng ký phòng chưa có biên bản nhận tài sản"));
        AssetUsage usage = checkout.getAssetUsage();
        if (usage == null || usage.getUser() == null || !usage.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền tạo biên bản trả cho đăng ký này");
        }
        ReqCreateDeviceReturnDTO deviceReq = new ReqCreateDeviceReturnDTO();
        deviceReq.setCheckoutId(checkout.getId());
        deviceReq.setReturnTime(clientReq.getReturnTime());
        deviceReq.setDeviceStatus(clientReq.getDeviceStatus());
        deviceReq.setQuantityReturnedGood(this.resolveNullable(clientReq.getQuantityReturnedGood()));
        deviceReq.setQuantityLost(this.resolveNullable(clientReq.getQuantityLost()));
        deviceReq.setQuantityDamaged(this.resolveNullable(clientReq.getQuantityDamaged()));
        deviceReq.setBorrowerSignName(clientReq.getBorrowerSignName());
        deviceReq.setStaffSignName(clientReq.getStaffSignName());
        deviceReq.setReturnerName(clientReq.getReturnerName());
        deviceReq.setReturnerPhone(clientReq.getReturnerPhone());
        deviceReq.setReceiverName(clientReq.getReceiverName());
        deviceReq.setReceiverPhone(clientReq.getReceiverPhone());
        deviceReq.setReturnConditionNote(clientReq.getReturnConditionNote());
        deviceReq.setReturnReportPrintOptIn(clientReq.getReturnReportPrintOptIn());
        return createDeviceReturn(deviceReq);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllDeviceReturns(Specification<DeviceReturn> spec, @NonNull Pageable pageable) {
        Page<DeviceReturn> page = deviceReturnRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResDeviceReturnListDTO> list = new ArrayList<>();
        for (DeviceReturn r : page.getContent()) {
            list.add(convertToResDeviceReturnListDTO(r));
        }
        rs.setResult(list);
        return rs;
    }

    public DeviceReturn getDeviceReturnById(@NonNull Long id) throws IdInvalidException {
        Optional<DeviceReturn> opt = deviceReturnRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy DeviceReturn với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResDeviceReturnDetailDTO getDeviceReturnDetailById(@NonNull Long id) throws IdInvalidException {
        return convertToResDeviceReturnDetailDTO(getDeviceReturnById(id));
    }

    @Transactional(readOnly = true)
    public ResDeviceReturnDetailDTO getDeviceReturnDetailByAssetUsageIdForCurrentUser(
            @NonNull Long assetUsageId,
            @NonNull String email) throws IdInvalidException {
        Checkout checkout = checkoutRepository.findByAssetUsageId(assetUsageId)
                .orElseThrow(() -> new IdInvalidException("Đăng ký phòng này chưa có biên bản nhận/trả"));
        DeviceReturn deviceReturn = deviceReturnRepository.findByCheckoutId(checkout.getId())
                .orElseThrow(() -> new IdInvalidException("Biên bản trả tài sản chưa được tạo"));
        AssetUsage usage = checkout.getAssetUsage();
        if (usage == null || usage.getUser() == null || !usage.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Không có quyền xem biên bản trả tài sản này");
        }
        return convertToResDeviceReturnDetailDTO(deviceReturn);
    }

    @Transactional
    public ResUpdateDeviceReturnDTO updateDeviceReturn(@NonNull Long id, @NonNull ReqUpdateDeviceReturnDTO req)
            throws IdInvalidException {
        DeviceReturn r = getDeviceReturnById(id);
        r.setReturnTime(req.getReturnTime());
        r.setDeviceStatus(req.getDeviceStatus());

        // Chỉ cập nhật những field được gửi lên; các field còn lại giữ nguyên để tránh làm mất dữ liệu biên bản.
        if (req.getQuantityReturnedGood() != null) {
            r.setQuantityReturnedGood(req.getQuantityReturnedGood());
        }
        if (req.getQuantityLost() != null) {
            r.setQuantityLost(req.getQuantityLost());
        }
        if (req.getQuantityDamaged() != null) {
            r.setQuantityDamaged(req.getQuantityDamaged());
        }
        if (req.getBorrowerSignName() != null) {
            r.setBorrowerSignName(trimToNull(req.getBorrowerSignName()));
        }
        if (req.getStaffSignName() != null) {
            r.setStaffSignName(trimToNull(req.getStaffSignName()));
        }
        if (req.getReturnerName() != null) {
            r.setReturnerNameSnapshot(trimToNull(req.getReturnerName()));
        }
        if (req.getReturnerPhone() != null) {
            r.setReturnerPhoneSnapshot(trimToNull(req.getReturnerPhone()));
        }
        if (req.getReceiverName() != null) {
            r.setReceiverNameSnapshot(trimToNull(req.getReceiverName()));
        }
        if (req.getReceiverPhone() != null) {
            r.setReceiverPhoneSnapshot(trimToNull(req.getReceiverPhone()));
        }
        if (req.getReturnConditionNote() != null) {
            r.setReturnConditionNote(trimToNull(req.getReturnConditionNote()));
        }
        if (req.getReturnReportPrintOptIn() != null) {
            r.setReturnReportPrintOptIn(req.getReturnReportPrintOptIn());
        }

        DeviceReturn saved = deviceReturnRepository.save(r);
        return convertToResUpdateDeviceReturnDTO(saved);
    }

    @Transactional
    public void deleteDeviceReturn(@NonNull Long id) throws IdInvalidException {
        DeviceReturn r = getDeviceReturnById(id);
        Checkout checkout = r.getCheckout();
        if (checkout != null) {
            AssetUsage usage = checkout.getAssetUsage();
            if (usage != null && usage.getStatus() == AssetUsageStatus.COMPLETED) {
                usage.setStatus(AssetUsageStatus.IN_PROGRESS);
                assetUsageRepository.save(usage);
            }
        }
        deviceReturnRepository.delete(r);
    }

    private ResDeviceReturnListDTO convertToResDeviceReturnListDTO(DeviceReturn r) {
        ResDeviceReturnListDTO res = new ResDeviceReturnListDTO();
        res.setId(r.getId());
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setCreatedBy(r.getCreatedBy());
        res.setUpdatedBy(r.getUpdatedBy());
        Checkout c = r.getCheckout();
        if (c != null) {
            res.setCheckoutId(c.getId());
            res.setReceiveTime(c.getReceiveTime());
            AssetUsage u = c.getAssetUsage();
            if (u != null) {
                res.setAssetUsageId(u.getId());
                res.setUsageType(u.getUsageType());
                res.setUsageDate(u.getUsageDate());
                res.setStartTime(u.getStartTime());
                res.setEndTime(u.getEndTime());
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
        }
        return res;
    }

    private ResDeviceReturnDetailDTO convertToResDeviceReturnDetailDTO(DeviceReturn r) {
        ResDeviceReturnDetailDTO res = new ResDeviceReturnDetailDTO();
        res.setId(r.getId());
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setQuantityReturnedGood(r.getQuantityReturnedGood());
        res.setQuantityLost(r.getQuantityLost());
        res.setQuantityDamaged(r.getQuantityDamaged());
        res.setBorrowerSignName(r.getBorrowerSignName());
        res.setStaffSignName(r.getStaffSignName());
        res.setReturnerNameSnapshot(r.getReturnerNameSnapshot());
        res.setReturnerPhoneSnapshot(r.getReturnerPhoneSnapshot());
        res.setReceiverNameSnapshot(r.getReceiverNameSnapshot());
        res.setReceiverPhoneSnapshot(r.getReceiverPhoneSnapshot());
        res.setReturnConditionNote(r.getReturnConditionNote());
        res.setReturnReportPrintOptIn(r.isReturnReportPrintOptIn());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setCreatedBy(r.getCreatedBy());
        res.setUpdatedBy(r.getUpdatedBy());
        Checkout c = r.getCheckout();
        if (c != null) {
            res.setCheckoutId(c.getId());
            res.setReceiveTime(c.getReceiveTime());
            res.setCheckoutConditionNote(c.getConditionNote());
            AssetUsage u = c.getAssetUsage();
            if (u != null) {
                res.setAssetUsageId(u.getId());
                res.setSubject(u.getSubject());
                res.setUsageType(u.getUsageType());
                res.setUsageDate(u.getUsageDate());
                res.setStartTime(u.getStartTime());
                res.setEndTime(u.getEndTime());
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
        }
        return res;
    }

    private ResCreateDeviceReturnDTO convertToResCreateDeviceReturnDTO(DeviceReturn r) {
        ResCreateDeviceReturnDTO res = new ResCreateDeviceReturnDTO();
        res.setId(r.getId());
        res.setCheckoutId(r.getCheckout() != null ? r.getCheckout().getId() : null);
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setQuantityReturnedGood(r.getQuantityReturnedGood());
        res.setQuantityLost(r.getQuantityLost());
        res.setQuantityDamaged(r.getQuantityDamaged());
        res.setBorrowerSignName(r.getBorrowerSignName());
        res.setStaffSignName(r.getStaffSignName());
        res.setReturnerNameSnapshot(r.getReturnerNameSnapshot());
        res.setReturnerPhoneSnapshot(r.getReturnerPhoneSnapshot());
        res.setReceiverNameSnapshot(r.getReceiverNameSnapshot());
        res.setReceiverPhoneSnapshot(r.getReceiverPhoneSnapshot());
        res.setReturnConditionNote(r.getReturnConditionNote());
        res.setReturnReportPrintOptIn(r.isReturnReportPrintOptIn());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }

    private ResUpdateDeviceReturnDTO convertToResUpdateDeviceReturnDTO(DeviceReturn r) {
        ResUpdateDeviceReturnDTO res = new ResUpdateDeviceReturnDTO();
        res.setId(r.getId());
        res.setCheckoutId(r.getCheckout() != null ? r.getCheckout().getId() : null);
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setQuantityReturnedGood(r.getQuantityReturnedGood());
        res.setQuantityLost(r.getQuantityLost());
        res.setQuantityDamaged(r.getQuantityDamaged());
        res.setBorrowerSignName(r.getBorrowerSignName());
        res.setStaffSignName(r.getStaffSignName());
        res.setReturnerNameSnapshot(r.getReturnerNameSnapshot());
        res.setReturnerPhoneSnapshot(r.getReturnerPhoneSnapshot());
        res.setReceiverNameSnapshot(r.getReceiverNameSnapshot());
        res.setReceiverPhoneSnapshot(r.getReceiverPhoneSnapshot());
        res.setReturnConditionNote(r.getReturnConditionNote());
        res.setReturnReportPrintOptIn(r.isReturnReportPrintOptIn());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setUpdatedBy(r.getUpdatedBy());
        return res;
    }

    /**
     * Resolve tổng số lượng để validate trả tốt / mất / hỏng.
     * Nếu borrowDevicesJson có mảng devices thì cộng theo field quantity.
     * Nếu rỗng => coi là 1 (phòng/tài sản).
     */
    private int resolveReturnTotalQuantity(@NonNull AssetUsage usage) {
        String json = usage.getBorrowDevicesJson();
        if (json == null || json.trim().isEmpty()) {
            return 1;
        }
        try {
            JsonNode root = objectMapper.readTree(json.trim());
            if (!root.isArray()) {
                return 1;
            }
            int sum = 0;
            for (JsonNode node : root) {
                JsonNode q = node.get("quantity");
                if (q == null || q.isNull()) continue;
                // số lượng có thể gửi dưới dạng number/string; ở đây ép về int an toàn.
                int val = q.isNumber() ? q.asInt(0) : parseIntSafe(q.asText());
                if (val > 0) sum += val;
            }
            return sum > 0 ? sum : 1;
        } catch (Exception ex) {
            // Nếu JSON sai định dạng => fallback 1 để không chặn luồng tạo return.
            return 1;
        }
    }

    private static int parseIntSafe(String t) {
        if (t == null || t.isBlank()) return 0;
        try {
            return Integer.parseInt(t.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String userDisplayName(User u) {
        if (u == null) return null;
        if (u.getFullName() != null && !u.getFullName().isBlank()) {
            return u.getFullName().trim();
        }
        if (u.getName() != null && !u.getName().isBlank()) {
            return u.getName().trim();
        }
        return u.getEmail();
    }

    private static String safePhone(String contactPhone, User u) {
        String phone = trimToNull(contactPhone);
        if (phone != null) return phone;
        return u != null ? trimToNull(u.getPhoneNumber()) : null;
    }

    private Integer resolveNullable(Integer v) {
        // Chỉ trả về giá trị (có thể null) để tránh shadow variable trong mapper.
        return v;
    }
}
