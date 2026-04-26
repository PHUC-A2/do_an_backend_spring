package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.PitchEquipment;

@Repository
public interface PitchEquipmentRepository extends JpaRepository<PitchEquipment, Long> {

    long countByTenantId(long tenantId);

    List<PitchEquipment> findByPitchIdOrderByIdAsc(Long pitchId);

    @Query("SELECT COALESCE(SUM(pe.quantity), 0) FROM PitchEquipment pe WHERE pe.equipment.id = :equipmentId")
    long sumQuantityByEquipmentId(@Param("equipmentId") Long equipmentId);

    /** Thiết bị có thể gắn nhiều sân — dùng cho chi tiết thiết bị (admin). */
    List<PitchEquipment> findByEquipment_IdOrderByPitch_IdAsc(Long equipmentId);

    Optional<PitchEquipment> findByPitchIdAndEquipmentId(Long pitchId, Long equipmentId);

    void deleteByPitchIdAndEquipmentId(Long pitchId, Long equipmentId);

    void deleteByPitchId(Long pitchId);

    void deleteByEquipmentId(Long equipmentId);
}
