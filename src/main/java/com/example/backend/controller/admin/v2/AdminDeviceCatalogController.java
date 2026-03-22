package com.example.backend.controller.admin.v2;

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

import com.example.backend.domain.entity.v2.RoomDeviceCatalog;
import com.example.backend.domain.request.v2.ReqCreateDeviceCatalogDTO;
import com.example.backend.domain.request.v2.ReqUpdateDeviceCatalogDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.v2.ResDeviceCatalogDTO;
import com.example.backend.service.v2.RoomDeviceCatalogService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v2/admin/device-catalog")
public class AdminDeviceCatalogController {

    private final RoomDeviceCatalogService roomDeviceCatalogService;

    public AdminDeviceCatalogController(RoomDeviceCatalogService roomDeviceCatalogService) {
        this.roomDeviceCatalogService = roomDeviceCatalogService;
    }

    @PostMapping
    @ApiMessage("Tạo danh mục thiết bị thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CATALOG_CREATE')")
    public ResponseEntity<ResDeviceCatalogDTO> create(@Valid @RequestBody ReqCreateDeviceCatalogDTO dto) {
        ResDeviceCatalogDTO res = roomDeviceCatalogService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @ApiMessage("Lấy danh sách danh mục thiết bị thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CATALOG_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<RoomDeviceCatalog> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(roomDeviceCatalogService.getAll(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin danh mục thiết bị thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CATALOG_VIEW_DETAIL')")
    public ResponseEntity<ResDeviceCatalogDTO> getById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.ok(roomDeviceCatalogService.getDtoById(id));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật danh mục thiết bị thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CATALOG_UPDATE')")
    public ResponseEntity<ResDeviceCatalogDTO> update(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateDeviceCatalogDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(roomDeviceCatalogService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa danh mục thiết bị thành công")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('DEVICE_CATALOG_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        roomDeviceCatalogService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
