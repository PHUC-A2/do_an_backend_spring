package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.PitchEquipment;

@Repository
public interface PitchEquipmentRepository extends JpaRepository<PitchEquipment, Long> {

    List<PitchEquipment> findByPitchIdOrderByIdAsc(Long pitchId);

    Optional<PitchEquipment> findByPitchIdAndEquipmentId(Long pitchId, Long equipmentId);

    void deleteByPitchIdAndEquipmentId(Long pitchId, Long equipmentId);

    void deleteByPitchId(Long pitchId);

    void deleteByEquipmentId(Long equipmentId);
}
