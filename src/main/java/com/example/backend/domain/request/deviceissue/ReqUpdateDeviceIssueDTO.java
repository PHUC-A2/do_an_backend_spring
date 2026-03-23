package com.example.backend.domain.request.deviceissue;

import com.example.backend.util.constant.deviceissue.IssueStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateDeviceIssueDTO {

    @NotNull(message = "Thiết bị không được để trống")
    private Long deviceId;

    @NotNull(message = "Tài sản không được để trống")
    private Long assetId;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotBlank(message = "Người báo không được để trống")
    private String reportedBy;

    @NotNull(message = "Trạng thái không được để trống")
    private IssueStatus status;
}
