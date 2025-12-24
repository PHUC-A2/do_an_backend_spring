package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Permission> createPermission(
            @Valid @RequestBody ReqCreatePermissionDTO dto)
            throws NameInvalidException {

        Permission permission = this.permissionService.createPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @GetMapping("/permissions")
    @ApiMessage("Lấy danh sách tất cả quyền")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(
            @Filter Specification<Permission> spec, Pageable pageable) {

        return ResponseEntity.ok(
                this.permissionService.getAllPermissions(spec, pageable));
    }

    @GetMapping("/permissions/{id}")
    @ApiMessage("Lấy thông tin quyền theo ID")
    public ResponseEntity<Permission> getPermissionById(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        Permission permission = this.permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @PutMapping("/permissions/{id}")
    @ApiMessage("Cập nhật thông tin quyền")
    public ResponseEntity<Permission> updatePermission(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdatePermissionDTO dto)
            throws IdInvalidException, NameInvalidException {

        Permission permission = this.permissionService.updatePermission(id, dto);
        return ResponseEntity.ok(permission);
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Xóa quyền")
    public ResponseEntity<Void> deletePermission(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        this.permissionService.deletePermission(id);
        return ResponseEntity.ok().build();
    }
}
