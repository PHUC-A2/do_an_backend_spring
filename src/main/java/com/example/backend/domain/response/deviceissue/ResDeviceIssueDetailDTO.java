package com.example.backend.domain.response.deviceissue;

import java.time.Instant;

import com.example.backend.util.constant.deviceissue.IssueStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResDeviceIssueDetailDTO {

    private Long id;
    private Long deviceId;
    private String deviceName;
    private Long assetId;
    private String assetName;
    private String description;
    private String reportedBy;
    private IssueStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
