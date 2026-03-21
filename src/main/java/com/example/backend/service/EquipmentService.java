package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.request.equipment.ReqCreateEquipmentDTO;
import com.example.backend.domain.request.equipment.ReqUpdateEquipmentDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.equipment.ResEquipmentDTO;
import com.example.backend.repository.EquipmentRepository;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.util.error.IdInvalidException;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final PitchEquipmentRepository pitchEquipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository,
            PitchEquipmentRepository pitchEquipmentRepository) {
        this.equipmentRepository = equipmentRepository;
        this.pitchEquipmentRepository = pitchEquipmentRepository;
    }

    public ResEquipmentDTO createEquipment(@NonNull ReqCreateEquipmentDTO req) {
        Equipment equipment = new Equipment();
        equipment.setName(req.getName());
        equipment.setDescription(req.getDescription());
        equipment.setTotalQuantity(req.getTotalQuantity());
        equipment.setAvailableQuantity(req.getTotalQuantity()); // ban đầu availableQuantity = totalQuantity
        equipment.setPrice(req.getPrice());
        equipment.setImageUrl(req.getImageUrl());
        equipment.setStatus(req.getStatus() != null ? req.getStatus()
                : com.example.backend.util.constant.equipment.EquipmentStatusEnum.ACTIVE);
        equipment.setConditionNote(req.getConditionNote());

        Equipment saved = equipmentRepository.save(equipment);
        return convertToResEquipmentDTO(saved);
    }

    public ResultPaginationDTO getAllEquipments(Specification<Equipment> spec, @NonNull Pageable pageable) {
        Page<Equipment> page = equipmentRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        rs.setMeta(mt);

        List<ResEquipmentDTO> resList = new ArrayList<>();
        for (Equipment e : page.getContent()) {
            resList.add(convertToResEquipmentDTO(e));
        }
        rs.setResult(resList);
        return rs;
    }

    public Equipment getEquipmentById(@NonNull Long id) throws IdInvalidException {
        Optional<Equipment> opt = equipmentRepository.findById(id);
        if (opt.isPresent())
            return opt.get();
        throw new IdInvalidException("Không tìm thấy thiết bị với ID = " + id);
    }

    public ResEquipmentDTO updateEquipment(@NonNull Long id, ReqUpdateEquipmentDTO req) throws IdInvalidException {
        Equipment equipment = getEquipmentById(id);

        // Tính lại availableQuantity khi totalQuantity thay đổi
        int diff = req.getTotalQuantity() - equipment.getTotalQuantity();
        int newAvailable = equipment.getAvailableQuantity() + diff;
        if (newAvailable < 0)
            newAvailable = 0;

        equipment.setName(req.getName());
        equipment.setDescription(req.getDescription());
        equipment.setTotalQuantity(req.getTotalQuantity());
        equipment.setAvailableQuantity(newAvailable);
        equipment.setPrice(req.getPrice());
        equipment.setImageUrl(req.getImageUrl());
        equipment.setStatus(req.getStatus());
        equipment.setConditionNote(req.getConditionNote());

        Equipment saved = equipmentRepository.save(equipment);
        return convertToResEquipmentDTO(saved);
    }

    public void deleteEquipment(@NonNull Long id) throws IdInvalidException {
        getEquipmentById(id);
        pitchEquipmentRepository.deleteByEquipmentId(id);
        equipmentRepository.deleteById(id);
    }

    public ResEquipmentDTO convertToResEquipmentDTO(Equipment equipment) {
        ResEquipmentDTO res = new ResEquipmentDTO();
        res.setId(equipment.getId());
        res.setName(equipment.getName());
        res.setDescription(equipment.getDescription());
        res.setTotalQuantity(equipment.getTotalQuantity());
        res.setAvailableQuantity(equipment.getAvailableQuantity());
        res.setPrice(equipment.getPrice());
        res.setImageUrl(equipment.getImageUrl());
        res.setStatus(equipment.getStatus());
        res.setConditionNote(equipment.getConditionNote());
        res.setCreatedAt(equipment.getCreatedAt());
        res.setUpdatedAt(equipment.getUpdatedAt());
        res.setCreatedBy(equipment.getCreatedBy());
        res.setUpdatedBy(equipment.getUpdatedBy());
        return res;
    }
}
