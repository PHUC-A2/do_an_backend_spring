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

import com.example.backend.domain.entity.AssetUsage;
import com.example.backend.domain.entity.Checkout;
import com.example.backend.domain.entity.DeviceReturn;
import com.example.backend.domain.request.devicereturn.ReqCreateDeviceReturnDTO;
import com.example.backend.domain.request.devicereturn.ReqUpdateDeviceReturnDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.devicereturn.ResCreateDeviceReturnDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnDetailDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnListDTO;
import com.example.backend.domain.response.devicereturn.ResUpdateDeviceReturnDTO;
import com.example.backend.repository.AssetUsageRepository;
import com.example.backend.repository.CheckoutRepository;
import com.example.backend.repository.DeviceReturnRepository;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

/**
 * Phiếu trả — 1 checkout 1 return; chỉ tạo khi {@link AssetUsage} đang IN_PROGRESS (đã nhận).
 */
@Service
public class DeviceReturnService {

    private final DeviceReturnRepository deviceReturnRepository;
    private final CheckoutRepository checkoutRepository;
    private final AssetUsageRepository assetUsageRepository;

    public DeviceReturnService(
            DeviceReturnRepository deviceReturnRepository,
            CheckoutRepository checkoutRepository,
            AssetUsageRepository assetUsageRepository) {
        this.deviceReturnRepository = deviceReturnRepository;
        this.checkoutRepository = checkoutRepository;
        this.assetUsageRepository = assetUsageRepository;
    }

    @Transactional
    public ResCreateDeviceReturnDTO createDeviceReturn(@NonNull ReqCreateDeviceReturnDTO req) {
        Checkout checkout = checkoutRepository.findById(req.getCheckoutId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy checkout ID = " + req.getCheckoutId()));

        if (deviceReturnRepository.existsByCheckoutId(req.getCheckoutId())) {
            throw new BadRequestException("Checkout này đã có phiếu trả");
        }

        AssetUsage usage = checkout.getAssetUsage();
        if (usage == null) {
            throw new BadRequestException("Checkout không gắn đăng ký sử dụng");
        }
        if (usage.getStatus() != AssetUsageStatus.IN_PROGRESS) {
            throw new BadRequestException("Chỉ được trả khi đăng ký đang ở trạng thái Đang dùng (đã nhận tài sản)");
        }

        DeviceReturn r = new DeviceReturn();
        r.setCheckout(checkout);
        r.setReturnTime(req.getReturnTime() != null ? req.getReturnTime() : java.time.Instant.now());
        r.setDeviceStatus(req.getDeviceStatus());

        DeviceReturn saved = deviceReturnRepository.save(r);

        usage.setStatus(AssetUsageStatus.COMPLETED);
        assetUsageRepository.save(usage);

        return convertToResCreateDeviceReturnDTO(saved);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllDeviceReturns(Specification<DeviceReturn> spec, @NonNull Pageable pageable) {
        Page<DeviceReturn> page = deviceReturnRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        List<ResDeviceReturnListDTO> list = new ArrayList<>();
        for (DeviceReturn r : page.getContent()) {
            list.add(convertToResDeviceReturnListDTO(r));
        }
        rs.setResult(list);
        return rs;
    }

    public DeviceReturn getDeviceReturnById(@NonNull Long id) throws IdInvalidException {
        Optional<DeviceReturn> opt = deviceReturnRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy DeviceReturn với ID = " + id);
    }

    @Transactional(readOnly = true)
    public ResDeviceReturnDetailDTO getDeviceReturnDetailById(@NonNull Long id) throws IdInvalidException {
        return convertToResDeviceReturnDetailDTO(getDeviceReturnById(id));
    }

    @Transactional
    public ResUpdateDeviceReturnDTO updateDeviceReturn(@NonNull Long id, @NonNull ReqUpdateDeviceReturnDTO req)
            throws IdInvalidException {
        DeviceReturn r = getDeviceReturnById(id);
        r.setReturnTime(req.getReturnTime());
        r.setDeviceStatus(req.getDeviceStatus());
        DeviceReturn saved = deviceReturnRepository.save(r);
        return convertToResUpdateDeviceReturnDTO(saved);
    }

    @Transactional
    public void deleteDeviceReturn(@NonNull Long id) throws IdInvalidException {
        DeviceReturn r = getDeviceReturnById(id);
        Checkout checkout = r.getCheckout();
        if (checkout != null) {
            AssetUsage usage = checkout.getAssetUsage();
            if (usage != null && usage.getStatus() == AssetUsageStatus.COMPLETED) {
                usage.setStatus(AssetUsageStatus.IN_PROGRESS);
                assetUsageRepository.save(usage);
            }
        }
        deviceReturnRepository.delete(r);
    }

    private ResDeviceReturnListDTO convertToResDeviceReturnListDTO(DeviceReturn r) {
        ResDeviceReturnListDTO res = new ResDeviceReturnListDTO();
        res.setId(r.getId());
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setCreatedBy(r.getCreatedBy());
        res.setUpdatedBy(r.getUpdatedBy());
        Checkout c = r.getCheckout();
        if (c != null) {
            res.setCheckoutId(c.getId());
            res.setReceiveTime(c.getReceiveTime());
            AssetUsage u = c.getAssetUsage();
            if (u != null) {
                res.setAssetUsageId(u.getId());
                res.setUsageType(u.getUsageType());
                res.setUsageDate(u.getUsageDate());
                res.setStartTime(u.getStartTime());
                res.setEndTime(u.getEndTime());
                res.setAssetUsageStatus(u.getStatus());
                if (u.getUser() != null) {
                    res.setUserId(u.getUser().getId());
                    res.setUserName(u.getUser().getName());
                    res.setUserEmail(u.getUser().getEmail());
                }
                if (u.getAsset() != null) {
                    res.setAssetId(u.getAsset().getId());
                    res.setAssetName(u.getAsset().getAssetName());
                }
            }
        }
        return res;
    }

    private ResDeviceReturnDetailDTO convertToResDeviceReturnDetailDTO(DeviceReturn r) {
        ResDeviceReturnDetailDTO res = new ResDeviceReturnDetailDTO();
        res.setId(r.getId());
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setCreatedBy(r.getCreatedBy());
        res.setUpdatedBy(r.getUpdatedBy());
        Checkout c = r.getCheckout();
        if (c != null) {
            res.setCheckoutId(c.getId());
            res.setReceiveTime(c.getReceiveTime());
            res.setCheckoutConditionNote(c.getConditionNote());
            AssetUsage u = c.getAssetUsage();
            if (u != null) {
                res.setAssetUsageId(u.getId());
                res.setSubject(u.getSubject());
                res.setUsageType(u.getUsageType());
                res.setUsageDate(u.getUsageDate());
                res.setStartTime(u.getStartTime());
                res.setEndTime(u.getEndTime());
                res.setAssetUsageStatus(u.getStatus());
                if (u.getUser() != null) {
                    res.setUserId(u.getUser().getId());
                    res.setUserName(u.getUser().getName());
                    res.setUserEmail(u.getUser().getEmail());
                }
                if (u.getAsset() != null) {
                    res.setAssetId(u.getAsset().getId());
                    res.setAssetName(u.getAsset().getAssetName());
                }
            }
        }
        return res;
    }

    private ResCreateDeviceReturnDTO convertToResCreateDeviceReturnDTO(DeviceReturn r) {
        ResCreateDeviceReturnDTO res = new ResCreateDeviceReturnDTO();
        res.setId(r.getId());
        res.setCheckoutId(r.getCheckout() != null ? r.getCheckout().getId() : null);
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }

    private ResUpdateDeviceReturnDTO convertToResUpdateDeviceReturnDTO(DeviceReturn r) {
        ResUpdateDeviceReturnDTO res = new ResUpdateDeviceReturnDTO();
        res.setId(r.getId());
        res.setCheckoutId(r.getCheckout() != null ? r.getCheckout().getId() : null);
        res.setReturnTime(r.getReturnTime());
        res.setDeviceStatus(r.getDeviceStatus());
        res.setUpdatedAt(r.getUpdatedAt());
        res.setUpdatedBy(r.getUpdatedBy());
        return res;
    }
}
