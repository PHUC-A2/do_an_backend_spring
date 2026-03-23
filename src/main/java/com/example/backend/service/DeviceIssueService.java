package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.entity.Device;
import com.example.backend.domain.entity.DeviceIssue;
import com.example.backend.domain.request.deviceissue.ReqCreateDeviceIssueDTO;
import com.example.backend.domain.request.deviceissue.ReqUpdateDeviceIssueDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.deviceissue.ResCreateDeviceIssueDTO;
import com.example.backend.domain.response.deviceissue.ResDeviceIssueDetailDTO;
import com.example.backend.domain.response.deviceissue.ResDeviceIssueListDTO;
import com.example.backend.domain.response.deviceissue.ResUpdateDeviceIssueDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.DeviceIssueRepository;
import com.example.backend.repository.DeviceRepository;
import com.example.backend.util.constant.deviceissue.IssueStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * CRUD sự cố thiết bị (bảng device_issues) — assetId phải trùng tài sản của device.
 */
@Service
public class DeviceIssueService {

    private final DeviceIssueRepository deviceIssueRepository;
    private final DeviceRepository deviceRepository;
    private final AssetRepository assetRepository;
    private final AssetUsageRepository assetUsageRepository;
    private final UserService userService;

    public DeviceIssueService(
            DeviceIssueRepository deviceIssueRepository,
            DeviceRepository deviceRepository,
            AssetRepository assetRepository,
            AssetUsageRepository assetUsageRepository,
            UserService userService) {
        this.deviceIssueRepository = deviceIssueRepository;
        this.deviceRepository = deviceRepository;
        this.assetRepository = assetRepository;
        this.assetUsageRepository = assetUsageRepository;
        this.userService = userService;
    }

    @Transactional
    public ResCreateDeviceIssueDTO createDeviceIssue(@NonNull ReqCreateDeviceIssueDTO req) {
        Device device = loadDeviceAndAssertAsset(req.getDeviceId(), req.getAssetId());
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản ID = " + req.getAssetId()));

        DeviceIssue issue = new DeviceIssue();
        issue.setDevice(device);
        issue.setAsset(asset);
        issue.setDescription(req.getDescription().trim());
        issue.setReportedBy(req.getReportedBy().trim());
        issue.setStatus(req.getStatus() != null ? req.getStatus() : IssueStatus.OPEN);

        DeviceIssue saved = deviceIssueRepository.save(issue);
        return convertToResCreateDeviceIssueDTO(saved);
    }

    @Transactional
    public ResCreateDeviceIssueDTO createDeviceIssueForCurrentUser(
            @NonNull Long assetUsageId,
            @NonNull Long deviceId,
            @NonNull String description,
            @NonNull String email) {
        var user = userService.handleGetUserByUsername(email);
        var usage = assetUsageRepository.findById(assetUsageId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đăng ký phòng"));
        if (usage.getUser() == null || !usage.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền báo sự cố cho đăng ký này");
        }
        Device device = loadDeviceAndAssertAsset(deviceId, usage.getAsset().getId());
        ReqCreateDeviceIssueDTO req = new ReqCreateDeviceIssueDTO();
        req.setDeviceId(device.getId());
        req.setAssetId(usage.getAsset().getId());
        req.setDescription(description);
        req.setReportedBy(user.getEmail());
        return createDeviceIssue(req);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllDeviceIssues(Specification<DeviceIssue> spec, @NonNull Pageable pageable) {
        Page<DeviceIssue> page = deviceIssueRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResDeviceIssueListDTO> list = new ArrayList<>();
        for (DeviceIssue issue : page.getContent()) {
            list.add(convertToResDeviceIssueListDTO(issue));
        }
        rs.setResult(list);
        return rs;
    }

    public DeviceIssue getDeviceIssueById(@NonNull Long id) throws IdInvalidException {
        Optional<DeviceIssue> opt = deviceIssueRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy DeviceIssue với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResDeviceIssueDetailDTO getDeviceIssueDetailById(@NonNull Long id) throws IdInvalidException {
        return convertToResDeviceIssueDetailDTO(getDeviceIssueById(id));
    }

    @Transactional
    public ResUpdateDeviceIssueDTO updateDeviceIssue(@NonNull Long id, @NonNull ReqUpdateDeviceIssueDTO req)
            throws IdInvalidException {
        DeviceIssue issue = getDeviceIssueById(id);
        Device device = loadDeviceAndAssertAsset(req.getDeviceId(), req.getAssetId());
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản ID = " + req.getAssetId()));

        issue.setDevice(device);
        issue.setAsset(asset);
        issue.setDescription(req.getDescription().trim());
        issue.setReportedBy(req.getReportedBy().trim());
        issue.setStatus(req.getStatus());

        DeviceIssue saved = deviceIssueRepository.save(issue);
        return convertToResUpdateDeviceIssueDTO(saved);
    }

    @Transactional
    public void deleteDeviceIssue(@NonNull Long id) throws IdInvalidException {
        getDeviceIssueById(id);
        deviceIssueRepository.deleteById(id);
    }

    private Device loadDeviceAndAssertAsset(@NonNull Long deviceId, @NonNull Long assetId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thiết bị ID = " + deviceId));
        if (device.getAsset() == null || !device.getAsset().getId().equals(assetId)) {
            throw new BadRequestException("Tài sản không khớp với thiết bị đã chọn");
        }
        return device;
    }

    private ResDeviceIssueListDTO convertToResDeviceIssueListDTO(DeviceIssue issue) {
        ResDeviceIssueListDTO res = new ResDeviceIssueListDTO();
        res.setId(issue.getId());
        res.setDescription(issue.getDescription());
        res.setReportedBy(issue.getReportedBy());
        res.setStatus(issue.getStatus());
        res.setCreatedAt(issue.getCreatedAt());
        res.setUpdatedAt(issue.getUpdatedAt());
        res.setCreatedBy(issue.getCreatedBy());
        res.setUpdatedBy(issue.getUpdatedBy());
        Device d = issue.getDevice();
        if (d != null) {
            res.setDeviceId(d.getId());
            res.setDeviceName(d.getDeviceName());
        }
        Asset a = issue.getAsset();
        if (a != null) {
            res.setAssetId(a.getId());
            res.setAssetName(a.getAssetName());
        }
        return res;
    }

    private ResDeviceIssueDetailDTO convertToResDeviceIssueDetailDTO(DeviceIssue issue) {
        ResDeviceIssueDetailDTO res = new ResDeviceIssueDetailDTO();
        res.setId(issue.getId());
        res.setDescription(issue.getDescription());
        res.setReportedBy(issue.getReportedBy());
        res.setStatus(issue.getStatus());
        res.setCreatedAt(issue.getCreatedAt());
        res.setUpdatedAt(issue.getUpdatedAt());
        res.setCreatedBy(issue.getCreatedBy());
        res.setUpdatedBy(issue.getUpdatedBy());
        Device d = issue.getDevice();
        if (d != null) {
            res.setDeviceId(d.getId());
            res.setDeviceName(d.getDeviceName());
        }
        Asset a = issue.getAsset();
        if (a != null) {
            res.setAssetId(a.getId());
            res.setAssetName(a.getAssetName());
        }
        return res;
    }

    private ResCreateDeviceIssueDTO convertToResCreateDeviceIssueDTO(DeviceIssue issue) {
        ResCreateDeviceIssueDTO res = new ResCreateDeviceIssueDTO();
        res.setId(issue.getId());
        res.setDeviceId(issue.getDevice() != null ? issue.getDevice().getId() : null);
        res.setAssetId(issue.getAsset() != null ? issue.getAsset().getId() : null);
        res.setDescription(issue.getDescription());
        res.setReportedBy(issue.getReportedBy());
        res.setStatus(issue.getStatus());
        res.setCreatedAt(issue.getCreatedAt());
        return res;
    }

    private ResUpdateDeviceIssueDTO convertToResUpdateDeviceIssueDTO(DeviceIssue issue) {
        ResUpdateDeviceIssueDTO res = new ResUpdateDeviceIssueDTO();
        res.setId(issue.getId());
        res.setDeviceId(issue.getDevice() != null ? issue.getDevice().getId() : null);
        res.setAssetId(issue.getAsset() != null ? issue.getAsset().getId() : null);
        res.setDescription(issue.getDescription());
        res.setReportedBy(issue.getReportedBy());
        res.setStatus(issue.getStatus());
        res.setUpdatedAt(issue.getUpdatedAt());
        res.setUpdatedBy(issue.getUpdatedBy());
        return res;
    }
}
