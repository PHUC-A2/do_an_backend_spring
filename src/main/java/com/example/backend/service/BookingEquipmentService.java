package com.example.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.BookingEquipment;
import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.EquipmentBorrowLog;
import com.example.backend.domain.entity.PitchEquipment;
import com.example.backend.domain.request.bookingequipment.ReqCreateBookingEquipmentDTO;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.domain.response.equipment.ResEquipmentBorrowLogDTO;
import com.example.backend.domain.response.equipment.ResEquipmentUsageRowDTO;
import com.example.backend.domain.response.equipment.ResEquipmentUsageStatsDTO;
import com.example.backend.repository.BookingEquipmentRepository;
import com.example.backend.repository.EquipmentBorrowLogRepository;
import com.example.backend.repository.EquipmentRepository;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingEquipmentService {

    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final NotificationService notificationService;
    private final PitchEquipmentRepository pitchEquipmentRepository;
    private final EquipmentBorrowLogRepository equipmentBorrowLogRepository;
    private final EquipmentRepository equipmentRepository;

    public BookingEquipmentService(
            BookingEquipmentRepository bookingEquipmentRepository,
            BookingService bookingService,
            EquipmentService equipmentService,
            NotificationService notificationService,
            PitchEquipmentRepository pitchEquipmentRepository,
            EquipmentBorrowLogRepository equipmentBorrowLogRepository,
            EquipmentRepository equipmentRepository) {
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.bookingService = bookingService;
        this.equipmentService = equipmentService;
        this.notificationService = notificationService;
        this.pitchEquipmentRepository = pitchEquipmentRepository;
        this.equipmentBorrowLogRepository = equipmentBorrowLogRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public ResBookingEquipmentDTO borrowEquipmentByClient(@NonNull ReqCreateBookingEquipmentDTO req)
            throws IdInvalidException {
        return borrowEquipmentInternal(req, false, true);
    }

    @Transactional
    public ResBookingEquipmentDTO borrowEquipmentByAdmin(@NonNull ReqCreateBookingEquipmentDTO req)
            throws IdInvalidException {
        return borrowEquipmentInternal(req, true, false);
    }

    private ResBookingEquipmentDTO borrowEquipmentInternal(@NonNull ReqCreateBookingEquipmentDTO req,
            boolean notifyClient, boolean notifyAdmins)
            throws IdInvalidException {

        Booking booking = bookingService.getBookingById(req.getBookingId());
        Equipment equipment = equipmentService.getEquipmentById(req.getEquipmentId());

        if (equipment.getStatus() != EquipmentStatusEnum.ACTIVE) {
            throw new BadRequestException("Thiết bị hiện không thể cho mượn (chỉ trạng thái hoạt động tốt)");
        }

        PitchEquipment link = pitchEquipmentRepository
                .findByPitchIdAndEquipmentId(booking.getPitch().getId(), equipment.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Thiết bị chưa được gắn với sân (tài sản) của booking. Admin cần cấu hình thiết bị — sân trước."));

        EquipmentMobilityEnum configured = link.getEquipmentMobility() != null
                ? link.getEquipmentMobility()
                : EquipmentMobilityEnum.FIXED;
        if (configured != EquipmentMobilityEnum.MOVABLE) {
            throw new BadRequestException(
                    "Thiết bị cố định trên sân không thể mượn qua booking. Chỉ thiết bị được đánh dấu \"cho mượn / lưu động\" mới có luồng mượn–trả.");
        }
        if (req.getEquipmentMobility() != EquipmentMobilityEnum.MOVABLE) {
            throw new BadRequestException(
                    "Yêu cầu mượn phải dùng loại lưu động (thiết bị cho mượn).");
        }

        if (req.getQuantity() > link.getQuantity()) {
            throw new BadRequestException(
                    "Số lượng vượt quá số lượng cho phép trên sân (tối đa " + link.getQuantity() + ")");
        }

        if (equipment.getAvailableQuantity() < req.getQuantity()) {
            throw new BadRequestException(
                    "Số lượng không đủ. Hiện chỉ còn " + equipment.getAvailableQuantity() + " thiết bị");
        }

        BookingEquipment be = new BookingEquipment();
        be.setBooking(booking);
        be.setEquipment(equipment);
        be.setQuantity(req.getQuantity());
        be.setStatus(BookingEquipmentStatusEnum.BORROWED);
        be.setEquipmentMobility(req.getEquipmentMobility());
        be.setBorrowConditionNote(req.getBorrowConditionNote());

        boolean clientBorrow = notifyClient;
        if (clientBorrow && !Boolean.TRUE.equals(req.getBorrowConditionAcknowledged())) {
            throw new BadRequestException(
                    "Vui lòng xác nhận đã kiểm tra tình trạng thiết bị (biên bản mượn) trước khi mượn.");
        }
        be.setBorrowConditionAcknowledged(!clientBorrow || Boolean.TRUE.equals(req.getBorrowConditionAcknowledged()));
        be.setBorrowReportPrintOptIn(Boolean.TRUE.equals(req.getBorrowReportPrintOptIn()));

        bookingEquipmentRepository.save(be);

        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - req.getQuantity());
        equipmentRepository.save(equipment);

        User borrower = booking.getUser();
        appendLog(be, EquipmentBorrowLogTypeEnum.BORROW, req.getBorrowConditionNote(), userDisplayName(borrower),
                safePhone(borrower));

        String actorName = booking.getUser().getFullName() != null && !booking.getUser().getFullName().isBlank()
                ? booking.getUser().getFullName()
                : booking.getUser().getName();
        String msg = String.format("%s đã mượn %dx %s cho Booking #%d.",
                actorName, be.getQuantity(), equipment.getName(), booking.getId());

        if (notifyAdmins) {
            notificationService.notifyAdmins(NotificationTypeEnum.EQUIPMENT_BORROWED, msg, null, booking.getId());
        }
        if (notifyClient) {
            notificationService.createAndPush(booking.getUser(), NotificationTypeEnum.EQUIPMENT_BORROWED,
                    String.format("Bạn đã được tạo mượn %dx %s cho Booking #%d.",
                            be.getQuantity(), equipment.getName(), booking.getId()),
                    booking.getId());
        }

        return convertToResDTO(be);
    }

    @Transactional
    public ResBookingEquipmentDTO updateStatusByClient(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req)
            throws IdInvalidException {
        return updateStatusInternal(id, req, false, true, true);
    }

    @Transactional
    public ResBookingEquipmentDTO updateStatusByAdmin(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req)
            throws IdInvalidException {
        return updateStatusInternal(id, req, true, false, false);
    }

    private ResBookingEquipmentDTO updateStatusInternal(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req,
            boolean notifyClient, boolean notifyAdmins, boolean submittedByClient)
            throws IdInvalidException {

        BookingEquipment be = getById(id);

        if (be.getStatus() != BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Chỉ có thể cập nhật trạng thái khi thiết bị đang được mượn");
        }

        if (submittedByClient) {
            if (!StringUtils.hasText(req.getReceiverName()) || !StringUtils.hasText(req.getReceiverPhone())) {
                throw new BadRequestException(
                        "Vui lòng nhập đầy đủ họ tên và số điện thoại người nhận thiết bị tại sân (bên giao nhận).");
            }
        }

        Equipment equipment = be.getEquipment();
        boolean detailed = req.getQuantityReturnedGood() != null
                || req.getQuantityLost() != null
                || req.getQuantityDamaged() != null;

        BookingEquipmentStatusEnum newStatus;
        if (detailed) {
            newStatus = applyReturnBreakdown(be, equipment, req);
        } else {
            newStatus = applyLegacyStatusChange(be, equipment, req.getStatus(), req);
        }

        if (submittedByClient) {
            be.setReturnAdminConfirmed(false);
            be.setReturnAdminConfirmedAt(null);
            be.setReturnAdminConfirmedBy(null);
        } else {
            be.setReturnAdminConfirmed(true);
            be.setReturnAdminConfirmedAt(Instant.now());
            be.setReturnAdminConfirmedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        }

        bookingEquipmentRepository.save(be);
        equipmentRepository.save(equipment);

        String returnNote = req.getReturnConditionNote();
        String breakdown = String.format("Trả tốt: %d — Mất: %d — Hỏng: %d",
                be.getQuantityReturnedGood(), be.getQuantityLost(), be.getQuantityDamaged());
        String returnLogNote = (returnNote != null && !returnNote.isBlank() ? returnNote + " — " : "")
                + breakdown + " — Trạng thái: " + mapStatusToVietnamese(newStatus);
        appendLog(be, EquipmentBorrowLogTypeEnum.RETURN, returnLogNote, be.getReturnerNameSnapshot(),
                be.getReturnerPhoneSnapshot());

        NotificationTypeEnum notifType = mapStatusToNotificationType(newStatus);
        String statusVi = mapStatusToVietnamese(newStatus);
        String actorName = be.getBooking().getUser().getFullName() != null
                && !be.getBooking().getUser().getFullName().isBlank()
                        ? be.getBooking().getUser().getFullName()
                        : be.getBooking().getUser().getName();

        if (notifyAdmins) {
            String adminMsg = String.format("%s đã cập nhật thiết bị %s (%dx) thành trạng thái: %s cho Booking #%d.",
                    actorName,
                    be.getEquipment().getName(),
                    be.getQuantity(),
                    statusVi,
                    be.getBooking().getId());
            notificationService.notifyAdmins(notifType, adminMsg, null, be.getBooking().getId());
        }

        if (notifyClient) {
            String clientMsg = String.format("Thiết bị %s (%dx) của Booking #%d đã được cập nhật trạng thái: %s.",
                    be.getEquipment().getName(),
                    be.getQuantity(),
                    be.getBooking().getId(),
                    statusVi);
            notificationService.createAndPush(be.getBooking().getUser(), notifType, clientMsg, be.getBooking().getId());
        }

        return convertToResDTO(be);
    }

    /**
     * Kiểm đếm chi tiết: trả tốt / mất / hỏng phải cộng đủ bằng SL mượn; có mất hoặc hỏng thì bắt buộc ký tên.
     */
    private BookingEquipmentStatusEnum applyReturnBreakdown(
            BookingEquipment be, Equipment equipment, ReqUpdateBookingEquipmentStatusDTO req) {
        int g = Optional.ofNullable(req.getQuantityReturnedGood()).orElse(0);
        int l = Optional.ofNullable(req.getQuantityLost()).orElse(0);
        int d = Optional.ofNullable(req.getQuantityDamaged()).orElse(0);
        int q = be.getQuantity();
        if (g < 0 || l < 0 || d < 0) {
            throw new BadRequestException("Số lượng không hợp lệ");
        }
        if (g + l + d != q) {
            throw new BadRequestException(
                    String.format("Tổng trả tốt + mất + hỏng (%d) phải bằng số lượng mượn (%d).", g + l + d, q));
        }
        if (l + d > 0) {
            if (!StringUtils.hasText(req.getBorrowerSignName()) || !StringUtils.hasText(req.getStaffSignName())) {
                throw new BadRequestException(
                        "Vui lòng ghi họ tên người mượn và nhân viên ký xác nhận khi có mất hoặc hỏng.");
            }
        }

        long unit = equipment.getPrice() != null ? equipment.getPrice().longValue() : 0L;
        be.setPenaltyAmount((long) l * unit);

        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + g);
        int drop = l + d;
        if (drop > 0) {
            int newTotal = equipment.getTotalQuantity() - drop;
            equipment.setTotalQuantity(Math.max(newTotal, 0));
        }

        be.setQuantityReturnedGood(g);
        be.setQuantityLost(l);
        be.setQuantityDamaged(d);
        be.setReturnConditionNote(req.getReturnConditionNote());
        be.setBorrowerSignName(trimToNull(req.getBorrowerSignName()));
        be.setStaffSignName(trimToNull(req.getStaffSignName()));
        be.setBookingBorrowerSnapshot(userDisplayName(be.getBooking().getUser()));
        applyReturnerSnapshots(be, req);

        BookingEquipmentStatusEnum derived;
        if (l == q) {
            derived = BookingEquipmentStatusEnum.LOST;
        } else if (d == q && g == 0) {
            derived = BookingEquipmentStatusEnum.DAMAGED;
        } else {
            derived = BookingEquipmentStatusEnum.RETURNED;
        }
        be.setStatus(derived);
        return derived;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Luồng cũ: một trạng thái cho cả dòng — vẫn lưu số liệu để in biên bản. */
    private BookingEquipmentStatusEnum applyLegacyStatusChange(
            BookingEquipment be,
            Equipment equipment,
            BookingEquipmentStatusEnum newStatus,
            ReqUpdateBookingEquipmentStatusDTO req) {
        int q = be.getQuantity();
        if (newStatus == BookingEquipmentStatusEnum.RETURNED) {
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + q);
            be.setQuantityReturnedGood(q);
            be.setQuantityLost(0);
            be.setQuantityDamaged(0);
            be.setPenaltyAmount(0L);
        } else if (newStatus == BookingEquipmentStatusEnum.LOST) {
            int newTotal = equipment.getTotalQuantity() - q;
            equipment.setTotalQuantity(Math.max(newTotal, 0));
            long penalty = (long) q * equipment.getPrice().longValue();
            be.setPenaltyAmount(penalty);
            be.setQuantityReturnedGood(0);
            be.setQuantityLost(q);
            be.setQuantityDamaged(0);
        } else if (newStatus == BookingEquipmentStatusEnum.DAMAGED) {
            int newTotal = equipment.getTotalQuantity() - q;
            equipment.setTotalQuantity(Math.max(newTotal, 0));
            be.setPenaltyAmount(0L);
            be.setQuantityReturnedGood(0);
            be.setQuantityLost(0);
            be.setQuantityDamaged(q);
        } else {
            throw new BadRequestException("Trạng thái không hợp lệ khi hoàn trả");
        }
        be.setStatus(newStatus);
        be.setReturnConditionNote(req.getReturnConditionNote());
        be.setBorrowerSignName(null);
        be.setStaffSignName(null);
        be.setBookingBorrowerSnapshot(userDisplayName(be.getBooking().getUser()));
        applyReturnerSnapshots(be, req);
        return newStatus;
    }

    private void applyReturnerSnapshots(BookingEquipment be, ReqUpdateBookingEquipmentStatusDTO req) {
        User u = be.getBooking().getUser();
        String rn = trimToNull(req.getReturnerName());
        String rp = trimToNull(req.getReturnerPhone());
        be.setReturnerNameSnapshot(rn != null ? rn : userDisplayName(u));
        be.setReturnerPhoneSnapshot(rp != null ? rp : safePhone(u));
        be.setReturnReportPrintOptIn(req.getReturnReportPrintOptIn());
        be.setReceiverNameSnapshot(trimToNull(req.getReceiverName()));
        be.setReceiverPhoneSnapshot(trimToNull(req.getReceiverPhone()));
    }

    @Transactional
    public ResBookingEquipmentDTO confirmReturnByAdmin(@NonNull Long id) throws IdInvalidException {
        BookingEquipment be = getById(id);
        if (be.getStatus() == BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Thiết bị chưa có thông tin trả.");
        }
        if (be.isReturnAdminConfirmed()) {
            throw new BadRequestException("Biên bản trả đã được xác nhận");
        }
        be.setReturnAdminConfirmed(true);
        be.setReturnAdminConfirmedAt(Instant.now());
        be.setReturnAdminConfirmedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        bookingEquipmentRepository.save(be);

        // Sau khi admin xác nhận biên bản trả, gửi thông báo cho khách của booking.
        String statusVi = mapStatusToVietnamese(be.getStatus());
        String clientMsg = String.format(
                "Biên bản trả thiết bị %s (%dx) của Booking #%d đã được admin xác nhận (%s).",
                be.getEquipment().getName(),
                be.getQuantity(),
                be.getBooking().getId(),
                statusVi);
        notificationService.createAndPush(
                be.getBooking().getUser(),
                mapStatusToNotificationType(be.getStatus()),
                clientMsg,
                be.getBooking().getId());

        return convertToResDTO(be);
    }

    private static String userDisplayName(User u) {
        if (u == null) {
            return null;
        }
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

    private void appendLog(BookingEquipment be, EquipmentBorrowLogTypeEnum type, String notes, String actorName,
            String actorPhone) {
        EquipmentBorrowLog log = new EquipmentBorrowLog();
        log.setBookingEquipment(be);
        log.setLogType(type);
        log.setNotes(notes);
        log.setActorName(actorName);
        log.setActorPhone(actorPhone);
        equipmentBorrowLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ResEquipmentBorrowLogDTO> getRecentBorrowLogs() {
        return equipmentBorrowLogRepository.findTop200ByOrderByCreatedAtDesc().stream()
                .map(this::toBorrowLogDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResEquipmentUsageStatsDTO getUsageStats() {
        List<ResEquipmentUsageRowDTO> byEquipment = bookingEquipmentRepository.aggregateBorrowCountByEquipment()
                .stream()
                .map(row -> new ResEquipmentUsageRowDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .collect(Collectors.toList());
        List<ResEquipmentUsageRowDTO> byPitch = bookingEquipmentRepository.aggregateBorrowCountByPitch()
                .stream()
                .map(row -> new ResEquipmentUsageRowDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .collect(Collectors.toList());
        return new ResEquipmentUsageStatsDTO(byEquipment, byPitch);
    }

    private ResEquipmentBorrowLogDTO toBorrowLogDto(EquipmentBorrowLog l) {
        BookingEquipment be = l.getBookingEquipment();
        com.example.backend.domain.entity.Booking b = be.getBooking();
        User u = b.getUser();
        var pitch = b.getPitch();

        ResEquipmentBorrowLogDTO d = new ResEquipmentBorrowLogDTO();
        d.setId(l.getId());
        d.setBookingEquipmentId(be.getId());
        d.setBookingId(b.getId());
        d.setEquipmentId(be.getEquipment().getId());
        d.setEquipmentName(be.getEquipment().getName());
        d.setLogType(l.getLogType());
        d.setNotes(l.getNotes());
        d.setCreatedAt(l.getCreatedAt());
        d.setCreatedBy(l.getCreatedBy());

        d.setBookingUserName(userDisplayName(u));
        d.setBookingUserPhone(safePhone(u));
        d.setPitchName(pitch != null ? pitch.getName() : null);
        d.setActorName(l.getActorName() != null ? l.getActorName() : userDisplayName(u));
        d.setActorPhone(l.getActorPhone() != null ? l.getActorPhone() : safePhone(u));
        d.setBorrowConditionAcknowledged(be.isBorrowConditionAcknowledged());
        d.setBorrowReportPrintOptIn(be.isBorrowReportPrintOptIn());
        d.setReturnerNameSnapshot(be.getReturnerNameSnapshot());
        d.setReturnerPhoneSnapshot(be.getReturnerPhoneSnapshot());
        d.setReturnReportPrintOptIn(be.getReturnReportPrintOptIn());
        d.setReceiverNameSnapshot(be.getReceiverNameSnapshot());
        d.setReceiverPhoneSnapshot(be.getReceiverPhoneSnapshot());
        d.setReturnAdminConfirmed(be.isReturnAdminConfirmed());
        return d;
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

    public List<ResBookingEquipmentDTO> getAll() {
        return bookingEquipmentRepository.findAll()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    public List<ResBookingEquipmentDTO> getAllByUserEmail(@NonNull String email) {
        return bookingEquipmentRepository.findByBookingUserEmail(email)
                .stream()
                .filter(be -> !be.isDeletedByClient())
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteByClient(@NonNull Long id, @NonNull String email) throws IdInvalidException {
        BookingEquipment be = getById(id);
        if (!be.getBooking().getUser().getEmail().equals(email)) {
            throw new BadRequestException("Không có quyền xóa bản ghi này");
        }
        if (be.getStatus() == BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Không thể xóa thiết bị đang được mượn");
        }
        be.setDeletedByClient(true);
        bookingEquipmentRepository.save(be);
    }

    public List<ResBookingEquipmentDTO> getByBookingId(@NonNull Long bookingId) {
        return bookingEquipmentRepository.findByBookingId(bookingId)
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    public BookingEquipment getById(@NonNull Long id) throws IdInvalidException {
        Optional<BookingEquipment> opt = bookingEquipmentRepository.findById(id);
        if (opt.isPresent())
            return opt.get();
        throw new IdInvalidException("Không tìm thấy bản ghi mượn thiết bị với ID = " + id);
    }

    public ResBookingEquipmentDTO convertToResDTO(BookingEquipment be) {
        ResBookingEquipmentDTO res = new ResBookingEquipmentDTO();
        res.setId(be.getId());
        res.setBookingId(be.getBooking().getId());
        res.setEquipmentId(be.getEquipment().getId());
        res.setEquipmentName(be.getEquipment().getName());
        res.setEquipmentImageUrl(be.getEquipment().getImageUrl());
        res.setQuantity(be.getQuantity());
        res.setStatus(be.getStatus());
        res.setPenaltyAmount(be.getPenaltyAmount() != null ? be.getPenaltyAmount() : 0L);
        res.setEquipmentPrice(be.getEquipment().getPrice() != null ? be.getEquipment().getPrice().longValue() : 0L);
        res.setDeletedByClient(be.isDeletedByClient());
        res.setEquipmentMobility(be.getEquipmentMobility());
        res.setBorrowConditionNote(be.getBorrowConditionNote());
        res.setReturnConditionNote(be.getReturnConditionNote());
        res.setQuantityReturnedGood(be.getQuantityReturnedGood() != null ? be.getQuantityReturnedGood() : 0);
        res.setQuantityLost(be.getQuantityLost() != null ? be.getQuantityLost() : 0);
        res.setQuantityDamaged(be.getQuantityDamaged() != null ? be.getQuantityDamaged() : 0);
        res.setBorrowerSignName(be.getBorrowerSignName());
        res.setStaffSignName(be.getStaffSignName());
        res.setBookingBorrowerSnapshot(be.getBookingBorrowerSnapshot());
        res.setBorrowConditionAcknowledged(be.isBorrowConditionAcknowledged());
        res.setBorrowReportPrintOptIn(be.isBorrowReportPrintOptIn());
        res.setReturnerNameSnapshot(be.getReturnerNameSnapshot());
        res.setReturnerPhoneSnapshot(be.getReturnerPhoneSnapshot());
        res.setReturnReportPrintOptIn(be.getReturnReportPrintOptIn());
        res.setReceiverNameSnapshot(be.getReceiverNameSnapshot());
        res.setReceiverPhoneSnapshot(be.getReceiverPhoneSnapshot());
        res.setReturnAdminConfirmed(be.isReturnAdminConfirmed());
        res.setReturnAdminConfirmedAt(be.getReturnAdminConfirmedAt());
        res.setReturnAdminConfirmedBy(be.getReturnAdminConfirmedBy());
        return res;
    }
}
