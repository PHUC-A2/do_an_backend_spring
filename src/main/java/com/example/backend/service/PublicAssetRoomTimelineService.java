package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.response.timeline.room.ResAssetRoomTimelineDTO;
import com.example.backend.domain.response.timeline.room.ResRoomBusyIntervalDTO;
import com.example.backend.domain.response.timeline.room.ResRoomFlexibleSlotDTO;
import com.example.backend.domain.response.timeline.room.ResRoomPeriodSlotDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.booking.SlotStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.schedule.RoomPeriodDefinitions;
import com.example.backend.util.schedule.RoomPeriodDefinitions.PeriodDef;

/**
 * Lịch trống phòng tin (asset) — tiết cố định hoặc khung bận linh hoạt (theo {@link AssetUsage}).
 */
@Service
public class PublicAssetRoomTimelineService {

    public static final String MODE_PERIODS = "PERIODS";
    public static final String MODE_FLEXIBLE = "FLEXIBLE";
    private static final int FLEX_SLOT_MINUTES = 5;

    private static final Set<AssetUsageStatus> TIMELINE_BLOCKING = EnumSet.of(
            AssetUsageStatus.PENDING,
            AssetUsageStatus.APPROVED,
            AssetUsageStatus.IN_PROGRESS);

    private static final LocalTime DEFAULT_FLEX_VIEW_START = LocalTime.of(7, 0);
    private static final LocalTime DEFAULT_FLEX_VIEW_END = LocalTime.of(22, 0);

    private final AssetRepository assetRepository;
    private final AssetUsageRepository assetUsageRepository;

    public PublicAssetRoomTimelineService(
            AssetRepository assetRepository,
            AssetUsageRepository assetUsageRepository) {
        this.assetRepository = assetRepository;
        this.assetUsageRepository = assetUsageRepository;
    }

    public ResAssetRoomTimelineDTO getRoomTimeline(
            @NonNull Long assetId,
            @NonNull LocalDate date,
            @NonNull String modeRaw) {

        String mode = modeRaw.trim().toUpperCase();
        if (!MODE_PERIODS.equals(mode) && !MODE_FLEXIBLE.equals(mode)) {
            throw new BadRequestException("mode phải là PERIODS hoặc FLEXIBLE");
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản (phòng)"));

        List<AssetUsage> usages = assetUsageRepository.findUsagesForRoomTimeline(
                asset.getId(),
                date,
                TIMELINE_BLOCKING);

        if (MODE_PERIODS.equals(mode)) {
            return buildPeriodsResponse(asset, date, usages);
        }
        return buildFlexibleResponse(asset, date, usages);
    }

    private ResAssetRoomTimelineDTO buildPeriodsResponse(Asset asset, LocalDate date, List<AssetUsage> usages) {
        LocalDateTime now = LocalDateTime.now();
        List<ResRoomPeriodSlotDTO> periods = new ArrayList<>();

        for (PeriodDef def : RoomPeriodDefinitions.TEN_PERIODS) {
            LocalDateTime pStart = date.atTime(def.start());
            LocalDateTime pEnd = date.atTime(def.end());

            SlotStatus st;
            AssetUsageStatus busyUsageStatus = null;
            if (!pEnd.isAfter(now)) {
                st = SlotStatus.PAST;
            } else if (anyUsageOverlaps(usages, def.start(), def.end())) {
                st = SlotStatus.BUSY;
                busyUsageStatus = resolveBusyUsageStatus(usages, def.start(), def.end());
            } else {
                st = SlotStatus.FREE;
            }

            periods.add(new ResRoomPeriodSlotDTO(
                    def.index(),
                    "Tiết " + def.index(),
                    pStart,
                    pEnd,
                    st,
                    busyUsageStatus));
        }

        return new ResAssetRoomTimelineDTO(
                date,
                asset.getId(),
                MODE_PERIODS,
                resolveOpenTime(asset),
                resolveCloseTime(asset),
                FLEX_SLOT_MINUTES,
                periods,
                List.of(),
                resolveOpenTime(asset),
                resolveCloseTime(asset),
                List.of());
    }

    private ResAssetRoomTimelineDTO buildFlexibleResponse(Asset asset, LocalDate date, List<AssetUsage> usages) {
        List<ResRoomBusyIntervalDTO> busy = new ArrayList<>();
        for (AssetUsage u : usages) {
            LocalDateTime start = date.atTime(u.getStartTime());
            LocalDateTime end = date.atTime(u.getEndTime());
            busy.add(new ResRoomBusyIntervalDTO(start, end, u.getStatus()));
        }

        LocalDateTime now = LocalDateTime.now();
        List<ResRoomFlexibleSlotDTO> slots = new ArrayList<>();
        LocalDateTime cursor = resolveFlexibleStartDateTime(asset, date);
        LocalDateTime endBoundary = resolveFlexibleEndDateTime(asset, date);
        while (cursor.isBefore(endBoundary)) {
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = slotStart.plusMinutes(FLEX_SLOT_MINUTES);
            SlotStatus st;
            AssetUsageStatus busyUsageStatus = null;
            if (!slotEnd.isAfter(now)) {
                st = SlotStatus.PAST;
            } else if (anyUsageOverlapsDateTime(usages, date, slotStart, slotEnd)) {
                st = SlotStatus.BUSY;
                busyUsageStatus = resolveBusyUsageStatusDateTime(usages, date, slotStart, slotEnd);
            } else {
                st = SlotStatus.FREE;
            }
            slots.add(new ResRoomFlexibleSlotDTO(slotStart, slotEnd, st, busyUsageStatus));
            cursor = slotEnd;
        }

        return new ResAssetRoomTimelineDTO(
                date,
                asset.getId(),
                MODE_FLEXIBLE,
                resolveOpenTime(asset),
                resolveCloseTime(asset),
                FLEX_SLOT_MINUTES,
                List.of(),
                slots,
                resolveOpenTime(asset),
                resolveCloseTime(asset),
                busy);
    }

    private boolean anyUsageOverlaps(List<AssetUsage> usages, LocalTime periodStart, LocalTime periodEnd) {
        for (AssetUsage u : usages) {
            LocalTime uS = u.getStartTime();
            LocalTime uE = u.getEndTime();
            if (periodStart.isBefore(uE) && periodEnd.isAfter(uS)) {
                return true;
            }
        }
        return false;
    }

    private boolean anyUsageOverlapsDateTime(
            List<AssetUsage> usages,
            LocalDate date,
            LocalDateTime slotStart,
            LocalDateTime slotEnd) {
        for (AssetUsage u : usages) {
            LocalDateTime uStart = date.atTime(u.getStartTime());
            LocalDateTime uEnd = date.atTime(u.getEndTime());
            if (slotStart.isBefore(uEnd) && slotEnd.isAfter(uStart)) {
                return true;
            }
        }
        return false;
    }

    private LocalTime resolveOpenTime(Asset asset) {
        if (asset.isOpen24h()) {
            return LocalTime.MIN;
        }
        return asset.getOpenTime() != null ? asset.getOpenTime() : DEFAULT_FLEX_VIEW_START;
    }

    private LocalTime resolveCloseTime(Asset asset) {
        if (asset.isOpen24h()) {
            return LocalTime.MAX;
        }
        return asset.getCloseTime() != null ? asset.getCloseTime() : DEFAULT_FLEX_VIEW_END;
    }

    private LocalDateTime resolveFlexibleStartDateTime(Asset asset, LocalDate date) {
        return date.atTime(resolveOpenTime(asset));
    }

    private LocalDateTime resolveFlexibleEndDateTime(Asset asset, LocalDate date) {
        if (asset.isOpen24h()) {
            return date.plusDays(1).atStartOfDay();
        }
        return date.atTime(resolveCloseTime(asset));
    }

    /**
     * Ưu tiên trạng thái hiển thị trên timeline:
     * IN_PROGRESS (ĐANG MƯỢN) > APPROVED (ĐÃ ĐẶT) > PENDING (BẬN).
     */
    private AssetUsageStatus resolveBusyUsageStatus(List<AssetUsage> usages, LocalTime periodStart, LocalTime periodEnd) {
        AssetUsageStatus resolved = null;
        for (AssetUsage u : usages) {
            LocalTime uS = u.getStartTime();
            LocalTime uE = u.getEndTime();
            if (!(periodStart.isBefore(uE) && periodEnd.isAfter(uS))) {
                continue;
            }
            AssetUsageStatus st = u.getStatus();
            if (st == AssetUsageStatus.IN_PROGRESS) {
                return AssetUsageStatus.IN_PROGRESS;
            }
            if (st == AssetUsageStatus.APPROVED) {
                resolved = AssetUsageStatus.APPROVED;
            } else if (st == AssetUsageStatus.PENDING && resolved == null) {
                resolved = AssetUsageStatus.PENDING;
            }
        }
        return resolved;
    }

    private AssetUsageStatus resolveBusyUsageStatusDateTime(
            List<AssetUsage> usages,
            LocalDate date,
            LocalDateTime slotStart,
            LocalDateTime slotEnd) {
        AssetUsageStatus resolved = null;
        for (AssetUsage u : usages) {
            LocalDateTime uStart = date.atTime(u.getStartTime());
            LocalDateTime uEnd = date.atTime(u.getEndTime());
            if (!(slotStart.isBefore(uEnd) && slotEnd.isAfter(uStart))) {
                continue;
            }
            AssetUsageStatus st = u.getStatus();
            if (st == AssetUsageStatus.IN_PROGRESS) {
                return AssetUsageStatus.IN_PROGRESS;
            }
            if (st == AssetUsageStatus.APPROVED) {
                resolved = AssetUsageStatus.APPROVED;
            } else if (st == AssetUsageStatus.PENDING && resolved == null) {
                resolved = AssetUsageStatus.PENDING;
            }
        }
        return resolved;
    }
}
