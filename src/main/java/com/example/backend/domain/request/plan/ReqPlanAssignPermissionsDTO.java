package com.example.backend.domain.request.plan;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPlanAssignPermissionsDTO {
    @NotNull
    private List<Long> permissionIds = new ArrayList<>();
}
