package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.PitchService;
import com.example.backend.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

import lombok.RequiredArgsConstructor;

/**
 * Danh sách sân theo tenant (dashboard admin). Marketplace dùng {@code GET /api/v1/pitches} (không lọc).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPitchListController {

    private final PitchService pitchService;

    @GetMapping("/pitches")
    @ApiMessage("Lấy danh sách sân theo tenant (admin)")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PITCH_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getPitchesForTenant(
            @Filter Specification<Pitch> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(pitchService.getAllPitchesForCurrentTenant(spec, pageable));
    }
}
