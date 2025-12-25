package com.example.backend.domain.request.role;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqAssignPermissionsToRoleDTO {

    @NotNull(message = "permissionIds không được null")
    private List<Long> permissionIds;
}
