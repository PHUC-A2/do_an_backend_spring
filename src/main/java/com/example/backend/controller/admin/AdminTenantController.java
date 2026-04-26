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

import com.example.backend.domain.request.tenant.ReqCreateTenantAdminDTO;
import com.example.backend.domain.request.tenant.ReqUpdateTenantAdminDTO;
import com.example.backend.domain.response.common.MessageResponse;
import com.example.backend.domain.response.tenant.ResTenantAdminDTO;
import com.example.backend.service.TenantService;
import com.example.backend.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
public class AdminTenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Danh sách tenant (quản trị hệ thống)")
    public ResponseEntity<List<ResTenantAdminDTO>> listAll() {
        return ResponseEntity.ok(tenantService.listAllTenantsForSystemAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Chi tiết tenant (quản trị hệ thống)")
    public ResponseEntity<ResTenantAdminDTO> getOne(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tenantService.getTenantByIdForAdmin(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Tạo tenant (quản trị hệ thống)")
    public ResponseEntity<ResTenantAdminDTO> create(@Valid @RequestBody ReqCreateTenantAdminDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenantByAdmin(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Cập nhật tenant (quản trị hệ thống)")
    public ResponseEntity<ResTenantAdminDTO> update(
            @PathVariable("id") Long id, @Valid @RequestBody ReqUpdateTenantAdminDTO body) {
        return ResponseEntity.ok(tenantService.updateTenantByAdmin(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Xóa tenant (quản trị hệ thống)")
    public ResponseEntity<MessageResponse> delete(@PathVariable("id") Long id) {
        tenantService.deleteTenantByAdmin(id);
        return ResponseEntity.ok(new MessageResponse("Đã xóa tenant"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Duyệt yêu cầu chủ sân")
    public ResponseEntity<MessageResponse> approve(@PathVariable("id") Long id) {
        tenantService.approveTenant(id);
        return ResponseEntity.ok(new MessageResponse("Đã duyệt tenant"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Từ chối yêu cầu chủ sân")
    public ResponseEntity<MessageResponse> reject(@PathVariable("id") Long id) {
        tenantService.rejectTenant(id);
        return ResponseEntity.ok(new MessageResponse("Đã từ chối yêu cầu"));
    }
}
