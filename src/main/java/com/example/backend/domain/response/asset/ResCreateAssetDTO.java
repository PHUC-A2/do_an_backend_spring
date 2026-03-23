package com.example.backend.domain.response.asset;

import java.time.Instant;

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
    private String location;
    private Long capacity;
    private String assetsUrl;
    private Instant createdAt; // thời điểm tạo trả về cho client
}
