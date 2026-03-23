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

import com.example.backend.domain.entity.DeviceIssue;
import com.example.backend.domain.request.deviceissue.ReqCreateDeviceIssueDTO;
import com.example.backend.domain.request.deviceissue.ReqUpdateDeviceIssueDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.deviceissue.ResCreateDeviceIssueDTO;
import com.example.backend.domain.response.deviceissue.ResDeviceIssueDetailDTO;
import com.example.backend.domain.response.deviceissue.ResUpdateDeviceIssueDTO;
import com.example.backend.service.DeviceIssueService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class DeviceIssueController {

    private final DeviceIssueService deviceIssueService;

    public DeviceIssueController(DeviceIssueService deviceIssueService) {
        this.deviceIssueService = deviceIssueService;
    }

    @PostMapping("/device-issues")
    @ApiMessage("Tạo báo cáo sự cố thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_ISSUE_CREATE')")
    public ResponseEntity<ResCreateDeviceIssueDTO> createDeviceIssue(
            @Valid @RequestBody @NonNull ReqCreateDeviceIssueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceIssueService.createDeviceIssue(dto));
    }

    @GetMapping("/device-issues")
    @ApiMessage("Lấy danh sách sự cố thiết bị")
    public ResponseEntity<ResultPaginationDTO> getAllDeviceIssues(
            @Filter Specification<DeviceIssue> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(deviceIssueService.getAllDeviceIssues(spec, pageable));
    }

    @GetMapping("/device-issues/{id}")
    @ApiMessage("Lấy chi tiết sự cố thiết bị")
    public ResponseEntity<ResDeviceIssueDetailDTO> getDeviceIssueById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.ok(deviceIssueService.getDeviceIssueDetailById(id));
    }

    @PutMapping("/device-issues/{id}")
    @ApiMessage("Cập nhật sự cố thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_ISSUE_UPDATE')")
    public ResponseEntity<ResUpdateDeviceIssueDTO> updateDeviceIssue(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateDeviceIssueDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(deviceIssueService.updateDeviceIssue(id, dto));
    }

    @DeleteMapping("/device-issues/{id}")
    @ApiMessage("Xóa sự cố thiết bị")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_ISSUE_DELETE')")
    public ResponseEntity<Void> deleteDeviceIssue(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        deviceIssueService.deleteDeviceIssue(id);
        return ResponseEntity.ok().build();
    }
}
