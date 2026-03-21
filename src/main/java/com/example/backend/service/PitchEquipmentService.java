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
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;
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

    /**
     * Public: toàn bộ thiết bị gắn sân (cố định + cho mượn) để hiển thị chi tiết sân.
     */
    public List<ResPitchEquipmentDTO> getPublicCatalogByPitchId(@NonNull Long pitchId) throws IdInvalidException {
        return getByPitchId(pitchId);
    }

    /**
     * Danh sách thiết bị có thể mượn thêm khi đặt sân: chỉ MOVABLE, ACTIVE, còn hàng trong kho.
     */
    public List<ResPitchEquipmentDTO> getPublicBorrowableByPitchId(@NonNull Long pitchId) throws IdInvalidException {
        return getByPitchId(pitchId).stream()
                .filter(dto -> dto.getEquipmentMobility() == EquipmentMobilityEnum.MOVABLE)
                .filter(dto -> dto.getEquipmentStatus() == EquipmentStatusEnum.ACTIVE
                        && dto.getEquipmentAvailableQuantity() != null
                        && dto.getEquipmentAvailableQuantity() > 0)
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
        item.setEquipmentMobility(req.getEquipmentMobility() != null
                ? req.getEquipmentMobility()
                : EquipmentMobilityEnum.FIXED);

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
        Equipment e = item.getEquipment();
        ResPitchEquipmentDTO res = new ResPitchEquipmentDTO();
        res.setId(item.getId());
        res.setPitchId(item.getPitch().getId());
        res.setEquipmentId(e.getId());
        res.setEquipmentName(e.getName());
        res.setEquipmentImageUrl(e.getImageUrl());
        res.setQuantity(item.getQuantity());
        res.setSpecification(item.getSpecification());
        res.setNote(item.getNote());
        res.setEquipmentMobility(item.getEquipmentMobility() != null
                ? item.getEquipmentMobility()
                : EquipmentMobilityEnum.FIXED);
        res.setEquipmentAvailableQuantity(e.getAvailableQuantity());
        res.setEquipmentStatus(e.getStatus());
        res.setEquipmentConditionNote(e.getConditionNote());
        return res;
    }
}
