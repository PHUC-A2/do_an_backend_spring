package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.request.asset.ReqCreateAssetDTO;
import com.example.backend.domain.request.asset.ReqUpdateAssetDTO;
import com.example.backend.domain.response.asset.ResAssetDetailDTO;
import com.example.backend.domain.response.asset.ResAssetListDTO;
import com.example.backend.domain.response.asset.ResCreateAssetDTO;
import com.example.backend.domain.response.asset.ResUpdateAssetDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.util.error.IdInvalidException;

/**
 * Service quản lý tài sản — luồng CRUD copy từ PitchService/UserService (phân trang + spec).
 */
@Service
public class AssetService {

    private final AssetRepository assetRepository; // truy cập DB

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /** Tạo mới bản ghi tài sản */
    public ResCreateAssetDTO createAsset(@NonNull ReqCreateAssetDTO req) {
        Asset asset = convertToEntityOnCreate(req);
        Asset saved = assetRepository.save(asset);
        return convertToResCreateAssetDTO(saved);
    }

    /** Danh sách có phân trang, filter Spring Filter */
    public ResultPaginationDTO getAllAssets(Specification<Asset> spec, @NonNull Pageable pageable) {
        Page<Asset> page = assetRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // page 1-based giống UserService
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        List<ResAssetListDTO> resList = new ArrayList<>();
        for (Asset a : page.getContent()) {
            resList.add(convertToResAssetListDTO(a));
        }
        rs.setResult(resList);
        return rs;
    }

    /** Lấy entity theo id hoặc ném IdInvalidException */
    public Asset getAssetById(@NonNull Long id) throws IdInvalidException {
        Optional<Asset> opt = assetRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy Asset với ID = " + id); // cùng format UserService
    }

    /** Cập nhật theo id — gán trực tiếp từ DTO giống PitchService.updatePitch */
    public ResUpdateAssetDTO updateAsset(@NonNull Long id, ReqUpdateAssetDTO req) throws IdInvalidException {
        Asset asset = getAssetById(id);
        asset.setAssetName(req.getAssetName());
        asset.setResponsibleName(req.getResponsibleName());
        asset.setLocation(req.getLocation());
        asset.setCapacity(req.getCapacity());
        asset.setAssetsUrl(req.getAssetsUrl());
        Asset updated = assetRepository.save(asset);
        return convertToResUpdateAssetDTO(updated);
    }

    /** Xóa cứng */
    public void deleteAsset(@NonNull Long id) throws IdInvalidException {
        getAssetById(id); // đảm bảo tồn tại trước khi xóa
        assetRepository.deleteById(id);
    }

    /** Map request tạo -> entity */
    @NonNull
    public Asset convertToEntityOnCreate(@NonNull ReqCreateAssetDTO req) {
        Asset a = new Asset();
        a.setAssetName(req.getAssetName());
        a.setResponsibleName(req.getResponsibleName());
        a.setLocation(req.getLocation());
        a.setCapacity(req.getCapacity());
        a.setAssetsUrl(req.getAssetsUrl());
        return a;
    }

    /** Entity -> response create */
    public ResCreateAssetDTO convertToResCreateAssetDTO(Asset a) {
        ResCreateAssetDTO res = new ResCreateAssetDTO();
        res.setId(a.getId());
        res.setAssetName(a.getAssetName());
        res.setResponsibleName(a.getResponsibleName());
        res.setLocation(a.getLocation());
        res.setCapacity(a.getCapacity());
        res.setAssetsUrl(a.getAssetsUrl());
        res.setCreatedAt(a.getCreatedAt());
        return res;
    }

    /** Entity -> response update */
    public ResUpdateAssetDTO convertToResUpdateAssetDTO(Asset a) {
        ResUpdateAssetDTO res = new ResUpdateAssetDTO();
        res.setId(a.getId());
        res.setAssetName(a.getAssetName());
        res.setResponsibleName(a.getResponsibleName());
        res.setLocation(a.getLocation());
        res.setCapacity(a.getCapacity());
        res.setAssetsUrl(a.getAssetsUrl());
        res.setUpdatedAt(a.getUpdatedAt());
        res.setUpdatedBy(a.getUpdatedBy());
        return res;
    }

    /** Entity -> dòng list */
    public ResAssetListDTO convertToResAssetListDTO(Asset a) {
        ResAssetListDTO res = new ResAssetListDTO();
        res.setId(a.getId());
        res.setAssetName(a.getAssetName());
        res.setResponsibleName(a.getResponsibleName());
        res.setLocation(a.getLocation());
        res.setCapacity(a.getCapacity());
        res.setAssetsUrl(a.getAssetsUrl());
        res.setCreatedAt(a.getCreatedAt());
        res.setUpdatedAt(a.getUpdatedAt());
        res.setCreatedBy(a.getCreatedBy());
        res.setUpdatedBy(a.getUpdatedBy());
        return res;
    }

    /** Entity -> chi tiết */
    public ResAssetDetailDTO convertToResAssetDetailDTO(Asset a) {
        ResAssetDetailDTO res = new ResAssetDetailDTO();
        res.setId(a.getId());
        res.setAssetName(a.getAssetName());
        res.setResponsibleName(a.getResponsibleName());
        res.setLocation(a.getLocation());
        res.setCapacity(a.getCapacity());
        res.setAssetsUrl(a.getAssetsUrl());
        res.setCreatedAt(a.getCreatedAt());
        res.setUpdatedAt(a.getUpdatedAt());
        res.setCreatedBy(a.getCreatedBy());
        res.setUpdatedBy(a.getUpdatedBy());
        return res;
    }
}
