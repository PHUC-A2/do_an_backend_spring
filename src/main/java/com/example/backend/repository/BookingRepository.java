package com.example.backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // check trùng lịch
    boolean existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Long pitchId,
            LocalDateTime end,
            LocalDateTime start);

    boolean existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
            Long pitchId,
            LocalDateTime end,
            LocalDateTime start,
            Long id);

}
