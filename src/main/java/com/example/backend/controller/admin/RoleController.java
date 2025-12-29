package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.request.role.ReqAssignPermissionsToRoleDTO;
import com.example.backend.domain.request.role.ReqCreateRoleDTO;
import com.example.backend.domain.request.role.ReqUpdateRoleDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.role.ResCreateRoleDTO;
import com.example.backend.domain.response.role.ResRoleDetailDTO;
import com.example.backend.domain.response.role.ResRoleListDTO;
import com.example.backend.domain.response.role.ResUpdateRoleDTO;
import com.example.backend.service.RoleService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @ApiMessage("Tạo vai trò mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ResCreateRoleDTO> createRole(@Valid @RequestBody ReqCreateRoleDTO dto)
            throws NameInvalidException {
        ResCreateRoleDTO res = this.roleService.createRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/roles")
    @ApiMessage("Lấy danh sách tất cả vai trò")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllRoles(
            @Filter Specification<Role> spec, Pageable pageable) {
        return ResponseEntity.ok(this.roleService.getAllRoles(spec, pageable));
    }

    @GetMapping("/roles/{id}")
    @ApiMessage("Lấy thông tin vai trò theo ID")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_VIEW_DETAIL')")
    public ResponseEntity<ResRoleDetailDTO> getRoleById(@PathVariable("id") Long id) throws IdInvalidException {
        Role role = this.roleService.getRoleById(id);
        return ResponseEntity.ok(this.roleService.convertToResRoleDetailsDTO(role));
    }

    @PutMapping("/roles/{id}")
    @ApiMessage("Cập nhật thông tin vai trò")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ResUpdateRoleDTO> updateRole(@PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdateRoleDTO dto)
            throws IdInvalidException, NameInvalidException {

        ResUpdateRoleDTO res = this.roleService.updateRole(id, dto);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Xóa vai trò")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") Long id) throws IdInvalidException {
        this.roleService.deleteRole(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/roles/{id}/assign-permissions")
    @ApiMessage("Gán danh sách permission cho role")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ROLE_ASSIGN_PERMISSION')")
    public ResponseEntity<ResRoleListDTO> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody ReqAssignPermissionsToRoleDTO req) throws IdInvalidException {

        return ResponseEntity.ok(
                this.roleService.assignPermissionsToRole(id, req.getPermissionIds()));
    }

}
