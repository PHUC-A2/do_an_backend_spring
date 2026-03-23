package com.example.backend.domain.response.asset;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chi tiết một tài sản — cùng field với list (giống ResUserDetailDTO nhưng không có roles).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResAssetDetailDTO {

    private Long id;
    private String assetName;
    private String responsibleName;
    private String location;
    private Long capacity;
    private String assetsUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
