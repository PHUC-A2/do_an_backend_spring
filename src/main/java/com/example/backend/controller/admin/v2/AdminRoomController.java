package com.example.backend.controller.admin.v2;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.v2.Room;
import com.example.backend.domain.request.v2.ReqCreateRoomDTO;
import com.example.backend.domain.request.v2.ReqUpdateRoomDTO;
import com.example.backend.domain.request.v2.ReqUpdateRoomStatusDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.v2.ResCreateRoomDTO;
import com.example.backend.domain.response.v2.ResRoomDTO;
import com.example.backend.domain.response.v2.ResUpdateRoomDTO;
import com.example.backend.service.v2.RoomService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

/**
 * API quản trị phòng tin học — tiền tố {@code /api/v2/admin/rooms}.
 */
@RestController
@RequestMapping("/api/v2/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    /**
     * @param roomService tầng nghiệp vụ phòng
     */
    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Tạo phòng mới. Yêu cầu quyền {@code ROOM_CREATE} hoặc {@code ALL}.
     */
    @PostMapping
    @ApiMessage("Thêm phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_CREATE')")
    public ResponseEntity<ResCreateRoomDTO> createRoom(@Valid @RequestBody ReqCreateRoomDTO dto) {
        ResCreateRoomDTO res = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Danh sách phòng có phân trang và bộ lọc động (Spring Filter).
     * Yêu cầu quyền {@code ROOM_VIEW_LIST} hoặc {@code ALL}.
     */
    @GetMapping
    @ApiMessage("Lấy danh sách phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getRooms(
            @Filter Specification<Room> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(roomService.getRooms(spec, pageable));
    }

    /**
     * Chi tiết một phòng theo id.
     * Yêu cầu quyền {@code ROOM_VIEW_DETAIL} hoặc {@code ALL}.
     */
    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_VIEW_DETAIL')")
    public ResponseEntity<ResRoomDTO> getRoomById(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        return ResponseEntity.ok(roomService.getRoomDtoById(id));
    }

    /**
     * Cập nhật toàn bộ thông tin phòng (trừ trạng thái có thể đổi riêng bằng PATCH).
     * Yêu cầu quyền {@code ROOM_UPDATE} hoặc {@code ALL}.
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_UPDATE')")
    public ResponseEntity<ResUpdateRoomDTO> updateRoom(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateRoomDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    /**
     * Chỉ cập nhật trạng thái phòng (hoạt động / ngưng / bảo trì).
     * Yêu cầu quyền {@code ROOM_UPDATE} hoặc {@code ALL}.
     */
    @PatchMapping("/{id}/status")
    @ApiMessage("Cập nhật trạng thái phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_UPDATE')")
    public ResponseEntity<ResRoomDTO> updateRoomStatus(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateRoomStatusDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, dto.getStatus()));
    }

    /**
     * Xóa phòng theo id.
     * Yêu cầu quyền {@code ROOM_DELETE} hoặc {@code ALL}.
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa phòng thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROOM_DELETE')")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }
}
