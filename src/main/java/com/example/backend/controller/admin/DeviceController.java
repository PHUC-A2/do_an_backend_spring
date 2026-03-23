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

import com.example.backend.domain.entity.Device;
import com.example.backend.domain.request.device.ReqCreateDeviceDTO;
import com.example.backend.domain.request.device.ReqUpdateDeviceDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.device.ResCreateDeviceDTO;
import com.example.backend.domain.response.device.ResDeviceDetailDTO;
import com.example.backend.domain.response.device.ResUpdateDeviceDTO;
import com.example.backend.service.DeviceService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/devices")
    @ApiMessage("Tạo thiết bị (theo tài sản) mới")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CREATE')")
    public ResponseEntity<ResCreateDeviceDTO> createDevice(
            @Valid @RequestBody @NonNull ReqCreateDeviceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(dto));
    }

    @GetMapping("/devices")
    @ApiMessage("Lấy danh sách thiết bị theo tài sản")
    public ResponseEntity<ResultPaginationDTO> getAllDevices(
            @Filter Specification<Device> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(deviceService.getAllDevices(spec, pageable));
    }

    @GetMapping("/devices/{id}")
    @ApiMessage("Lấy chi tiết thiết bị theo ID")
    public ResponseEntity<ResDeviceDetailDTO> getDeviceById(
            @PathVariable("id") @NonNull Long id) throws IdInvalidException {
        Device d = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceService.convertToResDeviceDetailDTO(d));
    }

    @PutMapping("/devices/{id}")
    @ApiMessage("Cập nhật thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_UPDATE')")
    public ResponseEntity<ResUpdateDeviceDTO> updateDevice(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateDeviceDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(deviceService.updateDevice(id, dto));
    }

    @DeleteMapping("/devices/{id}")
    @ApiMessage("Xóa thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_DELETE')")
    public ResponseEntity<Void> deleteDevice(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }
}
