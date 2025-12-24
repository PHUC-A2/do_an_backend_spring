package com.example.backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.request.permission.ReqCreatePermissionDTO;
import com.example.backend.domain.request.permission.ReqUpdatePermissionDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
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
    public Permission createPermission(ReqCreatePermissionDTO permissionReq)
            throws NameInvalidException {

        boolean isNameExists = permissionRepository.existsByName(permissionReq.getName());
        if (isNameExists) {
            throw new NameInvalidException(
                    "Name '" + permissionReq.getName() + "' đã tồn tại");
        }

        Permission permission = convertToReqCreatePermissionDTO(permissionReq);
        return this.permissionRepository.save(permission);
    }

    // GET ALL + PAGINATION
    public ResultPaginationDTO getAllPermissions(
            Specification<Permission> spec, Pageable pageable) {

        Page<Permission> pagePermission = this.permissionRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePermission.getTotalPages());
        mt.setTotal(pagePermission.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pagePermission.getContent());

        return rs;
    }

    // GET BY ID
    public Permission getPermissionById(Long id) throws IdInvalidException {
        Optional<Permission> permissionOptional = this.permissionRepository.findById(id);

        if (permissionOptional.isPresent()) {
            return permissionOptional.get();
        } else {
            throw new IdInvalidException(
                    "Không tìm thấy Permission với ID = " + id);
        }
    }

    // UPDATE
    public Permission updatePermission(
            Long id, ReqUpdatePermissionDTO permissionReq)
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

        return this.permissionRepository.save(permission);
    }

    // DELETE
    public void deletePermission(Long id) throws IdInvalidException {
        Permission permission = getPermissionById(id);
        this.permissionRepository.deleteById(permission.getId());
    }

    // CONVERT CREATE DTO
    public Permission convertToReqCreatePermissionDTO(
            ReqCreatePermissionDTO req) {

        Permission permission = new Permission();
        permission.setId(req.getId());
        permission.setName(req.getName());
        permission.setDescription(req.getDescription());

        return permission;
    }
}
