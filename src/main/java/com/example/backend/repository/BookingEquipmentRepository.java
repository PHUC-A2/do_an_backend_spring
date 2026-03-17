package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend.domain.entity.BookingEquipment;

public interface BookingEquipmentRepository extends JpaRepository<BookingEquipment, Long>, JpaSpecificationExecutor<BookingEquipment> {

    List<BookingEquipment> findByBookingId(Long bookingId);

    List<BookingEquipment> findByBookingUserEmail(String email);
}
