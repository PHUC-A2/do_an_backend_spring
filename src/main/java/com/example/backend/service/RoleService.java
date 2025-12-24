package com.example.backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.request.role.ReqCreateRoleDTO;
import com.example.backend.domain.request.role.ReqUpdateRoleDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.RoleRepository;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.NameInvalidException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(ReqCreateRoleDTO roleReq) throws NameInvalidException {

        boolean isNameExists = this.roleRepository.existsByName(roleReq.getName());
        if (isNameExists) {
            throw new NameInvalidException("Name '" + roleReq.getName() + "' đã tồn tại");
        }

        Role role = convertToReqCreateRoleDTO(roleReq);
        return this.roleRepository.save(role);
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
        rs.setResult(pageRole.getContent());
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

    public Role updateRole(Long id, ReqUpdateRoleDTO roleReq)
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

        return roleRepository.save(role);
    }

    public void deleteRole(Long id) throws IdInvalidException {
        Role role = this.getRoleById(id);
        this.roleRepository.deleteById(role.getId());
    }

    // convert req create
    public Role convertToReqCreateRoleDTO(ReqCreateRoleDTO req) {
        Role role = new Role();
        role.setId(req.getId());
        role.setName(req.getName());
        role.setDescription(req.getDescription());

        return role;
    }

}
