package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.response.timeline.BookingTimeRange;
import com.example.backend.domain.response.timeline.ResPitchTimelineDTO;
import com.example.backend.domain.response.timeline.ResPitchTimelineSlotDTO;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.booking.SlotStatus;
import com.example.backend.util.error.BadRequestException;

@Service
public class PublicPitchBookingService {

        private final BookingRepository bookingRepository;
        private final PitchRepository pitchRepository;

        public PublicPitchBookingService(
                        BookingRepository bookingRepository,
                        PitchRepository pitchRepository) {
                this.bookingRepository = bookingRepository;
                this.pitchRepository = pitchRepository;
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
        private List<BookingTimeRange> getBookingRanges(
                        Long pitchId,
                        LocalDate date) {

                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);

                return bookingRepository
                                .findByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTimeAsc(
                                                pitchId,
                                                BookingStatusEnum.ACTIVE,
                                                endOfDay,
                                                startOfDay)

                                .stream()
                                .map(b -> new BookingTimeRange(
                                                b.getStartDateTime(),
                                                b.getEndDateTime()))
                                .toList();
        }

        private List<ResPitchTimelineSlotDTO> buildTimelineSlots(
                        Long pitchId,
                        LocalDate date,
                        int slotMinutes,
                        LocalTime openTime,
                        LocalTime closeTime) {

                List<BookingTimeRange> bookings = getBookingRanges(pitchId, date);

                List<ResPitchTimelineSlotDTO> slots = new ArrayList<>();

                LocalDateTime cursor = date.atTime(openTime);
                LocalDateTime end = date.atTime(closeTime);

                while (cursor.isBefore(end)) {

                        LocalDateTime slotStart = cursor;
                        LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);

                        boolean isBusy = bookings.stream().anyMatch(b -> slotStart.isBefore(b.getEnd()) &&
                                        slotEnd.isAfter(b.getStart()));

                        slots.add(new ResPitchTimelineSlotDTO(
                                        slotStart,
                                        slotEnd,
                                        isBusy ? SlotStatus.BUSY : SlotStatus.FREE));

                        cursor = slotEnd;
                }

                return slots;
        }
}
