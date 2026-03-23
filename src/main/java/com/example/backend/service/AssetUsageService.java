package com.example.backend.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.assetusage.ReqCreateAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqUpdateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageDetailDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageListDTO;
import com.example.backend.domain.response.assetusage.ResCreateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResUpdateAssetUsageDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * CRUD đăng ký sử dụng tài sản — kiểm tra trùng khung giờ theo db.md (asset + date + start/end).
 */
@Service
public class AssetUsageService {

    private static final Set<AssetUsageStatus> OVERLAP_BLOCKING = EnumSet.of(
            AssetUsageStatus.PENDING,
            AssetUsageStatus.APPROVED,
            AssetUsageStatus.IN_PROGRESS);

    private final AssetUsageRepository assetUsageRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public AssetUsageService(
            AssetUsageRepository assetUsageRepository,
            UserRepository userRepository,
            AssetRepository assetRepository) {
        this.assetUsageRepository = assetUsageRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public ResCreateAssetUsageDTO createAssetUsage(@NonNull ReqCreateAssetUsageDTO req) {
        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user với ID = " + req.getUserId()));
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        assertNoOverlap(req.getAssetId(), req.getDate(), req.getStartTime(), req.getEndTime(), 0L);

        AssetUsage u = new AssetUsage();
        u.setUser(user);
        u.setAsset(asset);
        u.setUsageType(req.getUsageType());
        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        u.setStatus(req.getStatus() != null ? req.getStatus() : AssetUsageStatus.PENDING);

        AssetUsage saved = assetUsageRepository.save(u);
        return convertToResCreateAssetUsageDTO(saved);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllAssetUsages(Specification<AssetUsage> spec, @NonNull Pageable pageable) {
        Page<AssetUsage> page = assetUsageRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResAssetUsageListDTO> list = new ArrayList<>();
        for (AssetUsage u : page.getContent()) {
            list.add(convertToResAssetUsageListDTO(u));
        }
        rs.setResult(list);
        return rs;
    }

    public AssetUsage getAssetUsageById(@NonNull Long id) throws IdInvalidException {
        Optional<AssetUsage> opt = assetUsageRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy AssetUsage với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResAssetUsageDetailDTO getAssetUsageDetailById(@NonNull Long id) throws IdInvalidException {
        AssetUsage u = getAssetUsageById(id);
        return convertToResAssetUsageDetailDTO(u);
    }

    @Transactional
    public ResUpdateAssetUsageDTO updateAssetUsage(@NonNull Long id, @NonNull ReqUpdateAssetUsageDTO req)
            throws IdInvalidException {
        assertValidTimeRange(req.getStartTime(), req.getEndTime());
        AssetUsage u = getAssetUsageById(id);
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user với ID = " + req.getUserId()));
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        assertNoOverlap(req.getAssetId(), req.getDate(), req.getStartTime(), req.getEndTime(), id);

        u.setUser(user);
        u.setAsset(asset);
        u.setUsageType(req.getUsageType());
        u.setUsageDate(req.getDate());
        u.setStartTime(req.getStartTime());
        u.setEndTime(req.getEndTime());
        u.setSubject(req.getSubject().trim());
        u.setStatus(req.getStatus());

        AssetUsage saved = assetUsageRepository.save(u);
        return convertToResUpdateAssetUsageDTO(saved);
    }

    @Transactional
    public void deleteAssetUsage(@NonNull Long id) throws IdInvalidException {
        getAssetUsageById(id);
        assetUsageRepository.deleteById(id);
    }

    public ResAssetUsageDetailDTO convertToResAssetUsageDetailDTO(AssetUsage u) {
        ResAssetUsageDetailDTO res = new ResAssetUsageDetailDTO();
        res.setId(u.getId());
        if (u.getUser() != null) {
            res.setUserId(u.getUser().getId());
            res.setUserName(u.getUser().getName());
            res.setUserEmail(u.getUser().getEmail());
        }
        if (u.getAsset() != null) {
            res.setAssetId(u.getAsset().getId());
            res.setAssetName(u.getAsset().getAssetName());
        }
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setCreatedBy(u.getCreatedBy());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private ResAssetUsageListDTO convertToResAssetUsageListDTO(AssetUsage u) {
        ResAssetUsageListDTO res = new ResAssetUsageListDTO();
        res.setId(u.getId());
        if (u.getUser() != null) {
            res.setUserId(u.getUser().getId());
            res.setUserName(u.getUser().getName());
            res.setUserEmail(u.getUser().getEmail());
        }
        if (u.getAsset() != null) {
            res.setAssetId(u.getAsset().getId());
            res.setAssetName(u.getAsset().getAssetName());
        }
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setCreatedBy(u.getCreatedBy());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private ResCreateAssetUsageDTO convertToResCreateAssetUsageDTO(AssetUsage u) {
        ResCreateAssetUsageDTO res = new ResCreateAssetUsageDTO();
        res.setId(u.getId());
        res.setUserId(u.getUser() != null ? u.getUser().getId() : null);
        res.setAssetId(u.getAsset() != null ? u.getAsset().getId() : null);
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setCreatedAt(u.getCreatedAt());
        return res;
    }

    private ResUpdateAssetUsageDTO convertToResUpdateAssetUsageDTO(AssetUsage u) {
        ResUpdateAssetUsageDTO res = new ResUpdateAssetUsageDTO();
        res.setId(u.getId());
        res.setUserId(u.getUser() != null ? u.getUser().getId() : null);
        res.setAssetId(u.getAsset() != null ? u.getAsset().getId() : null);
        res.setUsageType(u.getUsageType());
        res.setDate(u.getUsageDate());
        res.setStartTime(u.getStartTime());
        res.setEndTime(u.getEndTime());
        res.setSubject(u.getSubject());
        res.setStatus(u.getStatus());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setUpdatedBy(u.getUpdatedBy());
        return res;
    }

    private void assertValidTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc");
        }
    }

    private void assertNoOverlap(Long assetId, java.time.LocalDate date, java.time.LocalTime start,
            java.time.LocalTime end, long excludeId) {
        long n = assetUsageRepository.countTimeOverlap(assetId, date, start, end, excludeId, OVERLAP_BLOCKING);
        if (n > 0) {
            throw new BadRequestException("Khung giờ trùng với đăng ký khác trên cùng tài sản và ngày");
        }
    }
}
