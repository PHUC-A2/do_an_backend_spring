package com.example.backend.service.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.v2.Room;
import com.example.backend.util.constant.v2.room.RoomStatusEnum;
import com.example.backend.domain.request.v2.ReqCreateRoomDTO;
import com.example.backend.domain.request.v2.ReqUpdateRoomDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.v2.ResCreateRoomDTO;
import com.example.backend.domain.response.v2.ResRoomDTO;
import com.example.backend.domain.response.v2.ResUpdateRoomDTO;
import com.example.backend.repository.v2.RoomRepository;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * Nghiệp vụ phòng tin học: tạo, tra cứu, cập nhật, đổi trạng thái, xóa và ánh xạ entity ↔ DTO.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * @param roomRepository kho lưu trữ phòng
     */
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Tạo phòng mới sau khi kiểm tra tên không trùng.
     *
     * @param req dữ liệu tạo phòng
     * @return DTO phản hồi sau khi lưu
     * @throws BadRequestException nếu tên phòng đã tồn tại
     */
    @Transactional
    public ResCreateRoomDTO createRoom(@NonNull ReqCreateRoomDTO req) {
        String name = normalizeRoomName(req.getRoomName());
        if (roomRepository.existsByRoomName(name)) {
            throw new BadRequestException("Tên phòng \"" + name + "\" đã tồn tại");
        }

        Room room = new Room();
        applyCreate(req, room, name);
        Room saved = roomRepository.save(room);
        return toResCreateRoomDTO(saved);
    }

    /**
     * Lấy danh sách phòng có phân trang, áp dụng {@link Specification} (bộ lọc).
     *
     * @param spec     điều kiện lọc (có thể rỗng)
     * @param pageable thông tin trang và sắp xếp
     * @return meta phân trang + danh sách {@link ResRoomDTO}
     */
    public ResultPaginationDTO getRooms(Specification<Room> spec, @NonNull Pageable pageable) {
        Page<Room> page = roomRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        rs.setMeta(mt);

        List<ResRoomDTO> list = new ArrayList<>();
        for (Room r : page.getContent()) {
            list.add(toResRoomDTO(r));
        }
        rs.setResult(list);
        return rs;
    }

    /**
     * Tìm phòng theo id; ném lỗi nếu không tồn tại.
     *
     * @param id khóa chính
     * @return entity phòng
     * @throws IdInvalidException không có phòng tương ứng
     */
    public Room getRoomById(@NonNull Long id) throws IdInvalidException {
        Optional<Room> opt = roomRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy phòng với ID = " + id);
    }

    /**
     * Chi tiết phòng dạng DTO.
     *
     * @param id khóa chính
     * @return {@link ResRoomDTO}
     * @throws IdInvalidException không có phòng tương ứng
     */
    public ResRoomDTO getRoomDtoById(@NonNull Long id) throws IdInvalidException {
        return toResRoomDTO(getRoomById(id));
    }

    /**
     * Cập nhật toàn bộ thông tin phòng (trừ id); kiểm tra tên không trùng phòng khác.
     *
     * @param id  phòng cần sửa
     * @param req dữ liệu cập nhật
     * @return DTO sau khi lưu
     * @throws IdInvalidException phòng không tồn tại
     * @throws BadRequestException tên phòng bị trùng với phòng khác
     */
    @Transactional
    public ResUpdateRoomDTO updateRoom(@NonNull Long id, @NonNull ReqUpdateRoomDTO req) throws IdInvalidException {
        Room room = getRoomById(id);
        String name = normalizeRoomName(req.getRoomName());
        if (roomRepository.existsByRoomNameAndIdNot(name, id)) {
            throw new BadRequestException("Tên phòng \"" + name + "\" đã được sử dụng");
        }

        room.setRoomName(name);
        room.setBuilding(req.getBuilding().trim());
        room.setFloor(req.getFloor());
        room.setRoomNumber(req.getRoomNumber());
        room.setCapacity(req.getCapacity());
        room.setDescription(req.getDescription());
        room.setStatus(req.getStatus());
        room.setContactPerson(trimToNull(req.getContactPerson()));
        room.setContactPhone(trimToNull(req.getContactPhone()));
        room.setKeyLocation(trimToNull(req.getKeyLocation()));
        room.setNotes(req.getNotes());
        room.setRoomUrl(req.getRoomUrl());

        Room saved = roomRepository.save(room);
        return toResUpdateRoomDTO(saved);
    }

    /**
     * Chỉ đổi trạng thái vận hành của phòng.
     *
     * @param id     phòng cần đổi trạng thái
     * @param status trạng thái mới
     * @return DTO phòng sau khi lưu
     * @throws IdInvalidException phòng không tồn tại
     */
    @Transactional
    public ResRoomDTO updateRoomStatus(@NonNull Long id, @NonNull RoomStatusEnum status) throws IdInvalidException {
        Room room = getRoomById(id);
        room.setStatus(status);
        return toResRoomDTO(roomRepository.save(room));
    }

    /**
     * Xóa phòng theo id (sau khi xác nhận tồn tại).
     *
     * @param id khóa chính
     * @throws IdInvalidException phòng không tồn tại
     */
    @Transactional
    public void deleteRoom(@NonNull Long id) throws IdInvalidException {
        getRoomById(id);
        roomRepository.deleteById(id);
    }

    /**
     * Gán các trường từ request tạo mới vào entity (tên đã chuẩn hóa trước đó).
     *
     * @param req      body tạo phòng
     * @param room     entity mới
     * @param roomName tên phòng đã {@link #normalizeRoomName(String)}
     */
    private static void applyCreate(ReqCreateRoomDTO req, Room room, String roomName) {
        room.setRoomName(roomName);
        room.setBuilding(req.getBuilding().trim());
        room.setFloor(req.getFloor());
        room.setRoomNumber(req.getRoomNumber());
        room.setCapacity(req.getCapacity());
        room.setDescription(req.getDescription());
        room.setStatus(req.getStatus() != null ? req.getStatus() : RoomStatusEnum.ACTIVE);
        room.setRoomUrl(req.getRoomUrl());
        room.setContactPerson(trimToNull(req.getContactPerson()));
        room.setContactPhone(trimToNull(req.getContactPhone()));
        room.setKeyLocation(trimToNull(req.getKeyLocation()));
        room.setNotes(req.getNotes());
    }

    /**
     * Chuẩn hóa tên phòng: trim, gộp nhiều khoảng trắng thành một; giữ nguyên chữ hoa/thường.
     *
     * @param raw chuỗi gốc (có thể {@code null})
     * @return chuỗi đã chuẩn hóa, hoặc rỗng nếu {@code null}
     */
    private static String normalizeRoomName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", " ");
    }

    /**
     * Trim chuỗi; nếu rỗng sau trim thì trả {@code null} (để lưu CSDL gọn).
     *
     * @param s chuỗi tùy chọn
     * @return giá trị đã trim hoặc {@code null}
     */
    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Ánh xạ entity phòng sang DTO đầy đủ (dùng cho chi tiết và danh sách).
     *
     * @param r entity
     * @return {@link ResRoomDTO}
     */
    public ResRoomDTO toResRoomDTO(Room r) {
        ResRoomDTO dto = new ResRoomDTO();
        dto.setId(r.getId());
        dto.setRoomName(r.getRoomName());
        dto.setBuilding(r.getBuilding());
        dto.setFloor(r.getFloor());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setCapacity(r.getCapacity());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setRoomUrl(r.getRoomUrl());
        dto.setContactPerson(r.getContactPerson());
        dto.setContactPhone(r.getContactPhone());
        dto.setKeyLocation(r.getKeyLocation());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        dto.setCreatedBy(r.getCreatedBy());
        dto.setUpdatedBy(r.getUpdatedBy());
        return dto;
    }

    /**
     * DTO phản hồi sau tạo mới (kèm thông tin audit tạo).
     */
    private static ResCreateRoomDTO toResCreateRoomDTO(Room r) {
        ResCreateRoomDTO dto = new ResCreateRoomDTO();
        dto.setId(r.getId());
        dto.setRoomName(r.getRoomName());
        dto.setBuilding(r.getBuilding());
        dto.setFloor(r.getFloor());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setCapacity(r.getCapacity());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setRoomUrl(r.getRoomUrl());
        dto.setContactPerson(r.getContactPerson());
        dto.setContactPhone(r.getContactPhone());
        dto.setKeyLocation(r.getKeyLocation());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setCreatedBy(r.getCreatedBy());
        return dto;
    }

    /**
     * DTO phản hồi sau cập nhật (kèm thông tin audit sửa).
     */
    private static ResUpdateRoomDTO toResUpdateRoomDTO(Room r) {
        ResUpdateRoomDTO dto = new ResUpdateRoomDTO();
        dto.setId(r.getId());
        dto.setRoomName(r.getRoomName());
        dto.setBuilding(r.getBuilding());
        dto.setFloor(r.getFloor());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setCapacity(r.getCapacity());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setRoomUrl(r.getRoomUrl());
        dto.setContactPerson(r.getContactPerson());
        dto.setContactPhone(r.getContactPhone());
        dto.setKeyLocation(r.getKeyLocation());
        dto.setNotes(r.getNotes());
        dto.setUpdatedAt(r.getUpdatedAt());
        dto.setUpdatedBy(r.getUpdatedBy());
        return dto;
    }
}
