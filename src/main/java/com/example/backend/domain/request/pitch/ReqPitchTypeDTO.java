package com.example.backend.domain.request.pitch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqPitchTypeDTO {
    @NotBlank(message = "Tên loại sân không được để trống")
    private String name;

    private String code;
}
