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
import com.example.backend.domain.entity.Device;
import com.example.backend.domain.request.device.ReqCreateDeviceDTO;
import com.example.backend.domain.request.device.ReqUpdateDeviceDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.device.ResCreateDeviceDTO;
import com.example.backend.domain.response.device.ResDeviceDetailDTO;
import com.example.backend.domain.response.device.ResDeviceListDTO;
import com.example.backend.domain.response.device.ResUpdateDeviceDTO;
import com.example.backend.repository.AssetRepository;
import com.example.backend.repository.DeviceRepository;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * CRUD thiết bị theo tài sản (bảng devices) — cùng flow phân trang như AssetService.
 */
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final AssetRepository assetRepository;

    public DeviceService(DeviceRepository deviceRepository, AssetRepository assetRepository) {
        this.deviceRepository = deviceRepository;
        this.assetRepository = assetRepository;
    }

    public ResCreateDeviceDTO createDevice(@NonNull ReqCreateDeviceDTO req) {
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        Device d = new Device();
        d.setAsset(asset);
        d.setDeviceName(req.getDeviceName());
        d.setQuantity(req.getQuantity());
        d.setStatus(req.getStatus());
        d.setDeviceType(req.getDeviceType());
        Device saved = deviceRepository.save(d);
        return convertToResCreateDeviceDTO(saved);
    }

    public ResultPaginationDTO getAllDevices(Specification<Device> spec, @NonNull Pageable pageable) {
        Page<Device> page = deviceRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResDeviceListDTO> resList = new ArrayList<>();
        for (Device d : page.getContent()) {
            resList.add(convertToResDeviceListDTO(d));
        }
        rs.setResult(resList);
        return rs;
    }

    public Device getDeviceById(@NonNull Long id) throws IdInvalidException {
        Optional<Device> opt = deviceRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy Device với ID = " + id);
    }

    public ResUpdateDeviceDTO updateDevice(@NonNull Long id, ReqUpdateDeviceDTO req) throws IdInvalidException {
        Device d = getDeviceById(id);
        Asset asset = assetRepository.findById(req.getAssetId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài sản với ID = " + req.getAssetId()));
        d.setAsset(asset);
        d.setDeviceName(req.getDeviceName());
        d.setQuantity(req.getQuantity());
        d.setStatus(req.getStatus());
        d.setDeviceType(req.getDeviceType());
        Device updated = deviceRepository.save(d);
        return convertToResUpdateDeviceDTO(updated);
    }

    public void deleteDevice(@NonNull Long id) throws IdInvalidException {
        getDeviceById(id);
        deviceRepository.deleteById(id);
    }

    public ResCreateDeviceDTO convertToResCreateDeviceDTO(Device d) {
        ResCreateDeviceDTO res = new ResCreateDeviceDTO();
        res.setId(d.getId());
        res.setAssetId(d.getAsset().getId());
        res.setDeviceName(d.getDeviceName());
        res.setQuantity(d.getQuantity());
        res.setStatus(d.getStatus());
        res.setDeviceType(d.getDeviceType());
        res.setCreatedAt(d.getCreatedAt());
        return res;
    }

    public ResUpdateDeviceDTO convertToResUpdateDeviceDTO(Device d) {
        ResUpdateDeviceDTO res = new ResUpdateDeviceDTO();
        res.setId(d.getId());
        res.setAssetId(d.getAsset().getId());
        res.setDeviceName(d.getDeviceName());
        res.setQuantity(d.getQuantity());
        res.setStatus(d.getStatus());
        res.setDeviceType(d.getDeviceType());
        res.setUpdatedAt(d.getUpdatedAt());
        res.setUpdatedBy(d.getUpdatedBy());
        return res;
    }

    public ResDeviceListDTO convertToResDeviceListDTO(Device d) {
        ResDeviceListDTO res = new ResDeviceListDTO();
        res.setId(d.getId());
        Asset a = d.getAsset();
        res.setAssetId(a != null ? a.getId() : null);
        res.setAssetName(a != null ? a.getAssetName() : null);
        res.setDeviceName(d.getDeviceName());
        res.setQuantity(d.getQuantity());
        res.setStatus(d.getStatus());
        res.setDeviceType(d.getDeviceType());
        res.setCreatedAt(d.getCreatedAt());
        res.setUpdatedAt(d.getUpdatedAt());
        res.setCreatedBy(d.getCreatedBy());
        res.setUpdatedBy(d.getUpdatedBy());
        return res;
    }

    public ResDeviceDetailDTO convertToResDeviceDetailDTO(Device d) {
        ResDeviceDetailDTO res = new ResDeviceDetailDTO();
        res.setId(d.getId());
        Asset a = d.getAsset();
        res.setAssetId(a != null ? a.getId() : null);
        res.setAssetName(a != null ? a.getAssetName() : null);
        res.setDeviceName(d.getDeviceName());
        res.setQuantity(d.getQuantity());
        res.setStatus(d.getStatus());
        res.setDeviceType(d.getDeviceType());
        res.setCreatedAt(d.getCreatedAt());
        res.setUpdatedAt(d.getUpdatedAt());
        res.setCreatedBy(d.getCreatedBy());
        res.setUpdatedBy(d.getUpdatedBy());
        return res;
    }
}
