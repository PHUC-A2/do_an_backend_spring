package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.BookingEquipment;

public interface BookingEquipmentRepository extends JpaRepository<BookingEquipment, Long>, JpaSpecificationExecutor<BookingEquipment> {

    List<BookingEquipment> findByBookingId(Long bookingId);

    List<BookingEquipment> findByBookingUserEmail(String email);

    @Query(value = "SELECT e.id, e.name, COUNT(be.id) FROM booking_equipments be INNER JOIN equipments e ON e.id = be.equipment_id GROUP BY e.id, e.name ORDER BY COUNT(be.id) DESC", nativeQuery = true)
    List<Object[]> aggregateBorrowCountByEquipment();

    @Query(value = "SELECT p.id, p.name, COUNT(be.id) FROM booking_equipments be INNER JOIN bookings b ON b.id = be.booking_id INNER JOIN pitches p ON p.id = b.pitch_id GROUP BY p.id, p.name ORDER BY COUNT(be.id) DESC", nativeQuery = true)
    List<Object[]> aggregateBorrowCountByPitch();
}
