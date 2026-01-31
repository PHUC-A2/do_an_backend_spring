package com.example.backend.domain.request.role;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqAssignPermissionsToRoleDTO {

    @NotEmpty(message = "permissionIds không được rỗng")
    private List<Long> permissionIds;
}
