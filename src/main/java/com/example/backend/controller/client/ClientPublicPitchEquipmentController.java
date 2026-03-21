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

    /** Toàn bộ thiết bị gắn sân (cố định + cho mượn) — dùng cho trang chi tiết / mô tả sân. */
    @GetMapping("/pitches/{pitchId}/pitch-equipments")
    @ApiMessage("Danh sách thiết bị gắn sân (đầy đủ)")
    public ResponseEntity<List<ResPitchEquipmentDTO>> getCatalog(@PathVariable @NonNull Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(pitchEquipmentService.getPublicCatalogByPitchId(pitchId));
    }

    /** Chỉ thiết bị lưu động (cho mượn), ACTIVE và còn hàng — dùng khi đặt sân / mượn kèm. */
    @GetMapping("/pitches/{pitchId}/pitch-equipments/borrowable")
    @ApiMessage("Thiết bị có thể mượn thêm (lưu động)")
    public ResponseEntity<List<ResPitchEquipmentDTO>> getBorrowable(@PathVariable @NonNull Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(pitchEquipmentService.getPublicBorrowableByPitchId(pitchId));
    }
}
