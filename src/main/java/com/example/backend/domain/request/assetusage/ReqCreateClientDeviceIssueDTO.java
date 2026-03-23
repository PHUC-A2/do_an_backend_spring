package com.example.backend.domain.request.assetusage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateClientDeviceIssueDTO {

    @NotNull(message = "Thiết bị không được để trống")
    private Long deviceId;

    @NotBlank(message = "Mô tả sự cố không được để trống")
    private String description;
}
