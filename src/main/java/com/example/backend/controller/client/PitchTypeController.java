package com.example.backend.controller.client;

import java.util.List;

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

import com.example.backend.domain.request.pitch.ReqPitchTypeDTO;
import com.example.backend.domain.response.pitch.ResPitchTypeDTO;
import com.example.backend.service.PitchTypeService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PitchTypeController {

    private final PitchTypeService pitchTypeService;

    public PitchTypeController(PitchTypeService pitchTypeService) {
        this.pitchTypeService = pitchTypeService;
    }

    @GetMapping("/pitch-types")
    @ApiMessage("Lấy danh sách loại sân")
    public ResponseEntity<List<ResPitchTypeDTO>> getPitchTypes() {
        return ResponseEntity.ok(pitchTypeService.getAllPitchTypes());
    }

    @PostMapping("/pitch-types")
    @ApiMessage("Tạo loại sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_CREATE') or hasAuthority('PITCH_UPDATE')")
    public ResponseEntity<ResPitchTypeDTO> createPitchType(@Valid @RequestBody ReqPitchTypeDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pitchTypeService.createPitchType(req));
    }

    @PutMapping("/pitch-types/{id}")
    @ApiMessage("Cập nhật loại sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_UPDATE')")
    public ResponseEntity<ResPitchTypeDTO> updatePitchType(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqPitchTypeDTO req) throws IdInvalidException {
        return ResponseEntity.ok(pitchTypeService.updatePitchType(id, req));
    }

    @DeleteMapping("/pitch-types/{id}")
    @ApiMessage("Xóa loại sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_DELETE')")
    public ResponseEntity<Void> deletePitchType(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        pitchTypeService.deletePitchType(id);
        return ResponseEntity.ok().build();
    }
}
