package com.example.backend.domain.response.asset;

import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;

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
    private String responsibleName;
    private String location;
    private Long capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h;
    private AssetRoomFeeMode roomFeeMode;
    private String assetsUrl;
    private Instant updatedAt;
    private String updatedBy;
}
