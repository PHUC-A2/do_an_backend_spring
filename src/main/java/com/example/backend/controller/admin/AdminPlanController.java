package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.plan.ReqCreatePlanDTO;
import com.example.backend.domain.request.plan.ReqPlanAssignPermissionsDTO;
import com.example.backend.domain.request.plan.ReqUpdatePlanDTO;
import com.example.backend.domain.response.plan.ResPlanDTO;
import com.example.backend.service.PlanService;
import com.example.backend.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class AdminPlanController {

    private final PlanService planService;

    @GetMapping
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Danh sách gói dịch vụ")
    public ResponseEntity<List<ResPlanDTO>> list() {
        return ResponseEntity.ok(planService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Tạo gói dịch vụ")
    public ResponseEntity<ResPlanDTO> create(@Valid @RequestBody ReqCreatePlanDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Cập nhật gói dịch vụ")
    public ResponseEntity<ResPlanDTO> update(
            @PathVariable("id") Long id, @Valid @RequestBody ReqUpdatePlanDTO body) {
        return ResponseEntity.ok(planService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Xóa gói dịch vụ")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        planService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Gán quyền cho gói")
    public ResponseEntity<Void> assignPermissions(
            @PathVariable("id") Long id, @Valid @RequestBody ReqPlanAssignPermissionsDTO body) {
        planService.assignPermissions(id, body);
        return ResponseEntity.noContent().build();
    }

    /** Gán toàn bộ quyền trong catalog cho gói (chủ cửa hàng theo gói ≈ đủ quyền quản trị nội bộ). */
    @PostMapping("/{id}/permissions/full-catalog")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Gán toàn bộ quyền catalog cho gói")
    public ResponseEntity<Void> replaceWithFullCatalog(@PathVariable("id") Long id) {
        planService.replacePlanPermissionsWithFullCatalog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permission-names")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Tên quyền thuộc gói")
    public ResponseEntity<List<String>> permissionNames(@PathVariable("id") Long id) {
        return ResponseEntity.ok(planService.getPermissionNamesForPlan(id));
    }
}
