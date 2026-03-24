package com.example.backend.domain.response.asset;

import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response sau khi tạo tài sản — giống ResCreateUserDTO (id + field chính + createdAt).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateAssetDTO {

    private Long id; // id bản ghi mới
    private String assetName;
    private String responsibleName;
    private String location;
    private Long capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h;
    private AssetRoomFeeMode roomFeeMode;
    private String assetsUrl;
    private Instant createdAt; // thời điểm tạo trả về cho client
}
