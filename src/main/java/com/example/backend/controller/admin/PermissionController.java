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

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.request.permission.ReqCreatePermissionDTO;
import com.example.backend.domain.request.permission.ReqUpdatePermissionDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.permission.ResCreatePermissionDTO;
import com.example.backend.domain.response.permission.ResPermissionDTO;
import com.example.backend.domain.response.permission.ResUpdatePermissionDTO;
import com.example.backend.service.PermissionService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Tạo quyền mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<ResCreatePermissionDTO> createPermission(
            @Valid @RequestBody @NonNull ReqCreatePermissionDTO dto)
            throws NameInvalidException {

        ResCreatePermissionDTO res = this.permissionService.createPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/permissions")
    @ApiMessage("Lấy danh sách tất cả quyền")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PERMISSION_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(
            @Filter Specification<Permission> spec, @NonNull Pageable pageable) {

        return ResponseEntity.ok(
                this.permissionService.getAllPermissions(spec, pageable));
    }

    @GetMapping("/permissions/{id}")
    @ApiMessage("Lấy thông tin quyền theo ID")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PERMISSION_VIEW_DETAIL')")
    public ResponseEntity<ResPermissionDTO> getPermissionById(
            @PathVariable("id") @NonNull Long id)
            throws IdInvalidException {

        Permission permission = this.permissionService.getPermissionById(id);
        return ResponseEntity.ok(this.permissionService.convertToResPermissionDTO(permission));
    }

    @PutMapping("/permissions/{id}")
    @ApiMessage("Cập nhật thông tin quyền")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PERMISSION_UPDATE')")
    public ResponseEntity<ResUpdatePermissionDTO> updatePermission(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdatePermissionDTO dto)
            throws IdInvalidException, NameInvalidException {

        ResUpdatePermissionDTO res = this.permissionService.updatePermission(id, dto);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Xóa quyền")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<Void> deletePermission(
            @PathVariable("id") @NonNull Long id)
            throws IdInvalidException {

        this.permissionService.deletePermission(id);
        return ResponseEntity.ok().build();
    }
}
