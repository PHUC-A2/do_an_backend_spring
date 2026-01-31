package com.example.backend.domain.request.user;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class ReqAssignRolesToUserDTO {

    @NotEmpty(message = "roleIds không được rỗng")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
