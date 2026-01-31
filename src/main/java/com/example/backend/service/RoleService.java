package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

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
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public ResCreateRoleDTO createRole(@NonNull ReqCreateRoleDTO roleReq) throws NameInvalidException {

        boolean isNameExists = this.roleRepository.existsByName(roleReq.getName());
        if (isNameExists) {
            throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại");
        }

        Role role = convertToReqCreateRoleDTO(roleReq);
        Role saveRole = this.roleRepository.save(role);

        return this.convertToResCreateRoleDTO(saveRole);
    }

    public ResultPaginationDTO getAllRoles(@Nullable Specification<Role> spec, @NonNull Pageable pageable) {

        Page<Role> pageRole = this.roleRepository.findAll(spec, pageable);
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

    // public Role getRoleById(Long id) throws IdInvalidException {
    // Optional<Role> roleOptional = this.roleRepository.findById(id);
    // if (roleOptional.isPresent()) {
    // return roleOptional.get();
    // } else {
    // throw new IdInvalidException("Không tìm thấy Role với ID = " + id);
    // }
    // }

    public Role getRoleById(Long id) throws IdInvalidException {
        return roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy Role với ID = " + id));
    }

    public ResUpdateRoleDTO updateRole(Long id, ReqUpdateRoleDTO roleReq)
            throws IdInvalidException, NameInvalidException {

        Role role = getRoleById(id);

        // Chỉ check khi đổi name
        if (!role.getName().equals(roleReq.getName())
                && roleRepository.existsByName(roleReq.getName())) {
            throw new NameInvalidException(
                    "Name '" + roleReq.getName() + "' đã tồn tại");
        }

        role.setName(roleReq.getName());
        role.setDescription(roleReq.getDescription());

        Role updateRole = this.roleRepository.save(role);

        return this.convertToResUpdateRoleDTO(updateRole);
    }

    public void deleteRole(@NonNull Long id) throws IdInvalidException {
        // Role role = this.getRoleById(id);
        this.getRoleById(id);
        this.roleRepository.deleteById(id);
    }

    // convert req create
    @NonNull
    public Role convertToReqCreateRoleDTO(@NonNull ReqCreateRoleDTO req) {
        Role role = new Role();
        role.setName(req.getName());
        role.setDescription(req.getDescription());

        return role;
    }

    // entity -> res get
    public ResRoleListDTO convertToResRoleListDTO(Role role) {

        ResRoleListDTO res = new ResRoleListDTO();

        res.setId(role.getId());
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
    public ResRoleListDTO assignPermissionsToRole(
            Long roleId,
            List<Long> permissionIds) throws IdInvalidException {

        // 1. Check null
        // if (permissionIds == null) {
        //     throw new IdInvalidException("permissionIds không được null");
        // }

        // 2. Check role tồn tại
        Role role = this.getRoleById(roleId);

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

}
