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

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.request.pitch.ReqCreatePitchDTO;
import com.example.backend.domain.request.pitch.ReqUpdatePitchDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.pitch.ResCreatePitchDTO;
import com.example.backend.domain.response.pitch.ResPitchDTO;
import com.example.backend.domain.response.pitch.ResUpdatePitchDTO;
import com.example.backend.service.PitchService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PitchController {

    private final PitchService pitchService;

    public PitchController(PitchService pitchService) {
        this.pitchService = pitchService;
    }

    @PostMapping("/pitches")
    @ApiMessage("Tạo sân mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_CREATE')")
    public ResponseEntity<ResCreatePitchDTO> createPitch(
            @Valid @RequestBody @NonNull ReqCreatePitchDTO dto) {

        ResCreatePitchDTO res = this.pitchService.createPitch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/pitches")
    @ApiMessage("Lấy danh sách sân")
    // @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllPitches(
            @Filter Specification<Pitch> spec,
            @NonNull Pageable pageable) {

        return ResponseEntity.ok(
                this.pitchService.getAllPitches(spec, pageable));
    }

    @GetMapping("/pitches/{id}")
    @ApiMessage("Lấy thông tin sân theo ID")
    // @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_VIEW_DETAIL')")
    public ResponseEntity<ResPitchDTO> getPitchById(
            @PathVariable("id") @NonNull Long id)
            throws IdInvalidException {

        Pitch pitch = this.pitchService.getPitchById(id);
        ResPitchDTO res = this.pitchService.convertToResPitchDTO(pitch);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/pitches/{id}")
    @ApiMessage("Cập nhật thông tin sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_UPDATE')")
    public ResponseEntity<ResUpdatePitchDTO> updatePitch(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdatePitchDTO dto)
            throws IdInvalidException {

        ResUpdatePitchDTO res = this.pitchService.updatePitch(id, dto);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/pitches/{id}")
    @ApiMessage("Xóa sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_DELETE')")
    public ResponseEntity<Void> deletePitch(
            @PathVariable("id") @NonNull Long id)
            throws IdInvalidException {

        this.pitchService.deletePitch(id);
        return ResponseEntity.ok().build();
    }
}
