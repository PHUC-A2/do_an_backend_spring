package com.example.backend.domain.response.asset;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response sau khi cập nhật — bám ResUpdatePitchDTO (có id, updatedAt, updatedBy).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateAssetDTO {

    private Long id;
    private String assetName;
    private String location;
    private Long capacity;
    private String assetsUrl;
    private Instant updatedAt;
    private String updatedBy;
}
