package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.response.timeline.BookingTimeRange;
import com.example.backend.domain.response.timeline.ResPitchTimelineDTO;
import com.example.backend.domain.response.timeline.ResPitchTimelineSlotDTO;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.booking.SlotStatus;
import com.example.backend.util.error.BadRequestException;

@Service
public class PublicPitchBookingService {

        private final BookingRepository bookingRepository;
        private final PitchRepository pitchRepository;
        private final UserService userService;

        public PublicPitchBookingService(
                        BookingRepository bookingRepository,
                        PitchRepository pitchRepository,
                        UserService userService) {
                this.bookingRepository = bookingRepository;
                this.pitchRepository = pitchRepository;
                this.userService = userService;
        }

        // ===== ENTRY POINT DUY NHẤT =====
        public ResPitchTimelineDTO getPitchTimeline(
                        @NonNull Long pitchId,
                        @NonNull LocalDate date) {

                Pitch pitch = pitchRepository.findById(pitchId)
                                .orElseThrow(() -> new BadRequestException("Pitch not found"));

                LocalTime openTime = pitch.isOpen24h()
                                ? LocalTime.MIN
                                : pitch.getOpenTime();

                LocalTime closeTime = pitch.isOpen24h()
                                ? LocalTime.MAX
                                : pitch.getCloseTime();

                int slotMinutes = 30;

                List<ResPitchTimelineSlotDTO> slots = buildTimelineSlots(pitchId, date, slotMinutes, openTime,
                                closeTime);

                return new ResPitchTimelineDTO(
                                openTime,
                                closeTime,
                                slotMinutes,
                                slots);
        }

        // ===== INTERNAL =====
        private List<Booking> getBookingsForTimeline(
                        Long pitchId,
                        LocalDate date) {

                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);

                // ACTIVE và PAID đều chiếm slot — chỉ CANCELLED mới giải phóng
                List<BookingStatusEnum> occupyingStatuses = List.of(
                                BookingStatusEnum.PENDING,
                                BookingStatusEnum.ACTIVE,
                                BookingStatusEnum.PAID);

                return bookingRepository
                                .findByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTimeAsc(
                                                pitchId,
                                                occupyingStatuses,
                                                endOfDay,
                                                startOfDay);
        }

        private List<ResPitchTimelineSlotDTO> buildTimelineSlots(
                        Long pitchId,
                        LocalDate date,
                        int slotMinutes,
                        LocalTime openTime,
                        LocalTime closeTime) {

                List<Booking> bookings = getBookingsForTimeline(pitchId, date);
                Long currentUserId = SecurityUtil.getCurrentUserLogin()
                                .map(userService::handleGetUserByUsername)
                                .map(u -> u != null ? u.getId() : null)
                                .orElse(null);

                List<ResPitchTimelineSlotDTO> slots = new ArrayList<>();

                LocalDateTime cursor = date.atTime(openTime);
                LocalDateTime end = date.atTime(closeTime);
                LocalDateTime now = LocalDateTime.now();

                while (cursor.isBefore(end)) {

                        LocalDateTime slotStart = cursor;
                        LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);

                        SlotStatus slotStatus;
                        if (!slotEnd.isAfter(now)) {
                                slotStatus = SlotStatus.PAST;
                        } else {
                                boolean hasApproved = bookings.stream()
                                                .anyMatch(b -> isOverlap(slotStart, slotEnd, b)
                                                                && (b.getStatus() == BookingStatusEnum.ACTIVE
                                                                                || b.getStatus() == BookingStatusEnum.PAID));
                                boolean hasPending = bookings.stream()
                                                .anyMatch(b -> isOverlap(slotStart, slotEnd, b)
                                                                && b.getStatus() == BookingStatusEnum.PENDING);
                                boolean minePending = currentUserId != null && bookings.stream()
                                                .anyMatch(b -> isOverlap(slotStart, slotEnd, b)
                                                                && b.getStatus() == BookingStatusEnum.PENDING
                                                                && b.getUser() != null
                                                                && currentUserId.equals(b.getUser().getId()));
                                boolean mineApproved = currentUserId != null && bookings.stream()
                                                .anyMatch(b -> isOverlap(slotStart, slotEnd, b)
                                                                && (b.getStatus() == BookingStatusEnum.ACTIVE
                                                                                || b.getStatus() == BookingStatusEnum.PAID)
                                                                && b.getUser() != null
                                                                && currentUserId.equals(b.getUser().getId()));

                                if (hasApproved) {
                                        slotStatus = minePending && !mineApproved
                                                        ? SlotStatus.BOOKED_BY_OTHER
                                                        : SlotStatus.BOOKED;
                                } else if (hasPending) {
                                        slotStatus = SlotStatus.PENDING;
                                } else {
                                        slotStatus = SlotStatus.FREE;
                                }
                        }

                        slots.add(new ResPitchTimelineSlotDTO(
                                        slotStart,
                                        slotEnd,
                                        slotStatus));

                        cursor = slotEnd;
                }

                return slots;
        }

        private boolean isOverlap(LocalDateTime slotStart, LocalDateTime slotEnd, Booking booking) {
                BookingTimeRange range = new BookingTimeRange(booking.getStartDateTime(), booking.getEndDateTime());
                return slotStart.isBefore(range.getEnd()) && slotEnd.isAfter(range.getStart());
        }
}
