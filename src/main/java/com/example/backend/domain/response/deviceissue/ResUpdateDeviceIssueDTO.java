package com.example.backend.domain.response.deviceissue;

import java.time.Instant;

import com.example.backend.util.constant.deviceissue.IssueStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateDeviceIssueDTO {

    private Long id;
    private Long deviceId;
    private Long assetId;
    private String description;
    private String reportedBy;
    private IssueStatus status;
    private Instant updatedAt;
    private String updatedBy;
}
