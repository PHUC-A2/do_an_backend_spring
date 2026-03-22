package com.example.backend.service.v2;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.v2.Room;
import com.example.backend.domain.entity.v2.RoomSchedule;
import com.example.backend.domain.request.v2.CreateRoomScheduleRequestV2;
import com.example.backend.domain.request.v2.UpdateRoomScheduleRequestV2;
import com.example.backend.domain.response.v2.RoomScheduleResponseV2;
import com.example.backend.domain.response.v2.SlotPreviewV2;
import com.example.backend.repository.v2.RoomRepository;
import com.example.backend.repository.v2.RoomScheduleRepository;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Nghiệp vụ cấu hình lịch tiết theo phòng.
 */
@Service
public class RoomScheduleService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final RoomRepository roomRepository;
    private final RoomScheduleRepository roomScheduleRepository;
    private final ObjectMapper objectMapper;

    public RoomScheduleService(
            RoomRepository roomRepository,
            RoomScheduleRepository roomScheduleRepository,
            ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.roomScheduleRepository = roomScheduleRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Tính danh sách tiết từ tham số cấu hình.
     *
     * <p>Ưu tiên xếp buổi sáng trước, sau đó buổi chiều. Nếu {@code morningGapBreaks} / {@code afternoonGapBreaks}
     * có giá trị, dùng từng phút nghỉ tương ứng; thiếu phần tử thì lấp bằng {@code breakDuration} (nghỉ đồng đều).
     */
    public List<SlotPreviewV2> calculateSlots(
            int totalSlots,
            int slotDuration,
            int breakDuration,
            @NonNull LocalTime morningStart,
            @NonNull LocalTime morningEnd,
            @NonNull LocalTime afternoonStart,
            @NonNull LocalTime afternoonEnd,
            List<Integer> morningGapBreaks,
            List<Integer> afternoonGapBreaks) {
        validateTimeRanges(morningStart, morningEnd, afternoonStart, afternoonEnd);
        validateGapList(morningGapBreaks);
        validateGapList(afternoonGapBreaks);

        List<Integer> mGaps = normalizeGapList(morningGapBreaks);
        List<Integer> aGaps = normalizeGapList(afternoonGapBreaks);

        List<SlotRange> morningSlots = buildSlotsInWindow(morningStart, morningEnd, slotDuration, breakDuration, mGaps);
        List<SlotRange> afternoonSlots = buildSlotsInWindow(afternoonStart, afternoonEnd, slotDuration, breakDuration, aGaps);

        int mMax = morningSlots.size();
        int aMax = afternoonSlots.size();
        int lo = Math.max(0, totalSlots - aMax);
        int hi = Math.min(mMax, totalSlots);
        if (lo > hi) {
            throw new BadRequestException(
                    "Không đủ khung giờ buổi sáng/chiều để xếp " + totalSlots + " tiết");
        }
        int morningCount = hi;

        List<SlotPreviewV2> result = new ArrayList<>();
        int n = 1;
        for (int i = 0; i < morningCount; i++) {
            SlotRange s = morningSlots.get(i);
            result.add(new SlotPreviewV2(n++, formatTime(s.start), formatTime(s.end)));
        }
        int afternoonCount = totalSlots - morningCount;
        for (int j = 0; j < afternoonCount; j++) {
            SlotRange s = afternoonSlots.get(j);
            result.add(new SlotPreviewV2(n++, formatTime(s.start), formatTime(s.end)));
        }
        return result;
    }

    @Transactional
    public RoomScheduleResponseV2 createSchedule(Long roomId, @NonNull CreateRoomScheduleRequestV2 req)
            throws IdInvalidException {
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phòng với ID = " + roomId));
        Optional<RoomSchedule> existing = roomScheduleRepository.findByRoom_IdAndIsActiveTrue(roomId);
        if (existing.isPresent()) {
            throw new BadRequestException("Phòng đã có cấu hình lịch tiết. Vui lòng cập nhật.");
        }

        List<SlotPreviewV2> slots = calculateSlots(
                req.getTotalSlots(),
                req.getSlotDuration(),
                req.getBreakDuration(),
                req.getMorningStart(),
                req.getMorningEnd(),
                req.getAfternoonStart(),
                req.getAfternoonEnd(),
                req.getMorningGapBreaks(),
                req.getAfternoonGapBreaks());

        RoomSchedule entity = new RoomSchedule();
        entity.setRoom(room);
        applyFromCreate(req, entity);
        entity.setIsActive(true);
        RoomSchedule saved = roomScheduleRepository.save(entity);
        return toResponse(saved, slots);
    }

    @Transactional(readOnly = true)
    public RoomScheduleResponseV2 getScheduleByRoomId(@NonNull Long roomId) throws IdInvalidException {
        ensureRoomExists(roomId);
        RoomSchedule s = roomScheduleRepository
                .findByRoom_IdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new IdInvalidException("Chưa có cấu hình lịch tiết cho phòng này"));
        List<SlotPreviewV2> slots = calculateSlots(
                s.getTotalSlots(),
                s.getSlotDuration(),
                s.getBreakDuration(),
                s.getMorningStart(),
                s.getMorningEnd(),
                s.getAfternoonStart(),
                s.getAfternoonEnd(),
                jsonToGapsList(s.getMorningGapBreaksJson()),
                jsonToGapsList(s.getAfternoonGapBreaksJson()));
        return toResponse(s, slots);
    }

    @Transactional
    public RoomScheduleResponseV2 updateSchedule(
            @NonNull Long roomId, @NonNull Long scheduleId, @NonNull UpdateRoomScheduleRequestV2 req)
            throws IdInvalidException {
        ensureRoomExists(roomId);
        RoomSchedule s = roomScheduleRepository
                .findByIdAndRoom_Id(scheduleId, roomId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy cấu hình lịch tiết"));
        if (!Boolean.TRUE.equals(s.getIsActive())) {
            throw new BadRequestException("Bản ghi không còn hiệu lực");
        }

        applyFromUpdate(req, s);
        List<SlotPreviewV2> slots = calculateSlots(
                s.getTotalSlots(),
                s.getSlotDuration(),
                s.getBreakDuration(),
                s.getMorningStart(),
                s.getMorningEnd(),
                s.getAfternoonStart(),
                s.getAfternoonEnd(),
                jsonToGapsList(s.getMorningGapBreaksJson()),
                jsonToGapsList(s.getAfternoonGapBreaksJson()));
        RoomSchedule saved = roomScheduleRepository.save(s);
        return toResponse(saved, slots);
    }

    @Transactional
    public void deleteSchedule(@NonNull Long roomId, @NonNull Long scheduleId) throws IdInvalidException {
        ensureRoomExists(roomId);
        RoomSchedule s = roomScheduleRepository
                .findByIdAndRoom_Id(scheduleId, roomId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy cấu hình lịch tiết"));
        s.setIsActive(false);
        roomScheduleRepository.save(s);
    }

    public List<SlotPreviewV2> previewSlots(Long roomId, @NonNull CreateRoomScheduleRequestV2 req)
            throws IdInvalidException {
        ensureRoomExists(roomId);
        return calculateSlots(
                req.getTotalSlots(),
                req.getSlotDuration(),
                req.getBreakDuration(),
                req.getMorningStart(),
                req.getMorningEnd(),
                req.getAfternoonStart(),
                req.getAfternoonEnd(),
                req.getMorningGapBreaks(),
                req.getAfternoonGapBreaks());
    }

    private void ensureRoomExists(Long roomId) throws IdInvalidException {
        Optional<Room> opt = roomRepository.findById(roomId);
        if (opt.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy phòng với ID = " + roomId);
        }
    }

    private void applyFromCreate(CreateRoomScheduleRequestV2 req, RoomSchedule e) {
        e.setTotalSlots(req.getTotalSlots());
        e.setSlotDuration(req.getSlotDuration());
        e.setBreakDuration(req.getBreakDuration());
        e.setMorningStart(req.getMorningStart());
        e.setMorningEnd(req.getMorningEnd());
        e.setAfternoonStart(req.getAfternoonStart());
        e.setAfternoonEnd(req.getAfternoonEnd());
        e.setMorningGapBreaksJson(gapsToJson(req.getMorningGapBreaks()));
        e.setAfternoonGapBreaksJson(gapsToJson(req.getAfternoonGapBreaks()));
    }

    private void applyFromUpdate(UpdateRoomScheduleRequestV2 req, RoomSchedule e) {
        e.setTotalSlots(req.getTotalSlots());
        e.setSlotDuration(req.getSlotDuration());
        e.setBreakDuration(req.getBreakDuration());
        e.setMorningStart(req.getMorningStart());
        e.setMorningEnd(req.getMorningEnd());
        e.setAfternoonStart(req.getAfternoonStart());
        e.setAfternoonEnd(req.getAfternoonEnd());
        e.setMorningGapBreaksJson(gapsToJson(req.getMorningGapBreaks()));
        e.setAfternoonGapBreaksJson(gapsToJson(req.getAfternoonGapBreaks()));
    }

    private String gapsToJson(List<Integer> gaps) {
        List<Integer> n = normalizeGapList(gaps);
        if (n == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(n);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Dữ liệu nghỉ giữa tiết không hợp lệ");
        }
    }

    private List<Integer> jsonToGapsList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception ex) {
            return null;
        }
    }

    private static List<Integer> normalizeGapList(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    private static void validateGapList(List<Integer> list) {
        if (list == null) {
            return;
        }
        for (Integer g : list) {
            if (g == null || g < 0 || g > 180) {
                throw new BadRequestException("Thời gian nghỉ giữa tiết phải từ 0 đến 180 phút");
            }
        }
    }

    private RoomScheduleResponseV2 toResponse(RoomSchedule s, List<SlotPreviewV2> slots) {
        RoomScheduleResponseV2 r = new RoomScheduleResponseV2();
        r.setId(s.getId());
        r.setRoomId(s.getRoom().getId());
        r.setTotalSlots(s.getTotalSlots());
        r.setSlotDuration(s.getSlotDuration());
        r.setBreakDuration(s.getBreakDuration());
        r.setMorningGapBreaks(jsonToGapsList(s.getMorningGapBreaksJson()));
        r.setAfternoonGapBreaks(jsonToGapsList(s.getAfternoonGapBreaksJson()));
        r.setMorningStart(s.getMorningStart());
        r.setMorningEnd(s.getMorningEnd());
        r.setAfternoonStart(s.getAfternoonStart());
        r.setAfternoonEnd(s.getAfternoonEnd());
        r.setIsActive(s.getIsActive());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        r.setSlots(slots);
        return r;
    }

    private static void validateTimeRanges(
            LocalTime morningStart,
            LocalTime morningEnd,
            LocalTime afternoonStart,
            LocalTime afternoonEnd) {
        if (!morningStart.isBefore(morningEnd)) {
            throw new BadRequestException("Giờ bắt đầu buổi sáng phải trước giờ kết thúc buổi sáng");
        }
        if (!afternoonStart.isBefore(afternoonEnd)) {
            throw new BadRequestException("Giờ bắt đầu buổi chiều phải trước giờ kết thúc buổi chiều");
        }
    }

    /**
     * Xếp tối đa số tiết vừa khung; sau mỗi tiết (trừ tiết cuối cùng trong khung) cộng thêm phút nghỉ.
     */
    private static List<SlotRange> buildSlotsInWindow(
            LocalTime windowStart,
            LocalTime windowEnd,
            int slotDuration,
            int uniformBreak,
            List<Integer> gapOverrides) {
        List<SlotRange> list = new ArrayList<>();
        LocalTime t = windowStart;
        while (true) {
            LocalTime endSlot = t.plusMinutes(slotDuration);
            if (endSlot.isAfter(windowEnd)) {
                break;
            }
            list.add(new SlotRange(t, endSlot));
            int gapAfter = gapMinutes(list.size() - 1, gapOverrides, uniformBreak);
            t = endSlot.plusMinutes(gapAfter);
        }
        return list;
    }

    private static int gapMinutes(int gapIndex, List<Integer> gapOverrides, int uniformBreak) {
        if (gapOverrides == null || gapOverrides.isEmpty()) {
            return uniformBreak;
        }
        if (gapIndex < gapOverrides.size()) {
            return gapOverrides.get(gapIndex);
        }
        return uniformBreak;
    }

    private static String formatTime(LocalTime t) {
        return t.format(TIME_FMT);
    }

    private static final class SlotRange {
        final LocalTime start;
        final LocalTime end;

        SlotRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
