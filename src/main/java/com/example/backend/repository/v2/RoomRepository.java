package com.example.backend.repository.v2;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.v2.Room;

/**
 * Truy vấn CSDL cho phòng tin học ({@link Room} / bảng {@code rooms_v2}).
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    /**
     * Kiểm tra đã tồn tại phòng có cùng tên hiển thị hay chưa (dùng khi tạo mới).
     *
     * @param roomName tên phòng đã chuẩn hóa
     * @return {@code true} nếu trùng tên
     */
    boolean existsByRoomName(String roomName);

    /**
     * Kiểm tra tên phòng trùng với bản ghi khác (dùng khi cập nhật, loại trừ chính {@code id} hiện tại).
     *
     * @param roomName tên phòng
     * @param id       id bản ghi đang sửa
     * @return {@code true} nếu tên đã được phòng khác sử dụng
     */
    boolean existsByRoomNameAndIdNot(String roomName, Long id);

    /**
     * Tìm phòng theo tên hiển thị (duy nhất trong hệ thống).
     *
     * @param roomName tên phòng
     * @return phòng nếu có
     */
    Optional<Room> findByRoomName(String roomName);
}
