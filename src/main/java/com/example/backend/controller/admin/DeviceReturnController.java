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

import com.example.backend.domain.entity.DeviceReturn;
import com.example.backend.domain.request.devicereturn.ReqCreateDeviceReturnDTO;
import com.example.backend.domain.request.devicereturn.ReqUpdateDeviceReturnDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.devicereturn.ResCreateDeviceReturnDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnDetailDTO;
import com.example.backend.domain.response.devicereturn.ResUpdateDeviceReturnDTO;
import com.example.backend.service.DeviceReturnService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class DeviceReturnController {

    private final DeviceReturnService deviceReturnService;

    public DeviceReturnController(DeviceReturnService deviceReturnService) {
        this.deviceReturnService = deviceReturnService;
    }

    @PostMapping("/returns")
    @ApiMessage("Tạo phiếu trả tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('RETURN_CREATE')")
    public ResponseEntity<ResCreateDeviceReturnDTO> createDeviceReturn(
            @Valid @RequestBody @NonNull ReqCreateDeviceReturnDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceReturnService.createDeviceReturn(dto));
    }

    @GetMapping("/returns")
    @ApiMessage("Lấy danh sách phiếu trả")
    public ResponseEntity<ResultPaginationDTO> getAllDeviceReturns(
            @Filter Specification<DeviceReturn> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(deviceReturnService.getAllDeviceReturns(spec, pageable));
    }

    @GetMapping("/returns/{id}")
    @ApiMessage("Lấy chi tiết phiếu trả")
    public ResponseEntity<ResDeviceReturnDetailDTO> getDeviceReturnById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.ok(deviceReturnService.getDeviceReturnDetailById(id));
    }

    @PutMapping("/returns/{id}")
    @ApiMessage("Cập nhật phiếu trả")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('RETURN_UPDATE')")
    public ResponseEntity<ResUpdateDeviceReturnDTO> updateDeviceReturn(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateDeviceReturnDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(deviceReturnService.updateDeviceReturn(id, dto));
    }

    @DeleteMapping("/returns/{id}")
    @ApiMessage("Xóa phiếu trả")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('RETURN_DELETE')")
    public ResponseEntity<Void> deleteDeviceReturn(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        deviceReturnService.deleteDeviceReturn(id);
        return ResponseEntity.ok().build();
    }
}
