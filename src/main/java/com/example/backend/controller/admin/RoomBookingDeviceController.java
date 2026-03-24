package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.bookingequipment.ReqUpdateBookingEquipmentStatusDTO;
import com.example.backend.domain.response.bookingequipment.ResBookingEquipmentDTO;
import com.example.backend.domain.response.equipment.ResEquipmentBorrowLogDTO;
import com.example.backend.domain.response.equipment.ResEquipmentUsageStatsDTO;
import com.example.backend.service.RoomBookingDeviceService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

/**
 * Controller quản trị dòng mượn/trả theo booking phòng (rooms) — clone controller booking sân.
 */
@RestController
@RequestMapping("/api/v1")
public class RoomBookingDeviceController {

    private final RoomBookingDeviceService roomBookingDeviceService;

    public RoomBookingDeviceController(RoomBookingDeviceService roomBookingDeviceService) {
        this.roomBookingDeviceService = roomBookingDeviceService;
    }

    @GetMapping("/room-booking-devices")
    @ApiMessage("Lấy danh sách dòng mượn/trả theo booking phòng")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_VIEW')")
    public ResponseEntity<List<ResBookingEquipmentDTO>> getAll() {
        return ResponseEntity.ok(roomBookingDeviceService.getAll());
    }

    @PatchMapping("/room-booking-devices/{id}/status")
    @ApiMessage("Cập nhật trạng thái dòng mượn/trả theo booking phòng")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_UPDATE')")
    public ResponseEntity<ResBookingEquipmentDTO> updateStatus(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody @NonNull ReqUpdateBookingEquipmentStatusDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(roomBookingDeviceService.updateStatusByAdmin(id, dto));
    }

    @PostMapping("/room-booking-devices/{id}/confirm-return")
    @ApiMessage("Admin xác nhận biên bản trả cho dòng mượn/trả theo booking phòng")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_UPDATE')")
    public ResponseEntity<ResBookingEquipmentDTO> confirmReturn(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(roomBookingDeviceService.confirmReturnByAdmin(id));
    }

    @GetMapping("/room-equipment-borrow-logs")
    @ApiMessage("Nhật ký mượn/trả theo dòng thiết bị rooms (gần nhất)")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_VIEW')")
    public ResponseEntity<List<ResEquipmentBorrowLogDTO>> getBorrowLogs() {
        return ResponseEntity.ok(roomBookingDeviceService.getRecentBorrowLogs());
    }

    @GetMapping("/room-equipment-usage-stats")
    @ApiMessage("Thống kê số lần mượn theo thiết bị và theo phòng/tài sản cho rooms")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_EQUIPMENT_VIEW')")
    public ResponseEntity<ResEquipmentUsageStatsDTO> getUsageStats() {
        return ResponseEntity.ok(roomBookingDeviceService.getUsageStats());
    }
}

