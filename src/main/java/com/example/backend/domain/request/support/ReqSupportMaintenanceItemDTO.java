package com.example.backend.domain.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSupportMaintenanceItemDTO {

    @NotBlank(message = "Nhãn không được để trống")
    private String label;

    @NotBlank(message = "Tần suất / mô tả không được để trống")
    private String frequencyText;

    private String color;

    private Integer sortOrder;
}
