package com.example.backend.controller.client;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.BookingEquipment;
import com.example.backend.domain.request.bookingequipment.ReqCreateBookingEquipmentDTO;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.service.BookingEquipmentService;
import com.example.backend.service.BookingService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/client")
public class ClientBookingEquipmentController {

    private final BookingEquipmentService bookingEquipmentService;
    private final BookingService bookingService;

    public ClientBookingEquipmentController(
            BookingEquipmentService bookingEquipmentService,
            BookingService bookingService) {
        this.bookingEquipmentService = bookingEquipmentService;
        this.bookingService = bookingService;
    }

    // Client mượn thiết bị sau khi đặt sân — chỉ được mượn cho booking của chính
    // mình
    @PostMapping("/booking-equipments")
    @ApiMessage("Mượn thiết bị")
    public ResponseEntity<ResBookingEquipmentDTO> borrowEquipment(
            @Valid @RequestBody @NonNull ReqCreateBookingEquipmentDTO req) throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        // Kiểm tra booking thuộc về chính user
        Booking booking = bookingService.getBookingByIdForUser(req.getBookingId(), email);
        if (booking == null) {
            throw new BadRequestException("Không có quyền mượn thiết bị cho booking này");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingEquipmentService.borrowEquipmentByClient(req));
    }

    // Client cập nhật trạng thái thiết bị mượn (RETURNED / LOST / DAMAGED)
    @PatchMapping("/booking-equipments/{id}/status")
    @ApiMessage("Cập nhật trạng thái thiết bị mượn")
    public ResponseEntity<ResBookingEquipmentDTO> updateStatus(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull ReqUpdateBookingEquipmentStatusDTO req) throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        // Kiểm tra booking equipment tồn tại và thuộc về user
        BookingEquipment be = bookingEquipmentService.getById(id);
        if (be == null) {
            throw new IdInvalidException("Không tìm thấy bản ghi mượn thiết bị");
        }
        bookingService.getBookingByIdForUser(be.getBooking().getId(), email);

        return ResponseEntity.ok(bookingEquipmentService.updateStatusByClient(id, req));
    }

    // Client xem toàn bộ lịch sử mượn thiết bị của mình (tất cả booking)
    @GetMapping("/booking-equipments")
    @ApiMessage("Lấy lịch sử mượn thiết bị của user")
    public ResponseEntity<List<ResBookingEquipmentDTO>> getAllByUser() {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        return ResponseEntity.ok(bookingEquipmentService.getAllByUserEmail(email));
    }

    // Client xóa mềm bản ghi thiết bị mượn khỏi danh sách của mình (admin vẫn thấy)
    @DeleteMapping("/booking-equipments/{id}")
    @ApiMessage("Xóa thiết bị mượn khỏi danh sách")
    public ResponseEntity<Void> softDelete(
            @PathVariable @NonNull Long id) throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        bookingEquipmentService.softDeleteByClient(id, email);
        return ResponseEntity.ok(null);
    }

    // Client xem danh sách thiết bị đã mượn trong booking của mình
    @GetMapping("/booking-equipments/booking/{bookingId}")
    @ApiMessage("Lấy danh sách thiết bị của booking")
    public ResponseEntity<List<ResBookingEquipmentDTO>> getByBookingId(
            @PathVariable @NonNull Long bookingId) throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        // Xác nhận booking của chính user
        bookingService.getBookingByIdForUser(bookingId, email);

        return ResponseEntity.ok(bookingEquipmentService.getByBookingId(bookingId));
    }
}
