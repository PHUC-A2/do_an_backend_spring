package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Booking;
import com.example.backend.util.constant.booking.BookingStatusEnum;

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

        List<Booking> findByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTimeAsc(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start);

        boolean existsByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start);

        boolean existsByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start,
                        Long id);

        long countByDeletedByUserFalse();

        long countByStatus(BookingStatusEnum status);

}
