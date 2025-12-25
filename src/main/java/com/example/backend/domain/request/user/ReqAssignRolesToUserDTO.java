package com.example.backend.domain.request.user;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class ReqAssignRolesToUserDTO {

    @NotNull(message = "roleIds không được null")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
