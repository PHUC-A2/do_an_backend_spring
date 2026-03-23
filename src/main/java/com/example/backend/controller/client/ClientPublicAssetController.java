package com.example.backend.controller.client;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Asset;
import com.example.backend.domain.response.asset.ResAssetDetailDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.device.ResDeviceListDTO;
import com.example.backend.domain.response.timeline.room.ResAssetRoomTimelineDTO;
import com.example.backend.service.AssetService;
import com.example.backend.service.DeviceService;
import com.example.backend.service.PublicAssetRoomTimelineService;
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
    private final PublicAssetRoomTimelineService publicAssetRoomTimelineService;
    private final DeviceService deviceService;

    public ClientPublicAssetController(
            AssetService assetService,
            PublicAssetRoomTimelineService publicAssetRoomTimelineService,
            DeviceService deviceService) {
        this.assetService = assetService;
        this.publicAssetRoomTimelineService = publicAssetRoomTimelineService;
        this.deviceService = deviceService;
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

    /**
     * Timeline phòng tin: PERIODS = 10 tiết/ngày; FLEXIBLE = các khoảng đã đặt (đặt giờ tùy chọn qua AssetUsage).
     */
    @GetMapping("/assets/{assetId}/room-timeline")
    @ApiMessage("Lấy timeline phòng tin theo ngày")
    public ResponseEntity<ResAssetRoomTimelineDTO> getPublicAssetRoomTimeline(
            @PathVariable("assetId") @NonNull Long assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NonNull LocalDate date,
            @RequestParam(defaultValue = "PERIODS") String mode) {
        return ResponseEntity.ok(publicAssetRoomTimelineService.getRoomTimeline(assetId, date, mode));
    }

    @GetMapping("/assets/{assetId}/devices")
    @ApiMessage("Lấy danh sách thiết bị theo phòng (public)")
    public ResponseEntity<java.util.List<ResDeviceListDTO>> getPublicAssetDevices(
            @PathVariable("assetId") @NonNull Long assetId) {
        return ResponseEntity.ok(deviceService.getDevicesByAssetId(assetId));
    }
}
