package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.PitchEquipment;
import com.example.backend.domain.request.pitchequipment.ReqUpsertPitchEquipmentDTO;
import com.example.backend.domain.response.pitchequipment.ResPitchEquipmentDTO;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.util.error.IdInvalidException;

@Service
public class PitchEquipmentService {

    private final PitchEquipmentRepository pitchEquipmentRepository;
    private final PitchService pitchService;
    private final EquipmentService equipmentService;

    public PitchEquipmentService(
            PitchEquipmentRepository pitchEquipmentRepository,
            PitchService pitchService,
            EquipmentService equipmentService) {
        this.pitchEquipmentRepository = pitchEquipmentRepository;
        this.pitchService = pitchService;
        this.equipmentService = equipmentService;
    }

    public List<ResPitchEquipmentDTO> getByPitchId(@NonNull Long pitchId) throws IdInvalidException {
        pitchService.getPitchById(pitchId);

        return pitchEquipmentRepository.findByPitchIdOrderByIdAsc(pitchId)
                .stream()
                .map(this::convertToResPitchEquipmentDTO)
                .collect(Collectors.toList());
    }

    public ResPitchEquipmentDTO upsertPitchEquipment(
            @NonNull Long pitchId,
            @NonNull ReqUpsertPitchEquipmentDTO req) throws IdInvalidException {

        Pitch pitch = pitchService.getPitchById(pitchId);
        Equipment equipment = equipmentService.getEquipmentById(req.getEquipmentId());

        PitchEquipment item = pitchEquipmentRepository
                .findByPitchIdAndEquipmentId(pitchId, req.getEquipmentId())
                .orElseGet(PitchEquipment::new);

        item.setPitch(pitch);
        item.setEquipment(equipment);
        item.setQuantity(req.getQuantity());
        item.setSpecification(req.getSpecification());
        item.setNote(req.getNote());

        PitchEquipment saved = pitchEquipmentRepository.save(item);
        return convertToResPitchEquipmentDTO(saved);
    }

    public void deletePitchEquipment(@NonNull Long pitchId, @NonNull Long equipmentId) throws IdInvalidException {
        pitchService.getPitchById(pitchId);
        equipmentService.getEquipmentById(equipmentId);

        PitchEquipment found = pitchEquipmentRepository
                .findByPitchIdAndEquipmentId(pitchId, equipmentId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy thiết bị sân để xóa"));

        pitchEquipmentRepository.delete(found);
    }

    public ResPitchEquipmentDTO convertToResPitchEquipmentDTO(@NonNull PitchEquipment item) {
        ResPitchEquipmentDTO res = new ResPitchEquipmentDTO();
        res.setId(item.getId());
        res.setPitchId(item.getPitch().getId());
        res.setEquipmentId(item.getEquipment().getId());
        res.setEquipmentName(item.getEquipment().getName());
        res.setEquipmentImageUrl(item.getEquipment().getImageUrl());
        res.setQuantity(item.getQuantity());
        res.setSpecification(item.getSpecification());
        res.setNote(item.getNote());
        return res;
    }
}
