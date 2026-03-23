package com.example.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;

public interface AssetUsageRepository extends JpaRepository<AssetUsage, Long>, JpaSpecificationExecutor<AssetUsage> {

    /**
     * Đăng ký theo tài sản + ngày + trạng thái (timeline phòng) — JPQL tường minh, tránh xung đột parse với text block.
     */
    @Query("SELECT a FROM AssetUsage a WHERE a.asset.id = :assetId AND a.usageDate = :usageDate AND a.status IN :statuses ORDER BY a.startTime ASC")
    List<AssetUsage> findUsagesForRoomTimeline(
            @Param("assetId") Long assetId,
            @Param("usageDate") LocalDate usageDate,
            @Param("statuses") Collection<AssetUsageStatus> statuses);

    /**
     * Đếm bản ghi trùng khung giờ cùng tài sản/ngày (composite index db.md) — chỉ các trạng thái còn giữ chỗ.
     */
    @Query("SELECT COUNT(a) FROM AssetUsage a WHERE a.asset.id = :assetId AND a.usageDate = :usageDate AND a.status IN :statuses AND a.startTime < :endTime AND :startTime < a.endTime AND (a.id <> :excludeId OR :excludeId = 0)")
    long countTimeOverlap(
            @Param("assetId") Long assetId,
            @Param("usageDate") LocalDate usageDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") long excludeId,
            @Param("statuses") Collection<AssetUsageStatus> statuses);
}
