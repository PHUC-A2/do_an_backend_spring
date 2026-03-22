package com.example.backend.repository.v2;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.v2.RoomSchedule;

/**
 * Truy vấn {@link RoomSchedule} (bảng {@code room_schedules_v2}).
 */
@Repository
public interface RoomScheduleRepository extends JpaRepository<RoomSchedule, Long> {

    @Query("SELECT rs FROM RoomSchedule rs JOIN FETCH rs.room WHERE rs.room.id = :roomId AND rs.isActive = true")
    Optional<RoomSchedule> findByRoom_IdAndIsActiveTrue(@Param("roomId") Long roomId);

    @Query("SELECT rs FROM RoomSchedule rs JOIN FETCH rs.room WHERE rs.id = :id AND rs.room.id = :roomId")
    Optional<RoomSchedule> findByIdAndRoom_Id(@Param("id") Long id, @Param("roomId") Long roomId);
}
