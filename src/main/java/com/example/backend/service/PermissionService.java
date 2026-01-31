package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.request.permission.ReqCreatePermissionDTO;
import com.example.backend.domain.request.permission.ReqUpdatePermissionDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.permission.ResCreatePermissionDTO;
import com.example.backend.domain.response.permission.ResPermissionDTO;
import com.example.backend.domain.response.permission.ResUpdatePermissionDTO;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // CREATE
    public ResCreatePermissionDTO createPermission(@NonNull ReqCreatePermissionDTO permissionReq)
            throws NameInvalidException {

        boolean isNameExists = permissionRepository.existsByName(permissionReq.getName());
        if (isNameExists) {
            throw new NameInvalidException(
                    "Name '" + permissionReq.getName() + "' đã tồn tại");
        }

        Permission permission = convertToReqCreatePermissionDTO(permissionReq);

        Permission savePermission = this.permissionRepository.save(permission);
        return this.convertToResCreatePermissionDTO(savePermission);
    }

    // GET ALL + PAGINATION
    public ResultPaginationDTO getAllPermissions(
            Specification<Permission> spec, @NonNull Pageable pageable) {

        Page<Permission> pagePermission = this.permissionRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePermission.getTotalPages());
        mt.setTotal(pagePermission.getTotalElements());

        rs.setMeta(mt);

        List<ResPermissionDTO> resList = new ArrayList<>();
        for (Permission permission : pagePermission.getContent()) {
            resList.add(this.convertToResPermissionDTO(permission));
        }

        rs.setResult(resList);

        return rs;
    }

    // GET BY ID
    public Permission getPermissionById(@NonNull Long id) throws IdInvalidException {
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);

        if (permissionOptional.isPresent()) {
            return permissionOptional.get();
        } else {
            throw new IdInvalidException(
                    "Không tìm thấy Permission với ID = " + id);
        }
    }

    // UPDATE
    public ResUpdatePermissionDTO updatePermission(
            @NonNull Long id, ReqUpdatePermissionDTO permissionReq)
            throws IdInvalidException, NameInvalidException {

        Permission permission = this.getPermissionById(id);

        // Chỉ check unique khi đổi name
        if (!permission.getName().equals(permissionReq.getName())
                && permissionRepository.existsByName(permissionReq.getName())) {
            throw new NameInvalidException(
                    "Name '" + permissionReq.getName() + "' đã tồn tại");
        }

        permission.setName(permissionReq.getName());
        permission.setDescription(permissionReq.getDescription());

        Permission updatePermission = this.permissionRepository.save(permission);
        return this.convertToResUpdatePermissionDTO(updatePermission);
    }

    // DELETE
    public void deletePermission(@NonNull Long id) throws IdInvalidException {
        // Permission permission = getPermissionById(id);
        this.getPermissionById(id);
        this.permissionRepository.deleteById(id);
    }

    // CONVERT CREATE DTO
    @NonNull
    public Permission convertToReqCreatePermissionDTO(
            @NonNull ReqCreatePermissionDTO req) {

        Permission permission = new Permission();
        permission.setId(req.getId());
        permission.setName(req.getName());
        permission.setDescription(req.getDescription());

        return permission;
    }

    // entity -> res get
    public ResPermissionDTO convertToResPermissionDTO(Permission permission) {

        ResPermissionDTO res = new ResPermissionDTO();

        res.setId(permission.getId());
        res.setName(permission.getName());
        res.setDescription(permission.getDescription());
        res.setCreatedAt(permission.getCreatedAt());
        res.setCreatedBy(permission.getCreatedBy());
        res.setUpdatedAt(permission.getUpdatedAt());
        res.setUpdatedBy(permission.getUpdatedBy());

        return res;
    }

    public ResCreatePermissionDTO convertToResCreatePermissionDTO(Permission permission) {

        ResCreatePermissionDTO res = new ResCreatePermissionDTO();

        res.setId(permission.getId());
        res.setName(permission.getName());
        res.setDescription(permission.getDescription());
        res.setCreatedAt(permission.getCreatedAt());
        res.setCreatedBy(permission.getCreatedBy());
        return res;
    }

    public ResUpdatePermissionDTO convertToResUpdatePermissionDTO(Permission permission) {

        ResUpdatePermissionDTO res = new ResUpdatePermissionDTO();

        res.setId(permission.getId());
        res.setName(permission.getName());
        res.setDescription(permission.getDescription());
        res.setUpdatedAt(permission.getUpdatedAt());
        res.setUpdatedBy(permission.getUpdatedBy());
        return res;
    }
}
