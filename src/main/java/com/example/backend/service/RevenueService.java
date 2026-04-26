package com.example.backend.service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.response.revenue.*;
import com.example.backend.repository.*;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueService {

        private final PaymentRepository paymentRepository;
        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final PitchRepository pitchRepository;

        public ResRevenueDashboardDTO getDashboard(LocalDate fromDate, LocalDate toDate) {

                long tid = TenantContext.requireCurrentTenantId();

                ZoneId zone = ZoneId.systemDefault();

                Instant start = fromDate.atStartOfDay(zone).toInstant();
                Instant end = toDate.plusDays(1)
                                .atStartOfDay(zone)
                                .toInstant()
                                .minusMillis(1);

                BigDecimal totalRevenue = paymentRepository
                                .sumRevenueByDateRange(tid, PaymentStatusEnum.PAID, start, end);

                LocalDate today = LocalDate.now();
                Instant todayStart = today.atStartOfDay(zone).toInstant();
                Instant todayEnd = today.plusDays(1)
                                .atStartOfDay(zone)
                                .toInstant()
                                .minusMillis(1);

                BigDecimal todayRevenue = paymentRepository
                                .sumRevenueByDateRange(tid, PaymentStatusEnum.PAID, todayStart, todayEnd);

                LocalDate firstDayOfWeek = today.with(DayOfWeek.MONDAY);
                Instant weekStart = firstDayOfWeek.atStartOfDay(zone).toInstant();

                BigDecimal weekRevenue = paymentRepository
                                .sumRevenueByDateRange(tid, PaymentStatusEnum.PAID, weekStart, todayEnd);

                LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                Instant monthStart = firstDayOfMonth.atStartOfDay(zone).toInstant();

                BigDecimal monthRevenue = paymentRepository
                                .sumRevenueByDateRange(tid, PaymentStatusEnum.PAID, monthStart, todayEnd);

                List<RevenuePointDTO> revenueByDate = paymentRepository
                                .revenueGroupedByDate(tid, PaymentStatusEnum.PAID, start, end)
                                .stream()
                                .map(row -> RevenuePointDTO.builder()
                                                .label(row[0].toString())
                                                .revenue((BigDecimal) row[1])
                                                .build())
                                .toList();

                List<RevenueByPitchDTO> revenueByPitch = paymentRepository
                                .revenueGroupedByPitch(tid, PaymentStatusEnum.PAID, start, end)
                                .stream()
                                .map(row -> RevenueByPitchDTO.builder()
                                                .pitchId((Long) row[0])
                                                .pitchName((String) row[1])
                                                .revenue((BigDecimal) row[2])
                                                .build())
                                .toList();

                return ResRevenueDashboardDTO.builder()
                                .totalRevenue(totalRevenue)
                                .todayRevenue(todayRevenue)
                                .weekRevenue(weekRevenue)
                                .monthRevenue(monthRevenue)
                                .totalBookings(
                                                bookingRepository.countByDeletedByUserFalseAndTenantId(tid))
                                .paidBookings(
                                                bookingRepository.countByStatusAndTenantId(BookingStatusEnum.PAID, tid))
                                .cancelledBookings(
                                                bookingRepository.countByStatusAndTenantId(BookingStatusEnum.CANCELLED, tid))
                                .totalUsers(userRepository.count())
                                .totalPitches(pitchRepository.countByTenantId(tid))
                                .revenueByDate(revenueByDate)
                                .revenueByPitch(revenueByPitch)
                                .build();

        }
}
