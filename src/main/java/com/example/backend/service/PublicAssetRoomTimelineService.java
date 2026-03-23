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

    private static final Set<AssetUsageStatus> TIMELINE_BLOCKING = EnumSet.of(
            AssetUsageStatus.PENDING,
            AssetUsageStatus.APPROVED,
            AssetUsageStatus.IN_PROGRESS);

    private static final LocalTime FLEX_VIEW_START = LocalTime.of(7, 0);
    private static final LocalTime FLEX_VIEW_END = LocalTime.of(22, 0);

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
            return buildPeriodsResponse(asset.getId(), date, usages);
        }
        return buildFlexibleResponse(asset.getId(), date, usages);
    }

    private ResAssetRoomTimelineDTO buildPeriodsResponse(Long assetId, LocalDate date, List<AssetUsage> usages) {
        LocalDateTime now = LocalDateTime.now();
        List<ResRoomPeriodSlotDTO> periods = new ArrayList<>();

        for (PeriodDef def : RoomPeriodDefinitions.TEN_PERIODS) {
            LocalDateTime pStart = date.atTime(def.start());
            LocalDateTime pEnd = date.atTime(def.end());

            SlotStatus st;
            if (!pEnd.isAfter(now)) {
                st = SlotStatus.PAST;
            } else if (anyUsageOverlaps(usages, def.start(), def.end())) {
                st = SlotStatus.BUSY;
            } else {
                st = SlotStatus.FREE;
            }

            periods.add(new ResRoomPeriodSlotDTO(
                    def.index(),
                    "Tiết " + def.index(),
                    pStart,
                    pEnd,
                    st));
        }

        return new ResAssetRoomTimelineDTO(
                date,
                assetId,
                MODE_PERIODS,
                periods,
                FLEX_VIEW_START,
                FLEX_VIEW_END,
                List.of());
    }

    private ResAssetRoomTimelineDTO buildFlexibleResponse(Long assetId, LocalDate date, List<AssetUsage> usages) {
        List<ResRoomBusyIntervalDTO> busy = new ArrayList<>();
        for (AssetUsage u : usages) {
            busy.add(new ResRoomBusyIntervalDTO(
                    date.atTime(u.getStartTime()),
                    date.atTime(u.getEndTime())));
        }

        return new ResAssetRoomTimelineDTO(
                date,
                assetId,
                MODE_FLEXIBLE,
                List.of(),
                FLEX_VIEW_START,
                FLEX_VIEW_END,
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
}
