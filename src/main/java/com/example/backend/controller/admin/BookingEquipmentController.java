package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.bookingequipment.ReqCreateBookingEquipmentDTO;
import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.service.BookingEquipmentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class BookingEquipmentController {

    private final BookingEquipmentService bookingEquipmentService;

    public BookingEquipmentController(BookingEquipmentService bookingEquipmentService) {
        this.bookingEquipmentService = bookingEquipmentService;
    }

    // Tạo bản ghi mượn thiết bị
    @PostMapping("/booking-equipments")
    @ApiMessage("Mượn thiết bị cho booking")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_CREATE')")
    public ResponseEntity<ResBookingEquipmentDTO> borrowEquipment(
            @Valid @RequestBody @NonNull ReqCreateBookingEquipmentDTO dto) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingEquipmentService.borrowEquipmentByAdmin(dto));
    }

    // Lấy tất cả bản ghi mượn thiết bị
    @GetMapping("/booking-equipments")
    @ApiMessage("Lấy tất cả bản ghi mượn thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_VIEW')")
    public ResponseEntity<List<ResBookingEquipmentDTO>> getAll() {
        return ResponseEntity.ok(bookingEquipmentService.getAll());
    }

    // Lấy danh sách thiết bị theo bookingId
    @GetMapping("/booking-equipments/booking/{bookingId}")
    @ApiMessage("Lấy danh sách thiết bị của booking")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_VIEW')")
    public ResponseEntity<List<ResBookingEquipmentDTO>> getByBookingId(
            @PathVariable Long bookingId) throws IdInvalidException {
        return ResponseEntity.ok(bookingEquipmentService.getByBookingId(bookingId));
    }

    // Cập nhật trạng thái (RETURNED / LOST / DAMAGED)
    @PatchMapping("/booking-equipments/{id}/status")
    @ApiMessage("Cập nhật trạng thái thiết bị mượn")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_UPDATE')")
    public ResponseEntity<ResBookingEquipmentDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateBookingEquipmentStatusDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(bookingEquipmentService.updateStatusByAdmin(id, dto));
    }
}
