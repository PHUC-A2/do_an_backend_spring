package com.example.backend.controller.admin.v2;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.v2.CreateRoomScheduleRequestV2;
import com.example.backend.domain.request.v2.UpdateRoomScheduleRequestV2;
import com.example.backend.domain.response.v2.RoomScheduleResponseV2;
import com.example.backend.domain.response.v2.SlotPreviewV2;
import com.example.backend.service.v2.RoomScheduleService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

/**
 * API cấu hình lịch tiết phòng — {@code /api/v2/admin/rooms/{roomId}/schedules}.
 */
@RestController
@RequestMapping("/api/v2/admin/rooms/{roomId}/schedules")
public class AdminRoomScheduleController {

    private final RoomScheduleService roomScheduleService;

    public AdminRoomScheduleController(RoomScheduleService roomScheduleService) {
        this.roomScheduleService = roomScheduleService;
    }

    @PostMapping
    @ApiMessage("Tạo cấu hình lịch tiết thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_UPDATE')")
    public ResponseEntity<RoomScheduleResponseV2> createSchedule(
            @PathVariable("roomId") @NonNull Long roomId,
            @Valid @RequestBody CreateRoomScheduleRequestV2 body) throws IdInvalidException {
        RoomScheduleResponseV2 res = roomScheduleService.createSchedule(roomId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @ApiMessage("Lấy cấu hình lịch tiết thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_VIEW_DETAIL')")
    public ResponseEntity<RoomScheduleResponseV2> getSchedule(@PathVariable("roomId") @NonNull Long roomId)
            throws IdInvalidException {
        return ResponseEntity.ok(roomScheduleService.getScheduleByRoomId(roomId));
    }

    @PostMapping("/preview")
    @ApiMessage("Xem trước lịch tiết thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_VIEW_DETAIL')")
    public ResponseEntity<List<SlotPreviewV2>> previewSlots(
            @PathVariable("roomId") @NonNull Long roomId,
            @Valid @RequestBody CreateRoomScheduleRequestV2 body) throws IdInvalidException {
        return ResponseEntity.ok(roomScheduleService.previewSlots(roomId, body));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật cấu hình lịch tiết thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_UPDATE')")
    public ResponseEntity<RoomScheduleResponseV2> updateSchedule(
            @PathVariable("roomId") @NonNull Long roomId,
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody UpdateRoomScheduleRequestV2 body) throws IdInvalidException {
        return ResponseEntity.ok(roomScheduleService.updateSchedule(roomId, id, body));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa cấu hình lịch tiết thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_UPDATE')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable("roomId") @NonNull Long roomId, @PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        roomScheduleService.deleteSchedule(roomId, id);
        return ResponseEntity.ok().build();
    }
}
