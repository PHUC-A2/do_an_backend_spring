package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.request.role.ReqCreateRoleDTO;
import com.example.backend.domain.request.role.ReqUpdateRoleDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.role.ResCreateRoleDTO;
import com.example.backend.domain.response.role.ResRoleDTO;
import com.example.backend.domain.response.role.ResUpdateRoleDTO;
import com.example.backend.repository.RoleRepository;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public ResCreateRoleDTO createRole(ReqCreateRoleDTO roleReq) throws NameInvalidException {

        boolean isNameExists = this.roleRepository.existsByName(roleReq.getName());
        if (isNameExists) {
            throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại");
        }

        Role role = convertToReqCreateRoleDTO(roleReq);
        Role saveRole = this.roleRepository.save(role);

        return this.convertToResCreateRoleDTO(saveRole);
    }

    public ResultPaginationDTO getAllRoles(Specification<Role> spec, Pageable pageable) {

        Page<Role> pageRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageRole.getTotalPages());
        mt.setTotal(pageRole.getTotalElements());

        rs.setMeta(mt);

        List<ResRoleDTO> resList = new ArrayList<>();
        for (Role role : pageRole.getContent()) {
            resList.add(this.convertToResRoleDTO(role));
        }

        rs.setResult(resList);
        return rs;
    }

    public Role getRoleById(Long id) throws IdInvalidException {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        } else {
            throw new IdInvalidException("Không tìm thấy Role với ID = " + id);
        }
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

    public void deleteRole(Long id) throws IdInvalidException {
        Role role = this.getRoleById(id);
        this.roleRepository.deleteById(role.getId());
    }

    // convert req create
    public Role convertToReqCreateRoleDTO(ReqCreateRoleDTO req) {
        Role role = new Role();
        role.setName(req.getName());
        role.setDescription(req.getDescription());

        return role;
    }

    // entity -> res get
    public ResRoleDTO convertToResRoleDTO(Role role) {

        ResRoleDTO res = new ResRoleDTO();

        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        res.setCreatedBy(role.getCreatedBy());
        res.setUpdatedAt(role.getUpdatedAt());
        res.setUpdatedBy(role.getUpdatedBy());

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

}
