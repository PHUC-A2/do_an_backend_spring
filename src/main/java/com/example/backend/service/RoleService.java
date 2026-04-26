package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.request.role.ReqCreateRoleDTO;
import com.example.backend.domain.request.role.ReqUpdateRoleDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.permission.ResPermissionNestedDTO;
import com.example.backend.domain.response.role.ResCreateRoleDTO;
import com.example.backend.domain.response.role.ResRoleDetailDTO;
import com.example.backend.domain.response.role.ResRoleListDTO;
import com.example.backend.domain.response.role.ResUpdateRoleDTO;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.TenantRepository;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.RoleSecurityUtil;
import com.example.backend.util.SecurityRbac;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            TenantRepository tenantRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public ResCreateRoleDTO createRole(@NonNull ReqCreateRoleDTO roleReq) throws NameInvalidException {
        if (SecurityRbac.hasAllAuthority()) {
            boolean nameTaken = this.roleRepository.existsByNameAndTenantIsNull(roleReq.getName());
            if (nameTaken) {
                throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại (role hệ thống)");
            }
        } else {
            long tid = TenantContext.requireCurrentTenantId();
            if (tid == TenantService.DEFAULT_TENANT_ID) {
                throw new BadRequestException(
                        "Chỉ tạo role trong ngữ cảnh shop đã gán. Không tạo role tại tenant hệ thống mặc định.");
            }
            if (isReservedSystemRoleName(roleReq.getName())) {
                throw new NameInvalidException("Tên này dành cho hệ thống (ADMIN, VIEW, …)");
            }
            if (this.roleRepository.existsByNameAndTenant_Id(roleReq.getName(), tid)) {
                throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại trong shop này");
            }
        }

        Role role = convertToReqCreateRoleDTO(roleReq);
        if (SecurityRbac.hasAllAuthority()) {
            role.setTenant(null);
        } else {
            long tid = TenantContext.requireCurrentTenantId();
            role.setTenant(tenantRepository.getReferenceById(tid));
        }

        Role saveRole = this.roleRepository.save(role);
        return this.convertToResCreateRoleDTO(saveRole);
    }

    public ResultPaginationDTO getAllRoles(@Nullable Specification<Role> spec, @NonNull Pageable pageable) {
        Specification<Role> scope = buildTenantScopeSpecification();
        Specification<Role> finalSpec = (spec == null) ? scope : spec.and(scope);

        Page<Role> pageRole = this.roleRepository.findAll(finalSpec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageRole.getTotalPages());
        mt.setTotal(pageRole.getTotalElements());

        rs.setMeta(mt);

        List<ResRoleListDTO> resList = new ArrayList<>();
        for (Role role : pageRole.getContent()) {
            resList.add(this.convertToResRoleListDTO(role));
        }

        rs.setResult(resList);
        return rs;
    }

    private static Specification<Role> buildTenantScopeSpecification() {
        if (SecurityRbac.hasAllAuthority()) {
            return (root, q, cb) -> cb.isTrue(cb.literal(true));
        }
        long tid = TenantContext.requireCurrentTenantId();
        if (tid == TenantService.DEFAULT_TENANT_ID) {
            return (root, q, cb) -> cb.equal(root.get("name"), RoleSecurityUtil.SYSTEM_DEFAULT_VIEW_NAME);
        }
        return (root, q, cb) -> cb.or(
                cb.equal(root.get("tenant").get("id"), tid),
                cb.and(
                        cb.isNull(root.get("tenant")),
                        cb.equal(root.get("name"), RoleSecurityUtil.SYSTEM_DEFAULT_VIEW_NAME)));
    }

    public Role getRoleById(Long id) throws IdInvalidException {
        Role role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy Role với ID = " + id));
        assertCurrentPrincipalCanAccessRole(role);
        return role;
    }

    private void assertCurrentPrincipalCanAccessRole(Role role) throws IdInvalidException {
        if (SecurityRbac.hasAllAuthority()) {
            return;
        }
        if (role.getTenant() == null) {
            if (RoleSecurityUtil.isGlobalSystemViewRole(role)) {
                return;
            }
            throw new IdInvalidException("Không tìm thấy Role với ID = " + role.getId());
        }
        long tid = TenantContext.requireCurrentTenantId();
        if (role.getTenant().getId() != tid) {
            throw new IdInvalidException("Không tìm thấy Role với ID = " + role.getId());
        }
    }

    @Transactional
    public ResUpdateRoleDTO updateRole(Long id, ReqUpdateRoleDTO roleReq)
            throws IdInvalidException, NameInvalidException {

        Role role = getRoleById(id);

        if (RoleSecurityUtil.isGlobalSystemAllRole(role) || RoleSecurityUtil.isGlobalSystemViewRole(role)) {
            if (!SecurityRbac.hasAllAuthority()) {
                throw new BadRequestException("Chỉ quản trị hệ thống được sửa role mặc định toàn hệ thống");
            }
        }

        if (SecurityRbac.hasAllAuthority()) {
            if (!role.getName().equals(roleReq.getName()) && this.roleRepository.existsByNameAndTenantIsNullAndIdNot(
                    roleReq.getName(), id)) {
                throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại (role hệ thống)");
            }
        } else {
            if (isReservedSystemRoleName(roleReq.getName())) {
                throw new NameInvalidException("Tên này dành cho hệ thống (ADMIN, VIEW, …)");
            }
            if (!role.getName().equals(roleReq.getName())) {
                long tid = role.getTenant() != null ? role.getTenant().getId() : TenantContext.requireCurrentTenantId();
                if (this.roleRepository.existsByNameAndTenant_IdAndIdNot(roleReq.getName(), tid, id)) {
                    throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại trong shop này");
                }
            }
        }

        role.setName(roleReq.getName());
        role.setDescription(roleReq.getDescription());

        Role updateRole = this.roleRepository.save(role);

        return this.convertToResUpdateRoleDTO(updateRole);
    }

    @Transactional
    public void deleteRole(@NonNull Long id) throws IdInvalidException {
        Role role = getRoleById(id);
        if (RoleSecurityUtil.isGlobalSystemAllRole(role) || RoleSecurityUtil.isGlobalSystemViewRole(role)) {
            throw new BadRequestException("Không xóa role hệ thống ADMIN / VIEW");
        }
        this.roleRepository.deleteById(id);
    }

    @NonNull
    public Role convertToReqCreateRoleDTO(@NonNull ReqCreateRoleDTO req) {
        Role r = new Role();
        r.setName(req.getName());
        r.setDescription(req.getDescription());
        return r;
    }

    // entity -> res get
    public ResRoleListDTO convertToResRoleListDTO(Role role) {

        ResRoleListDTO res = new ResRoleListDTO();

        res.setId(role.getId());
        res.setTenantId(role.getTenant() == null ? null : role.getTenant().getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setCreatedBy(role.getCreatedBy());
        res.setUpdatedAt(role.getUpdatedAt());
        res.setUpdatedBy(role.getUpdatedBy());

        return res;
    }

    // entity -> res get details
    public ResRoleDetailDTO convertToResRoleDetailsDTO(Role role) {

        ResRoleDetailDTO res = new ResRoleDetailDTO();

        res.setId(role.getId());
        res.setTenantId(role.getTenant() == null ? null : role.getTenant().getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setCreatedBy(role.getCreatedBy());
        res.setUpdatedAt(role.getUpdatedAt());
        res.setUpdatedBy(role.getUpdatedBy());

        // map permissions
        res.setPermissions(
                role.getPermissions()
                        .stream()
                        .map(p -> {
                            ResPermissionNestedDTO dto = new ResPermissionNestedDTO();
                            dto.setId(p.getId());
                            dto.setName(p.getName());
                            dto.setDescription(p.getDescription());
                            return dto;
                        })
                        .toList());

        return res;
    }

    public ResCreateRoleDTO convertToResCreateRoleDTO(Role role) {

        ResCreateRoleDTO res = new ResCreateRoleDTO();

        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setCreatedBy(role.getCreatedBy());
        return res;
    }

    public ResUpdateRoleDTO convertToResUpdateRoleDTO(Role role) {

        ResUpdateRoleDTO res = new ResUpdateRoleDTO();

        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setUpdatedAt(role.getUpdatedAt());
        res.setUpdatedBy(role.getUpdatedBy());
        return res;
    }

    // gắn permisison cho role
    @Transactional
    public ResRoleListDTO assignPermissionsToRole(
            Long roleId,
            List<Long> permissionIds) throws IdInvalidException {

        Role role = this.getRoleById(roleId);

        if (!SecurityRbac.hasAllAuthority()
                && (RoleSecurityUtil.isGlobalSystemAllRole(role) || RoleSecurityUtil.isGlobalSystemViewRole(role))) {
            throw new BadRequestException("Chỉ quản trị hệ thống được gán permission cho role mặc định toàn hệ thống");
        }

        // 3. Cho phép clear permission
        if (permissionIds.isEmpty()) {
            role.getPermissions().clear();
            roleRepository.save(role);
            return convertToResRoleListDTO(role);
        }

        // 4. Check permission tồn tại
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new IdInvalidException("Có permission không tồn tại");
        }

        // 5. Sync (giống Laravel sync)
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);

        Role savedRole = roleRepository.save(role);
        return convertToResRoleListDTO(savedRole);
    }

    private static boolean isReservedSystemRoleName(String name) {
        if (name == null) {
            return true;
        }
        String n = name.trim();
        return "ADMIN".equalsIgnoreCase(n) || "VIEW".equalsIgnoreCase(n) || "ALL".equalsIgnoreCase(n);
    }
}
