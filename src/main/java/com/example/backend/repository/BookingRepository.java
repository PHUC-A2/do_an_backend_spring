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

        // Timeline: query nhiều status cùng lúc (ACTIVE + PAID, bỏ CANCELLED)
        List<Booking> findByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTimeAsc(
                        Long pitchId,
                        List<BookingStatusEnum> statuses,
                        LocalDateTime end,
                        LocalDateTime start);

        boolean existsByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start);

        boolean existsByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        Long pitchId,
                        List<BookingStatusEnum> statuses,
                        LocalDateTime end,
                        LocalDateTime start);

        boolean existsByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start,
                        Long id);

        List<Booking> findByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNotOrderByStartDateTimeAsc(
                        Long pitchId,
                        BookingStatusEnum status,
                        LocalDateTime end,
                        LocalDateTime start,
                        Long id);

        boolean existsByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        Long pitchId,
                        List<BookingStatusEnum> statuses,
                        LocalDateTime end,
                        LocalDateTime start,
                        Long id);

        // Reminder: bookings starting between two times
        List<Booking> findByStatusInAndStartDateTimeBetween(
                        List<BookingStatusEnum> statuses,
                        LocalDateTime from,
                        LocalDateTime to);

        long countByDeletedByUserFalse();

        long countByStatus(BookingStatusEnum status);

        /** Booking chưa bị user ẩn khỏi lịch sử — theo từng trạng thái. */
        long countByDeletedByUserFalseAndStatus(BookingStatusEnum status);

        long countByTenantId(long tenantId);

        long countByDeletedByUserFalseAndTenantId(long tenantId);

        long countByDeletedByUserFalseAndStatusAndTenantId(BookingStatusEnum status, long tenantId);

        long countByStatusAndTenantId(BookingStatusEnum status, long tenantId);

}
