package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.request.asset.ReqCreateAssetDTO;
import com.example.backend.domain.request.asset.ReqUpdateAssetDTO;
import com.example.backend.domain.response.asset.ResAssetDetailDTO;
import com.example.backend.domain.response.asset.ResCreateAssetDTO;
import com.example.backend.domain.response.asset.ResUpdateAssetDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.AssetService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

/**
 * REST admin cho tài sản — bám UserController/PitchController (cùng @RequestMapping /api/v1).
 */
@RestController
@RequestMapping("/api/v1")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/assets")
    @ApiMessage("Tạo tài sản mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_CREATE')")
    public ResponseEntity<ResCreateAssetDTO> createAsset(
            @Valid @RequestBody @NonNull ReqCreateAssetDTO dto) {
        ResCreateAssetDTO res = assetService.createAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/assets")
    @ApiMessage("Lấy danh sách tài sản")
    // Giống PitchController: không bật PreAuthorize trên list (quyền kiểm soát ở FE / sau này bật lại)
    // @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllAssets(
            @Filter Specification<Asset> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(assetService.getAllAssets(spec, pageable));
    }

    @GetMapping("/assets/{id}")
    @ApiMessage("Lấy thông tin tài sản theo ID")
    // @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_VIEW_DETAIL')")
    public ResponseEntity<ResAssetDetailDTO> getAssetById(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        Asset a = assetService.getAssetById(id);
        return ResponseEntity.ok(assetService.convertToResAssetDetailDTO(a));
    }

    @PutMapping("/assets/{id}")
    @ApiMessage("Cập nhật thông tin tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_UPDATE')")
    public ResponseEntity<ResUpdateAssetDTO> updateAsset(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateAssetDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(assetService.updateAsset(id, dto));
    }

    @DeleteMapping("/assets/{id}")
    @ApiMessage("Xóa tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_DELETE')")
    public ResponseEntity<Void> deleteAsset(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }
}
