package com.example.backend.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.response.asset.ResAssetDetailDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.AssetService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

/**
 * Danh sách & chi tiết tài sản cho khách (không đăng nhập) — cùng whitelist {@code /api/v1/client/public/**}.
 * Không dùng {@code /api/v1/assets} (admin + JWT) để đồng bộ luồng với public pitch.
 */
@RestController
@RequestMapping("/api/v1/client/public")
public class ClientPublicAssetController {

    private final AssetService assetService;

    public ClientPublicAssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/assets")
    @ApiMessage("Lấy danh sách tài sản (public)")
    public ResponseEntity<ResultPaginationDTO> getPublicAssets(
            @Filter Specification<Asset> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(assetService.getAllAssets(spec, pageable));
    }

    @GetMapping("/assets/{id}")
    @ApiMessage("Lấy chi tiết tài sản (public)")
    public ResponseEntity<ResAssetDetailDTO> getPublicAssetById(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        Asset a = assetService.getAssetById(id);
        return ResponseEntity.ok(assetService.convertToResAssetDetailDTO(a));
    }
}
