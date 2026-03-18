package com.example.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.BookingEquipment;
import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.request.bookingequipment.ReqCreateBookingEquipmentDTO;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.repository.BookingEquipmentRepository;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
public class BookingEquipmentService {

    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final NotificationService notificationService;

    public BookingEquipmentService(
            BookingEquipmentRepository bookingEquipmentRepository,
            BookingService bookingService,
            EquipmentService equipmentService,
            NotificationService notificationService) {
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.bookingService = bookingService;
        this.equipmentService = equipmentService;
        this.notificationService = notificationService;
    }

    // Mượn thiết bị
    @Transactional
    public ResBookingEquipmentDTO borrowEquipmentByClient(@NonNull ReqCreateBookingEquipmentDTO req)
            throws IdInvalidException {
        return borrowEquipmentInternal(req, false, true);
    }

    // Admin tạo mượn thiết bị cho booking của khách
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

        // Kiểm tra trạng thái thiết bị
        if (equipment.getStatus() != com.example.backend.util.constant.equipment.EquipmentStatusEnum.ACTIVE) {
            throw new BadRequestException("Thiết bị hiện không thể cho mượn");
        }

        // Kiểm tra số lượng
        if (equipment.getAvailableQuantity() < req.getQuantity()) {
            throw new BadRequestException(
                    "Số lượng không đủ. Hiện chỉ còn " + equipment.getAvailableQuantity() + " thiết bị");
        }

        // Tạo bản ghi mượn
        BookingEquipment be = new BookingEquipment();
        be.setBooking(booking);
        be.setEquipment(equipment);
        be.setQuantity(req.getQuantity());
        be.setStatus(BookingEquipmentStatusEnum.BORROWED);

        bookingEquipmentRepository.save(be);

        // Giảm số lượng khả dụng
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - req.getQuantity());
        equipmentService.getEquipmentById(equipment.getId()); // reload (already handled by JPA)
        // Lưu trực tiếp qua repository để tránh circular
        com.example.backend.domain.entity.Equipment eq = equipment;
        eq.setAvailableQuantity(equipment.getAvailableQuantity());
        bookingEquipmentRepository.flush();

        String actorName = booking.getUser().getFullName() != null && !booking.getUser().getFullName().isBlank()
                ? booking.getUser().getFullName()
                : booking.getUser().getName();
        String msg = String.format("%s đã mượn %dx %s cho Booking #%d.",
                actorName, be.getQuantity(), equipment.getName(), booking.getId());

        if (notifyAdmins) {
            notificationService.notifyAdmins(NotificationTypeEnum.EQUIPMENT_BORROWED, msg);
        }
        if (notifyClient) {
            notificationService.createAndPush(booking.getUser(), NotificationTypeEnum.EQUIPMENT_BORROWED,
                    String.format("Bạn đã được tạo mượn %dx %s cho Booking #%d.",
                            be.getQuantity(), equipment.getName(), booking.getId()));
        }

        return convertToResDTO(be);
    }

    // Cập nhật trạng thái trả (RETURNED / LOST / DAMAGED)
    @Transactional
    public ResBookingEquipmentDTO updateStatusByClient(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req)
            throws IdInvalidException {
        return updateStatusInternal(id, req, false, true);
    }

    // Admin cập nhật trạng thái trả thiết bị
    @Transactional
    public ResBookingEquipmentDTO updateStatusByAdmin(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req)
            throws IdInvalidException {
        return updateStatusInternal(id, req, true, false);
    }

    private ResBookingEquipmentDTO updateStatusInternal(@NonNull Long id, ReqUpdateBookingEquipmentStatusDTO req,
            boolean notifyClient, boolean notifyAdmins)
            throws IdInvalidException {

        BookingEquipment be = getById(id);

        if (be.getStatus() != BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Chỉ có thể cập nhật trạng thái khi thiết bị đang được mượn");
        }

        BookingEquipmentStatusEnum newStatus = req.getStatus();
        Equipment equipment = be.getEquipment();

        if (newStatus == BookingEquipmentStatusEnum.RETURNED) {
            // Trả đủ → hoàn lại availableQuantity
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + be.getQuantity());

        } else if (newStatus == BookingEquipmentStatusEnum.LOST) {
            // Mất → giảm totalQuantity (thiết bị không còn tồn tại)
            // availableQuantity không hoàn vì thiết bị đã mất
            int newTotal = equipment.getTotalQuantity() - be.getQuantity();
            equipment.setTotalQuantity(Math.max(newTotal, 0));
            // Tính tiền phạt = số lượng × đơn giá
            long penalty = (long) be.getQuantity() * equipment.getPrice().longValue();
            be.setPenaltyAmount(penalty);

        } else if (newStatus == BookingEquipmentStatusEnum.DAMAGED) {
            // Hỏng → giảm totalQuantity, không hoàn availableQuantity
            int newTotal = equipment.getTotalQuantity() - be.getQuantity();
            equipment.setTotalQuantity(Math.max(newTotal, 0));
        }

        be.setStatus(newStatus);
        bookingEquipmentRepository.save(be);

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
            notificationService.notifyAdmins(notifType, adminMsg);
        }

        if (notifyClient) {
            String clientMsg = String.format("Thiết bị %s (%dx) của Booking #%d đã được cập nhật trạng thái: %s.",
                    be.getEquipment().getName(),
                    be.getQuantity(),
                    be.getBooking().getId(),
                    statusVi);
            notificationService.createAndPush(be.getBooking().getUser(), notifType, clientMsg);
        }

        return convertToResDTO(be);
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

    // Lấy tất cả bản ghi mượn thiết bị (admin)
    public List<ResBookingEquipmentDTO> getAll() {
        return bookingEquipmentRepository.findAll()
                .stream()
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả lịch sử mượn thiết bị của một user (theo email), loại bỏ bản ghi
    // đã xóa phía client
    public List<ResBookingEquipmentDTO> getAllByUserEmail(@NonNull String email) {
        return bookingEquipmentRepository.findByBookingUserEmail(email)
                .stream()
                .filter(be -> !be.isDeletedByClient())
                .map(this::convertToResDTO)
                .collect(Collectors.toList());
    }

    // Soft-delete phía client: ẩn khỏi danh sách client, admin vẫn thấy
    @Transactional
    public void softDeleteByClient(@NonNull Long id, @NonNull String email) throws IdInvalidException {
        BookingEquipment be = getById(id);
        // Chỉ được xóa bản ghi của chính mình
        if (!be.getBooking().getUser().getEmail().equals(email)) {
            throw new BadRequestException("Không có quyền xóa bản ghi này");
        }
        // Chỉ xóa khi đã hoàn tất (không phải BORROWED)
        if (be.getStatus() == BookingEquipmentStatusEnum.BORROWED) {
            throw new BadRequestException("Không thể xóa thiết bị đang được mượn");
        }
        be.setDeletedByClient(true);
        bookingEquipmentRepository.save(be);
    }

    // Lấy tất cả thiết bị của một booking
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
        return res;
    }
}
