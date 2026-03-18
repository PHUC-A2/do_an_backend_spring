package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.request.equipment.ReqCreateEquipmentDTO;
import com.example.backend.domain.request.equipment.ReqUpdateEquipmentDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.equipment.ResEquipmentDTO;
import com.example.backend.service.EquipmentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/equipments")
    @ApiMessage("Tạo thiết bị mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('EQUIPMENT_CREATE')")
    public ResponseEntity<ResEquipmentDTO> createEquipment(
            @Valid @RequestBody @NonNull ReqCreateEquipmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(dto));
    }

    @GetMapping("/equipments")
    @ApiMessage("Lấy danh sách thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('EQUIPMENT_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllEquipments(
            @Filter Specification<Equipment> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(equipmentService.getAllEquipments(spec, pageable));
    }

    @GetMapping("/equipments/{id}")
    @ApiMessage("Lấy thông tin thiết bị theo ID")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('EQUIPMENT_VIEW_DETAIL')")
    public ResponseEntity<ResEquipmentDTO> getEquipmentById(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        Equipment equipment = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(equipmentService.convertToResEquipmentDTO(equipment));
    }

    @PutMapping("/equipments/{id}")
    @ApiMessage("Cập nhật thông tin thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('EQUIPMENT_UPDATE')")
    public ResponseEntity<ResEquipmentDTO> updateEquipment(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateEquipmentDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, dto));
    }

    @DeleteMapping("/equipments/{id}")
    @ApiMessage("Xóa thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('EQUIPMENT_DELETE')")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok().build();
    }
}
