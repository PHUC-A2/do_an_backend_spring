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

import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.request.assetusage.ReqCreateAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqUpdateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageDetailDTO;
import com.example.backend.domain.response.assetusage.ResCreateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResUpdateAssetUsageDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.AssetUsageService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AssetUsageController {

    private final AssetUsageService assetUsageService;

    public AssetUsageController(AssetUsageService assetUsageService) {
        this.assetUsageService = assetUsageService;
    }

    @PostMapping("/asset-usages")
    @ApiMessage("Tạo đăng ký sử dụng tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_USAGE_CREATE')")
    public ResponseEntity<ResCreateAssetUsageDTO> createAssetUsage(
            @Valid @RequestBody @NonNull ReqCreateAssetUsageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetUsageService.createAssetUsage(dto));
    }

    @GetMapping("/asset-usages")
    @ApiMessage("Lấy danh sách đăng ký sử dụng tài sản")
    public ResponseEntity<ResultPaginationDTO> getAllAssetUsages(
            @Filter Specification<AssetUsage> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(assetUsageService.getAllAssetUsages(spec, pageable));
    }

    @GetMapping("/asset-usages/{id}")
    @ApiMessage("Lấy chi tiết đăng ký sử dụng tài sản")
    public ResponseEntity<ResAssetUsageDetailDTO> getAssetUsageById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.ok(assetUsageService.getAssetUsageDetailById(id));
    }

    @PutMapping("/asset-usages/{id}")
    @ApiMessage("Cập nhật đăng ký sử dụng tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_USAGE_UPDATE')")
    public ResponseEntity<ResUpdateAssetUsageDTO> updateAssetUsage(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateAssetUsageDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(assetUsageService.updateAssetUsage(id, dto));
    }

    @DeleteMapping("/asset-usages/{id}")
    @ApiMessage("Xóa đăng ký sử dụng tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('ASSET_USAGE_DELETE')")
    public ResponseEntity<Void> deleteAssetUsage(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        assetUsageService.deleteAssetUsage(id);
        return ResponseEntity.ok().build();
    }
}
