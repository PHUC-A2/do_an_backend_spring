package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.support.ReqSupportContactDTO;
import com.example.backend.domain.request.support.ReqSupportIssueGuideDTO;
import com.example.backend.domain.request.support.ReqSupportMaintenanceItemDTO;
import com.example.backend.domain.request.support.ReqSupportResourceLinkDTO;
import com.example.backend.domain.response.support.ResSupportContactDTO;
import com.example.backend.domain.response.support.ResSupportIssueGuideDTO;
import com.example.backend.domain.response.support.ResSupportMaintenanceItemDTO;
import com.example.backend.domain.response.support.ResSupportResourceLinkDTO;
import com.example.backend.service.SupportPageService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/admin/support")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportPageService supportPageService;

    // ─── Liên hệ kỹ thuật ──────────────────────────────────────

    @GetMapping("/contacts")
    @ApiMessage("Lấy danh sách liên hệ hỗ trợ")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_VIEW_LIST')")
    public ResponseEntity<List<ResSupportContactDTO>> listContacts() {
        return ResponseEntity.ok(supportPageService.listContacts());
    }

    @PostMapping("/contacts")
    @ApiMessage("Thêm liên hệ hỗ trợ")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportContactDTO> createContact(@Valid @RequestBody ReqSupportContactDTO req) {
        return ResponseEntity.ok(supportPageService.createContact(req));
    }

    @PutMapping("/contacts/{id}")
    @ApiMessage("Cập nhật liên hệ hỗ trợ")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportContactDTO> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ReqSupportContactDTO req) throws IdInvalidException {
        return ResponseEntity.ok(supportPageService.updateContact(id, req));
    }

    @DeleteMapping("/contacts/{id}")
    @ApiMessage("Xóa liên hệ hỗ trợ")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) throws IdInvalidException {
        supportPageService.deleteContact(id);
        return ResponseEntity.ok().build();
    }

    // ─── Hướng dẫn khắc phục sự cố ─────────────────────────────

    @GetMapping("/issue-guides")
    @ApiMessage("Lấy danh sách hướng dẫn sự cố")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_VIEW_LIST')")
    public ResponseEntity<List<ResSupportIssueGuideDTO>> listIssueGuides() {
        return ResponseEntity.ok(supportPageService.listIssueGuides());
    }

    @PostMapping("/issue-guides")
    @ApiMessage("Thêm hướng dẫn sự cố")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportIssueGuideDTO> createIssueGuide(
            @Valid @RequestBody ReqSupportIssueGuideDTO req) {
        return ResponseEntity.ok(supportPageService.createIssueGuide(req));
    }

    @PutMapping("/issue-guides/{id}")
    @ApiMessage("Cập nhật hướng dẫn sự cố")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportIssueGuideDTO> updateIssueGuide(
            @PathVariable Long id,
            @Valid @RequestBody ReqSupportIssueGuideDTO req) throws IdInvalidException {
        return ResponseEntity.ok(supportPageService.updateIssueGuide(id, req));
    }

    @DeleteMapping("/issue-guides/{id}")
    @ApiMessage("Xóa hướng dẫn sự cố")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<Void> deleteIssueGuide(@PathVariable Long id) throws IdInvalidException {
        supportPageService.deleteIssueGuide(id);
        return ResponseEntity.ok().build();
    }

    // ─── Liên kết tài nguyên ───────────────────────────────────

    @GetMapping("/resource-links")
    @ApiMessage("Lấy danh sách liên kết tài nguyên")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_VIEW_LIST')")
    public ResponseEntity<List<ResSupportResourceLinkDTO>> listResourceLinks() {
        return ResponseEntity.ok(supportPageService.listResourceLinks());
    }

    @PostMapping("/resource-links")
    @ApiMessage("Thêm liên kết tài nguyên")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportResourceLinkDTO> createResourceLink(
            @Valid @RequestBody ReqSupportResourceLinkDTO req) {
        return ResponseEntity.ok(supportPageService.createResourceLink(req));
    }

    @PutMapping("/resource-links/{id}")
    @ApiMessage("Cập nhật liên kết tài nguyên")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportResourceLinkDTO> updateResourceLink(
            @PathVariable Long id,
            @Valid @RequestBody ReqSupportResourceLinkDTO req) throws IdInvalidException {
        return ResponseEntity.ok(supportPageService.updateResourceLink(id, req));
    }

    @DeleteMapping("/resource-links/{id}")
    @ApiMessage("Xóa liên kết tài nguyên")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<Void> deleteResourceLink(@PathVariable Long id) throws IdInvalidException {
        supportPageService.deleteResourceLink(id);
        return ResponseEntity.ok().build();
    }

    // ─── Ghi chú bảo trì ───────────────────────────────────────

    @GetMapping("/maintenance-items")
    @ApiMessage("Lấy danh sách ghi chú bảo trì")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_VIEW_LIST')")
    public ResponseEntity<List<ResSupportMaintenanceItemDTO>> listMaintenanceItems() {
        return ResponseEntity.ok(supportPageService.listMaintenanceItems());
    }

    @PostMapping("/maintenance-items")
    @ApiMessage("Thêm ghi chú bảo trì")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportMaintenanceItemDTO> createMaintenanceItem(
            @Valid @RequestBody ReqSupportMaintenanceItemDTO req) {
        return ResponseEntity.ok(supportPageService.createMaintenanceItem(req));
    }

    @PutMapping("/maintenance-items/{id}")
    @ApiMessage("Cập nhật ghi chú bảo trì")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<ResSupportMaintenanceItemDTO> updateMaintenanceItem(
            @PathVariable Long id,
            @Valid @RequestBody ReqSupportMaintenanceItemDTO req) throws IdInvalidException {
        return ResponseEntity.ok(supportPageService.updateMaintenanceItem(id, req));
    }

    @DeleteMapping("/maintenance-items/{id}")
    @ApiMessage("Xóa ghi chú bảo trì")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SUPPORT_MANAGE')")
    public ResponseEntity<Void> deleteMaintenanceItem(@PathVariable Long id) throws IdInvalidException {
        supportPageService.deleteMaintenanceItem(id);
        return ResponseEntity.ok().build();
    }
}
