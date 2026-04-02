package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.security.ReqUpdateSecuritySettingsDTO;
import com.example.backend.domain.request.systemconfig.ReqBankAccountConfigDTO;
import com.example.backend.domain.request.systemconfig.ReqEmailSenderConfigDTO;
import com.example.backend.domain.request.systemconfig.ReqMessengerConfigDTO;
import com.example.backend.domain.response.security.ResSecuritySettingsDTO;
import com.example.backend.domain.response.systemconfig.ResBankAccountConfigDTO;
import com.example.backend.domain.response.systemconfig.ResEmailSenderConfigDTO;
import com.example.backend.domain.response.systemconfig.ResMessengerConfigDTO;
import com.example.backend.service.SystemConfigService;
import com.example.backend.service.paymentpin.SecuritySettingsService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/system-config")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;
    private final SecuritySettingsService securitySettingsService;

    @GetMapping("/email-senders")
    @ApiMessage("Lấy danh sách cấu hình email gửi")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MAIL_VIEW_LIST')")
    public ResponseEntity<List<ResEmailSenderConfigDTO>> getEmailSenderConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllEmailSenderConfigs());
    }

    @PostMapping("/email-senders")
    @ApiMessage("Tạo cấu hình email gửi")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MAIL_CREATE')")
    public ResponseEntity<ResEmailSenderConfigDTO> createEmailSenderConfig(
            @Valid @RequestBody ReqEmailSenderConfigDTO req) {
        return ResponseEntity.ok(systemConfigService.createEmailSenderConfig(req));
    }

    @PutMapping("/email-senders/{id}")
    @ApiMessage("Cập nhật cấu hình email gửi")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MAIL_UPDATE')")
    public ResponseEntity<ResEmailSenderConfigDTO> updateEmailSenderConfig(
            @PathVariable Long id,
            @Valid @RequestBody ReqEmailSenderConfigDTO req) throws IdInvalidException {
        return ResponseEntity.ok(systemConfigService.updateEmailSenderConfig(id, req));
    }

    @DeleteMapping("/email-senders/{id}")
    @ApiMessage("Xóa cấu hình email gửi")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MAIL_DELETE')")
    public ResponseEntity<Void> deleteEmailSenderConfig(@PathVariable Long id) throws IdInvalidException {
        systemConfigService.deleteEmailSenderConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bank-accounts")
    @ApiMessage("Lấy danh sách tài khoản ngân hàng thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_BANK_VIEW_LIST')")
    public ResponseEntity<List<ResBankAccountConfigDTO>> getBankAccountConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllBankAccountConfigs());
    }

    @PostMapping("/bank-accounts")
    @ApiMessage("Tạo tài khoản ngân hàng thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_BANK_CREATE')")
    public ResponseEntity<ResBankAccountConfigDTO> createBankAccountConfig(
            @Valid @RequestBody ReqBankAccountConfigDTO req) {
        return ResponseEntity.ok(systemConfigService.createBankAccountConfig(req));
    }

    @PutMapping("/bank-accounts/{id}")
    @ApiMessage("Cập nhật tài khoản ngân hàng thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_BANK_UPDATE')")
    public ResponseEntity<ResBankAccountConfigDTO> updateBankAccountConfig(
            @PathVariable Long id,
            @Valid @RequestBody ReqBankAccountConfigDTO req) throws IdInvalidException {
        return ResponseEntity.ok(systemConfigService.updateBankAccountConfig(id, req));
    }

    @DeleteMapping("/bank-accounts/{id}")
    @ApiMessage("Xóa tài khoản ngân hàng thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_BANK_DELETE')")
    public ResponseEntity<Void> deleteBankAccountConfig(@PathVariable Long id) throws IdInvalidException {
        systemConfigService.deleteBankAccountConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messenger")
    @ApiMessage("Lấy danh sách cấu hình messenger")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MESSENGER_VIEW_LIST')")
    public ResponseEntity<List<ResMessengerConfigDTO>> getMessengerConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllMessengerConfigs());
    }

    @PostMapping("/messenger")
    @ApiMessage("Tạo cấu hình messenger")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MESSENGER_CREATE')")
    public ResponseEntity<ResMessengerConfigDTO> createMessengerConfig(
            @Valid @RequestBody ReqMessengerConfigDTO req) {
        return ResponseEntity.ok(systemConfigService.createMessengerConfig(req));
    }

    @PutMapping("/messenger/{id}")
    @ApiMessage("Cập nhật cấu hình messenger")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MESSENGER_UPDATE')")
    public ResponseEntity<ResMessengerConfigDTO> updateMessengerConfig(
            @PathVariable Long id,
            @Valid @RequestBody ReqMessengerConfigDTO req) throws IdInvalidException {
        return ResponseEntity.ok(systemConfigService.updateMessengerConfig(id, req));
    }

    @DeleteMapping("/messenger/{id}")
    @ApiMessage("Xóa cấu hình messenger")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_MESSENGER_DELETE')")
    public ResponseEntity<Void> deleteMessengerConfig(@PathVariable Long id) throws IdInvalidException {
        systemConfigService.deleteMessengerConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/security")
    @ApiMessage("Lấy cấu hình bảo mật bổ sung (PIN xác nhận thanh toán)")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_SECURITY_VIEW_LIST')")
    public ResponseEntity<ResSecuritySettingsDTO> getSecuritySettings() {
        return ResponseEntity.ok(securitySettingsService.getDto());
    }

    @PatchMapping("/security")
    @ApiMessage("Cập nhật bắt buộc PIN khi xác nhận thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('SYSTEM_CONFIG_SECURITY_UPDATE')")
    public ResponseEntity<ResSecuritySettingsDTO> patchSecuritySettings(
            @Valid @RequestBody ReqUpdateSecuritySettingsDTO req) {
        return ResponseEntity.ok(securitySettingsService.updatePaymentConfirmationPinRequired(
                Boolean.TRUE.equals(req.getPaymentConfirmationPinRequired())));
    }
}
