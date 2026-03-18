package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.pitchequipment.ReqUpsertPitchEquipmentDTO;
import com.example.backend.domain.response.pitchequipment.ResPitchEquipmentDTO;
import com.example.backend.service.PitchEquipmentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PitchEquipmentController {

    private final PitchEquipmentService pitchEquipmentService;

    public PitchEquipmentController(PitchEquipmentService pitchEquipmentService) {
        this.pitchEquipmentService = pitchEquipmentService;
    }

    @GetMapping("/pitches/{pitchId}/pitch-equipments")
    @ApiMessage("Lấy danh sách thiết bị gắn với sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_VIEW_DETAIL') or hasAuthority('PITCH_VIEW_LIST')")
    public ResponseEntity<List<ResPitchEquipmentDTO>> getByPitchId(@PathVariable @NonNull Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(pitchEquipmentService.getByPitchId(pitchId));
    }

    @PutMapping("/pitches/{pitchId}/pitch-equipments")
    @ApiMessage("Thêm hoặc cập nhật thiết bị gắn với sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_UPDATE')")
    public ResponseEntity<ResPitchEquipmentDTO> upsert(
            @PathVariable @NonNull Long pitchId,
            @Valid @RequestBody ReqUpsertPitchEquipmentDTO req) throws IdInvalidException {
        return ResponseEntity.ok(pitchEquipmentService.upsertPitchEquipment(pitchId, req));
    }

    @DeleteMapping("/pitches/{pitchId}/pitch-equipments/{equipmentId}")
    @ApiMessage("Xóa thiết bị khỏi sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_UPDATE')")
    public ResponseEntity<Void> delete(
            @PathVariable @NonNull Long pitchId,
            @PathVariable @NonNull Long equipmentId) throws IdInvalidException {
        pitchEquipmentService.deletePitchEquipment(pitchId, equipmentId);
        return ResponseEntity.ok().build();
    }
}
