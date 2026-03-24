package com.example.backend.service;

import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.entity.Device;
import com.example.backend.domain.entity.RoomBookingDevice;
import com.example.backend.domain.entity.RoomBookingDeviceBorrowLog;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.domain.response.equipment.ResEquipmentBorrowLogDTO;
import com.example.backend.domain.response.equipment.ResEquipmentUsageRowDTO;
import com.example.backend.domain.response.equipment.ResEquipmentUsageStatsDTO;
import com.example.backend.repository.DeviceRepository;
import com.example.backend.repository.RoomBookingDeviceBorrowLogRepository;
import com.example.backend.repository.RoomBookingDeviceRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import com.example.backend.domain.entity.User;

/**
 * Service dòng mượn/trả theo booking phòng (rooms) — clone theo BookingEquipmentService.
 */
@Service
public class RoomBookingDeviceService {

    private final RoomBookingDeviceRepository roomBookingDeviceRepository;
    private final RoomBookingDeviceBorrowLogRepository roomBookingDeviceBorrowLogRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationService notificationService;

    /** ObjectMapper parse borrowDevicesJson của AssetUsage. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoomBookingDeviceService(
            RoomBookingDeviceRepository roomBookingDeviceRepository,
            RoomBookingDeviceBorrowLogRepository roomBookingDeviceBorrowLogRepository,
            DeviceRepository deviceRepository,
            NotificationService notificationService) {
        this.roomBookingDeviceRepository = roomBookingDeviceRepository;
        this.roomBookingDeviceBorrowLogRepository = roomBookingDeviceBorrowLogRepository;
        this.deviceRepository = deviceRepository;
        this.notificationService = notificationService;
    }

    /**
     * Lấy danh sách dòng mượn/trả theo booking phòng (AssetUsage).
     *
     * <p>
     * Dùng khi client tạo biên bản trả: cần đồng bộ trạng thái RETURNED/LOST/DAMAGED cho từng dòng.
     * </p>
     */
    @Transactional(readOnly = true)
    public List<RoomBookingDevice> getLinesByAssetUsageId(@NonNull Long assetUsageId) {
        List<RoomBookingDevice> list = roomBookingDeviceRepository.findByAssetUsage_Id(assetUsageId);
        return list != null ? list : List.of();
    }

    /**
     * Tạo dòng mượn theo booking khi client tạo checkout (nhận phòng).
     * <p>
     * - Mỗi dòng tương ứng 1 thiết bị trong {@code AssetUsage.borrowDevicesJson}.
     * - Trừ số lượng của Device để tránh mượn vượt kho (clone logic availability).
     * - Tạo nhật ký loại BORROW cho từng dòng.
     * </p>
     */
    @Transactional
    public void createBorrowLinesForAssetUsageIfNeeded(@NonNull AssetUsage usage) {
        // Chỉ tạo nếu usage có thiết bị mượn lưu trong borrowDevicesJson.
        if (usage.getBorrowDevicesJson() == null || usage.getBorrowDevicesJson().trim().isEmpty()) {
            return;
        }

        // Tránh tạo trùng: nếu đã có dòng cho booking thì bỏ qua.
        List<RoomBookingDevice> existing = roomBookingDeviceRepository.findByAssetUsage_Id(usage.getId());
        if (existing != null && !existing.isEmpty()) {
            return;
        }

        List<JsonNode> nodes = parseBorrowDevicesJson(usage.getBorrowDevicesJson());
        if (nodes.isEmpty()) {
            return;
        }

        User u = usage.getUser();
        String bookingBorrowerSnapshot = userDisplayName(u);
        String bookingPhone = usage.getContactPhone() != null ? usage.getContactPhone() : safePhone(u);
        String commonBorrowNote = trimToNull(usage.getBorrowNote());

        for (JsonNode n : nodes) {
            Long deviceId = n.get("deviceId") != null ? n.get("deviceId").asLong() : null;
            int qty = n.get("quantity") != null ? n.get("quantity").asInt(0) : 0;
            String deviceNote = trimToNull(n.get("deviceNote") != null ? n.get("deviceNote").asText() : null);

            // Validate đầu vào cơ bản để không tạo dòng rác.
            if (deviceId == null || deviceId <= 0 || qty <= 0) {
                continue;
            }

            Device d = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy Device với ID = " + deviceId));

            // Chỉ cho mượn khi thiết bị đang ở trạng thái AVAILABLE (giữ logic giống v1).
            if (d.getStatus() != com.example.backend.util.constant.device.DeviceStatus.AVAILABLE) {
                throw new BadRequestException("Thiết bị không ở trạng thái sẵn sàng để mượn (ID = " + deviceId + ")");
            }

            // Validate số lượng không vượt quá số đang tồn.
            if (d.getQuantity() < qty) {
                throw new BadRequestException("Số lượng mượn vượt tồn kho cho thiết bị ID = " + deviceId);
            }

            // Ghép ghi chú mượn: dùng note chung nếu có, sau đó thêm ghi chú riêng từng thiết bị.
            String borrowConditionNote = mergeNotes(commonBorrowNote, deviceNote);

            RoomBookingDevice line = new RoomBookingDevice();
            line.setAssetUsage(usage);
            line.setDevice(d);
            line.setQuantity(qty);
            line.setStatus(BookingEquipmentStatusEnum.BORROWED);
            line.setEquipmentMobility(mapMobility(d));
            line.setBorrowConditionNote(borrowConditionNote);
            line.setBorrowConditionAcknowledged(usage.isBorrowConditionAcknowledged());
            line.setBorrowReportPrintOptIn(usage.isBorrowReportPrintOptIn());
            line.setBookingBorrowerSnapshot(bookingBorrowerSnapshot);
            line.setEquipmentPrice(0L);
            line.setPenaltyAmount(0L);

            roomBookingDeviceRepository.save(line);

            // Trừ tồn kho theo số lượng mượn.
            d.setQuantity(d.getQuantity() - qty);
            // Cập nhật status theo tồn kho để FE biết thiết bị còn mượn được hay không.
            if (d.getQuantity() <= 0) {
                d.setStatus(com.example.backend.util.constant.device.DeviceStatus.BORROWED);
            } else {
                d.setStatus(com.example.backend.util.constant.device.DeviceStatus.AVAILABLE);
            }
            deviceRepository.save(d);

            // Tạo nhật ký mượn cho từng dòng (clone từ appendLog BORROW).
            RoomBookingDeviceBorrowLog log = new RoomBookingDeviceBorrowLog();
            log.setRoomBookingDevice(line);
            log.setLogType(EquipmentBorrowLogTypeEnum.BORROW);
            log.setNotes(borrowConditionNote);
            log.setActorName(bookingBorrowerSnapshot);
            log.setActorPhone(bookingPhone);
            roomBookingDeviceBorrowLogRepository.save(log);
        }
    }

    /**
     * Admin cập nhật trạng thái dòng (RETURNED / LOST / DAMAGED) — clone từ updateBookingEquipmentStatusByAdmin.
     */
    @Transactional
    public ResBookingEquipmentDTO updateStatusByAdmin(@NonNull Long id, @NonNull ReqUpdateBookingEquipmentStatusDTO req) throws IdInvalidException {
        return updateStatusInternal(id, req, true, false, false);
    }

    /**
     * Client cập nhật trạng thái dòng (được gọi từ DeviceReturnService) — clone từ updateStatusByClient.
     */
    @Transactional
    public ResBookingEquipmentDTO updateStatusByClient(@NonNull Long id, @NonNull ReqUpdateBookingEquipmentStatusDTO req) throws IdInvalidException {
        return updateStatusInternal(id, req, false, true, true);
    }

    private ResBookingEquipmentDTO updateStatusInternal(
            @NonNull Long id,
            @NonNull ReqUpdateBookingEquipmentStatusDTO req,
            boolean notifyClient,
            boolean notifyAdmins,
            boolean submittedByClient) throws IdInvalidException {

        RoomBookingDevice line = getById(id);

        // Clone rule: chỉ cho phép cập nhật trạng thái khi đang BORROWED.
        if (line.getStatus() != BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Chỉ có thể cập nhật trạng thái khi thiết bị đang được mượn");
        }

        if (submittedByClient) {
            // Clone rule: khi client submit trả, bắt buộc có receiverName/receiverPhone.
            if (!org.springframework.util.StringUtils.hasText(req.getReceiverName()) || !org.springframework.util.StringUtils.hasText(req.getReceiverPhone())) {
                throw new BadRequestException(
                        "Vui lòng nhập đầy đủ họ tên và số điện thoại người nhận thiết bị tại sân (bên giao nhận).");
            }
        }

        Device device = line.getDevice();

        boolean detailed = req.getQuantityReturnedGood() != null
                || req.getQuantityLost() != null
                || req.getQuantityDamaged() != null;

        BookingEquipmentStatusEnum newStatus;
        if (detailed) {
            newStatus = applyReturnBreakdown(line, device, req);
        } else {
            newStatus = applyLegacyStatusChange(line, device, req.getStatus(), req);
        }

        // Clone rule: submittedByClient quyết định trạng thái xác nhận admin.
        if (submittedByClient) {
            line.setReturnAdminConfirmed(false);
            line.setReturnAdminConfirmedAt(null);
            line.setReturnAdminConfirmedBy(null);
        } else {
            line.setReturnAdminConfirmed(true);
            line.setReturnAdminConfirmedAt(Instant.now());
            line.setReturnAdminConfirmedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        }

        roomBookingDeviceRepository.save(line);
        deviceRepository.save(device);

        // Tạo nhật ký RETURN + thông báo (clone logic BookingEquipmentService).
        String returnNote = req.getReturnConditionNote();
        String breakdown = String.format("Trả tốt: %d — Mất: %d — Hỏng: %d",
                line.getQuantityReturnedGood(), line.getQuantityLost(), line.getQuantityDamaged());

        String returnLogNote = (returnNote != null && !returnNote.isBlank() ? returnNote + " — " : "")
                + breakdown + " — Trạng thái: " + mapStatusToVietnamese(newStatus);

        appendReturnLog(line, returnLogNote);

        NotificationTypeEnum notifType = mapStatusToNotificationType(newStatus);
        String statusVi = mapStatusToVietnamese(newStatus);

        String actorMsg = String.format("Thiết bị %s (%dx) của RoomBooking #%d đã được cập nhật trạng thái: %s.",
                device.getDeviceName(), line.getQuantity(), line.getAssetUsage().getId(), statusVi);

        if (notifyAdmins) {
            // Clone UI behavior: client submit sẽ thông báo admin.
            notificationService.notifyAdmins(notifType, actorMsg);
        }
        if (notifyClient) {
            // Admin submit sẽ đẩy thông báo ngược lại cho người đặt.
            notificationService.createAndPush(line.getAssetUsage().getUser(), notifType, actorMsg);
        }

        return convertToResDTO(line);
    }

    /**
     * Admin xác nhận biên bản trả cho dòng thiết bị.
     */
    @Transactional
    public ResBookingEquipmentDTO confirmReturnByAdmin(@NonNull Long id) throws IdInvalidException {
        RoomBookingDevice line = getById(id);
        if (line.getStatus() == BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Thiết bị chưa có thông tin trả.");
        }
        if (line.isReturnAdminConfirmed()) {
            throw new BadRequestException("Biên bản trả đã được xác nhận");
        }
        line.setReturnAdminConfirmed(true);
        line.setReturnAdminConfirmedAt(Instant.now());
        line.setReturnAdminConfirmedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        roomBookingDeviceRepository.save(line);
        return convertToResDTO(line);
    }

    /** Lấy danh sách dòng thiết bị theo rooms (admin). */
    @Transactional(readOnly = true)
    public List<ResBookingEquipmentDTO> getAll() {
        List<RoomBookingDevice> all = roomBookingDeviceRepository.findAll();
        List<ResBookingEquipmentDTO> res = new ArrayList<>();
        for (RoomBookingDevice r : all) {
            res.add(convertToResDTO(r));
        }
        return res;
    }

    /** Lấy nhật ký mượn/trả gần nhất (tab log). */
    @Transactional(readOnly = true)
    public List<ResEquipmentBorrowLogDTO> getRecentBorrowLogs() {
        List<RoomBookingDeviceBorrowLog> list = roomBookingDeviceBorrowLogRepository.findTop200ByOrderByCreatedAtDesc();
        List<ResEquipmentBorrowLogDTO> res = new ArrayList<>();
        for (RoomBookingDeviceBorrowLog l : list) {
            res.add(toBorrowLogDto(l));
        }
        return res;
    }

    /** Lấy thống kê sử dụng (tab stats) theo thiết bị và theo phòng. */
    @Transactional(readOnly = true)
    public ResEquipmentUsageStatsDTO getUsageStats() {
        List<Object[]> byDevice = roomBookingDeviceRepository.aggregateBorrowCountByDevice();
        List<Object[]> byRoom = roomBookingDeviceRepository.aggregateBorrowCountByRoom();

        List<ResEquipmentUsageRowDTO> eqRows = new ArrayList<>();
        for (Object[] row : byDevice) {
            eqRows.add(new ResEquipmentUsageRowDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()));
        }
        List<ResEquipmentUsageRowDTO> roomRows = new ArrayList<>();
        for (Object[] row : byRoom) {
            roomRows.add(new ResEquipmentUsageRowDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()));
        }

        return new ResEquipmentUsageStatsDTO(eqRows, roomRows);
    }

    private void appendReturnLog(RoomBookingDevice line, String returnLogNote) {
        RoomBookingDeviceBorrowLog log = new RoomBookingDeviceBorrowLog();
        log.setRoomBookingDevice(line);
        log.setLogType(EquipmentBorrowLogTypeEnum.RETURN);
        log.setNotes(returnLogNote);
        log.setActorName(line.getReturnerNameSnapshot());
        log.setActorPhone(line.getReturnerPhoneSnapshot());
        roomBookingDeviceBorrowLogRepository.save(log);
    }

    private RoomBookingDevice getById(@NonNull Long id) throws IdInvalidException {
        Optional<RoomBookingDevice> opt = roomBookingDeviceRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy RoomBookingDevice với ID = " + id);
    }

    private ResBookingEquipmentDTO convertToResDTO(RoomBookingDevice line) {
        ResBookingEquipmentDTO res = new ResBookingEquipmentDTO();
        res.setId(line.getId());
        res.setBookingId(line.getAssetUsage() != null ? line.getAssetUsage().getId() : null);
        res.setEquipmentId(line.getDevice() != null ? line.getDevice().getId() : null);
        res.setEquipmentName(line.getDevice() != null ? line.getDevice().getDeviceName() : null);
        res.setEquipmentImageUrl(line.getDevice() != null ? line.getDevice().getImageUrl() : null);
        res.setQuantity(line.getQuantity());
        res.setStatus(line.getStatus());
        res.setPenaltyAmount(line.getPenaltyAmount() != null ? line.getPenaltyAmount() : 0L);
        res.setEquipmentPrice(line.getEquipmentPrice() != null ? line.getEquipmentPrice() : 0L);
        res.setDeletedByClient(line.isDeletedByClient());
        res.setEquipmentMobility(line.getEquipmentMobility());
        res.setBorrowConditionNote(line.getBorrowConditionNote());
        res.setReturnConditionNote(line.getReturnConditionNote());
        res.setQuantityReturnedGood(line.getQuantityReturnedGood() != null ? line.getQuantityReturnedGood() : 0);
        res.setQuantityLost(line.getQuantityLost() != null ? line.getQuantityLost() : 0);
        res.setQuantityDamaged(line.getQuantityDamaged() != null ? line.getQuantityDamaged() : 0);
        res.setBorrowerSignName(line.getBorrowerSignName());
        res.setStaffSignName(line.getStaffSignName());
        res.setBookingBorrowerSnapshot(line.getBookingBorrowerSnapshot());
        res.setBorrowConditionAcknowledged(line.isBorrowConditionAcknowledged());
        res.setBorrowReportPrintOptIn(line.isBorrowReportPrintOptIn());
        res.setReturnerNameSnapshot(line.getReturnerNameSnapshot());
        res.setReturnerPhoneSnapshot(line.getReturnerPhoneSnapshot());
        res.setReturnReportPrintOptIn(line.getReturnReportPrintOptIn());
        res.setReceiverNameSnapshot(line.getReceiverNameSnapshot());
        res.setReceiverPhoneSnapshot(line.getReceiverPhoneSnapshot());
        res.setReturnAdminConfirmed(line.isReturnAdminConfirmed());
        res.setReturnAdminConfirmedAt(line.getReturnAdminConfirmedAt());
        res.setReturnAdminConfirmedBy(line.getReturnAdminConfirmedBy());
        return res;
    }

    private ResEquipmentBorrowLogDTO toBorrowLogDto(RoomBookingDeviceBorrowLog l) {
        RoomBookingDevice line = l.getRoomBookingDevice();
        AssetUsage usage = line.getAssetUsage();
        Device d = line.getDevice();
        User u = usage.getUser();

        ResEquipmentBorrowLogDTO dto = new ResEquipmentBorrowLogDTO();
        dto.setId(l.getId());
        dto.setBookingEquipmentId(line.getId());
        dto.setBookingId(usage.getId());
        dto.setEquipmentId(d.getId());
        dto.setEquipmentName(d.getDeviceName());
        dto.setLogType(l.getLogType());
        dto.setNotes(l.getNotes());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setCreatedBy(l.getCreatedBy());

        dto.setBookingUserName(userDisplayName(u));
        dto.setBookingUserPhone(usage.getContactPhone());
        dto.setPitchName(usage.getAsset() != null ? usage.getAsset().getAssetName() : null);

        dto.setActorName(l.getActorName() != null ? l.getActorName() : userDisplayName(u));
        dto.setActorPhone(l.getActorPhone() != null ? l.getActorPhone() : safePhone(u));

        dto.setBorrowConditionAcknowledged(line.isBorrowConditionAcknowledged());
        dto.setBorrowReportPrintOptIn(line.isBorrowReportPrintOptIn());
        dto.setReturnerNameSnapshot(line.getReturnerNameSnapshot());
        dto.setReturnerPhoneSnapshot(line.getReturnerPhoneSnapshot());
        dto.setReturnReportPrintOptIn(line.getReturnReportPrintOptIn());
        dto.setReceiverNameSnapshot(line.getReceiverNameSnapshot());
        dto.setReceiverPhoneSnapshot(line.getReceiverPhoneSnapshot());
        dto.setReturnAdminConfirmed(line.isReturnAdminConfirmed());
        return dto;
    }

    private BookingEquipmentStatusEnum applyReturnBreakdown(
            RoomBookingDevice line,
            Device device,
            ReqUpdateBookingEquipmentStatusDTO req) {
        int g = Optional.ofNullable(req.getQuantityReturnedGood()).orElse(0);
        int l = Optional.ofNullable(req.getQuantityLost()).orElse(0);
        int d = Optional.ofNullable(req.getQuantityDamaged()).orElse(0);
        int q = line.getQuantity() != null ? line.getQuantity() : 0;

        if (g < 0 || l < 0 || d < 0) {
            throw new BadRequestException("Số lượng không hợp lệ");
        }
        if (g + l + d != q) {
            throw new BadRequestException("Tổng trả tốt + mất + hỏng phải bằng số lượng mượn");
        }

        // Clone rule: nếu có mất hoặc hỏng thì bắt buộc ký xác nhận.
        if (l + d > 0) {
            if (!org.springframework.util.StringUtils.hasText(req.getBorrowerSignName()) || !org.springframework.util.StringUtils.hasText(req.getStaffSignName())) {
                throw new BadRequestException(
                        "Vui lòng ghi họ tên người mượn và nhân viên ký xác nhận khi có mất hoặc hỏng.");
            }
        }

        // Theo rooms: Device.quantity đã bị trừ khi mượn, nên chỉ cần cộng lại phần trả tốt (g).
        device.setQuantity(device.getQuantity() + g);
        // Cập nhật status thiết bị sau khi trả tốt để FE có thể cho mượn lại.
        if (device.getQuantity() > 0) {
            device.setStatus(com.example.backend.util.constant.device.DeviceStatus.AVAILABLE);
        } else {
            device.setStatus(com.example.backend.util.constant.device.DeviceStatus.BORROWED);
        }

        line.setPenaltyAmount((long) l * (line.getEquipmentPrice() != null ? line.getEquipmentPrice() : 0L));

        line.setQuantityReturnedGood(g);
        line.setQuantityLost(l);
        line.setQuantityDamaged(d);
        line.setReturnConditionNote(req.getReturnConditionNote());
        line.setBorrowerSignName(trimToNull(req.getBorrowerSignName()));
        line.setStaffSignName(trimToNull(req.getStaffSignName()));
        line.setBookingBorrowerSnapshot(userDisplayName(line.getAssetUsage().getUser()));

        BookingEquipmentStatusEnum derived;
        if (l == q) {
            derived = BookingEquipmentStatusEnum.LOST;
        } else if (d == q && g == 0) {
            derived = BookingEquipmentStatusEnum.DAMAGED;
        } else {
            derived = BookingEquipmentStatusEnum.RETURNED;
        }

        applyReturnerSnapshots(line, req);
        line.setStatus(derived);
        return derived;
    }

    private BookingEquipmentStatusEnum applyLegacyStatusChange(
            RoomBookingDevice line,
            Device device,
            BookingEquipmentStatusEnum newStatus,
            ReqUpdateBookingEquipmentStatusDTO req) {
        int q = line.getQuantity() != null ? line.getQuantity() : 0;

        if (newStatus == BookingEquipmentStatusEnum.RETURNED) {
            device.setQuantity(device.getQuantity() + q);
            // Cập nhật status thiết bị theo tồn kho sau trả bình thường.
            if (device.getQuantity() > 0) {
                device.setStatus(com.example.backend.util.constant.device.DeviceStatus.AVAILABLE);
            } else {
                device.setStatus(com.example.backend.util.constant.device.DeviceStatus.BORROWED);
            }
            line.setQuantityReturnedGood(q);
            line.setQuantityLost(0);
            line.setQuantityDamaged(0);
            line.setPenaltyAmount(0L);
        } else if (newStatus == BookingEquipmentStatusEnum.LOST) {
            // LOST: không trả tốt nên không cộng lại tồn kho.
            line.setPenaltyAmount((long) q * (line.getEquipmentPrice() != null ? line.getEquipmentPrice() : 0L));
            line.setQuantityReturnedGood(0);
            line.setQuantityLost(q);
            line.setQuantityDamaged(0);
        } else if (newStatus == BookingEquipmentStatusEnum.DAMAGED) {
            // DAMAGED: không trả tốt nên không cộng lại tồn kho.
            line.setPenaltyAmount(0L);
            line.setQuantityReturnedGood(0);
            line.setQuantityLost(0);
            line.setQuantityDamaged(q);
        } else {
            throw new BadRequestException("Trạng thái không hợp lệ khi hoàn trả");
        }

        line.setStatus(newStatus);
        line.setReturnConditionNote(req.getReturnConditionNote());
        line.setBorrowerSignName(null);
        line.setStaffSignName(null);
        line.setBookingBorrowerSnapshot(userDisplayName(line.getAssetUsage().getUser()));
        applyReturnerSnapshots(line, req);
        return newStatus;
    }

    private void applyReturnerSnapshots(RoomBookingDevice line, ReqUpdateBookingEquipmentStatusDTO req) {
        User u = line.getAssetUsage().getUser();

        String rn = trimToNull(req.getReturnerName());
        String rp = trimToNull(req.getReturnerPhone());
        line.setReturnerNameSnapshot(rn != null ? rn : userDisplayName(u));
        line.setReturnerPhoneSnapshot(rp != null ? rp : safePhone(u));

        line.setReturnReportPrintOptIn(req.getReturnReportPrintOptIn());
        line.setReceiverNameSnapshot(trimToNull(req.getReceiverName()));
        line.setReceiverPhoneSnapshot(trimToNull(req.getReceiverPhone()));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String mergeNotes(String common, String specific) {
        if (common == null && specific == null) return null;
        if (common != null && specific == null) return common;
        if (common == null) return specific;
        return common + " · " + specific;
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

    private static String safePhone(User u) {
        if (u == null || u.getPhoneNumber() == null) {
            return null;
        }
        String t = u.getPhoneNumber().trim();
        return t.isEmpty() ? null : t;
    }

    private static EquipmentMobilityEnum mapMobility(Device d) {
        // Dùng đúng tên enum để FE reuse print và UI.
        return d.getDeviceType() == com.example.backend.util.constant.device.DeviceType.FIXED
                ? EquipmentMobilityEnum.FIXED
                : EquipmentMobilityEnum.MOVABLE;
    }

    private List<JsonNode> parseBorrowDevicesJson(String json) {
        if (json == null || json.trim().isEmpty()) return List.of();
        try {
            JsonNode root = objectMapper.readTree(json.trim());
            if (!root.isArray()) return List.of();
            List<JsonNode> out = new ArrayList<>();
            for (JsonNode n : root) {
                out.add(n);
            }
            return out;
        } catch (Exception ex) {
            // Nếu JSON sai định dạng => không tạo dòng rác.
            return List.of();
        }
    }

    private NotificationTypeEnum mapStatusToNotificationType(BookingEquipmentStatusEnum status) {
        return switch (status) {
            case RETURNED -> NotificationTypeEnum.EQUIPMENT_RETURNED;
            case LOST -> NotificationTypeEnum.EQUIPMENT_LOST;
            case DAMAGED -> NotificationTypeEnum.EQUIPMENT_DAMAGED;
            case BORROWED -> NotificationTypeEnum.EQUIPMENT_BORROWED;
        };
    }

    private String mapStatusToVietnamese(BookingEquipmentStatusEnum status) {
        return switch (status) {
            case RETURNED -> "Đã trả";
            case LOST -> "Mất";
            case DAMAGED -> "Hỏng";
            case BORROWED -> "Đang mượn";
        };
    }
}

