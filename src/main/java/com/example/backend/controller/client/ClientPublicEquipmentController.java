package com.example.backend.controller.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Equipment;
import com.example.backend.domain.response.equipment.ResEquipmentDTO;
import com.example.backend.repository.EquipmentRepository;
import com.example.backend.service.EquipmentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;

@RestController
@RequestMapping("/api/v1/client/public")
public class ClientPublicEquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;

    public ClientPublicEquipmentController(
            EquipmentRepository equipmentRepository,
            EquipmentService equipmentService) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentService = equipmentService;
    }

    // Lấy danh sách thiết bị ACTIVE để client xem và chọn mượn
    @GetMapping("/equipments")
    @ApiMessage("Lấy danh sách thiết bị có thể mượn")
    public ResponseEntity<List<ResEquipmentDTO>> getActiveEquipments() {
        List<Equipment> list = equipmentRepository.findAll().stream()
                .filter(e -> e.getStatus() == EquipmentStatusEnum.ACTIVE && e.getAvailableQuantity() > 0)
                .collect(Collectors.toList());

        List<ResEquipmentDTO> result = list.stream()
                .map(equipmentService::convertToResEquipmentDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
