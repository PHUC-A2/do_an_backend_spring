package com.example.backend.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.pitchequipment.ResPitchEquipmentDTO;
import com.example.backend.service.PitchEquipmentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/client/public")
public class ClientPublicPitchEquipmentController {

    private final PitchEquipmentService pitchEquipmentService;

    public ClientPublicPitchEquipmentController(PitchEquipmentService pitchEquipmentService) {
        this.pitchEquipmentService = pitchEquipmentService;
    }

    @GetMapping("/pitches/{pitchId}/pitch-equipments")
    @ApiMessage("Lấy danh sách thiết bị cố định theo sân")
    public ResponseEntity<List<ResPitchEquipmentDTO>> getByPitchId(@PathVariable @NonNull Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(pitchEquipmentService.getByPitchId(pitchId));
    }
}
