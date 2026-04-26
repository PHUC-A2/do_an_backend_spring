package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.BankAccountConfig;
import com.example.backend.domain.entity.EmailSenderConfig;
import com.example.backend.domain.entity.MessengerConfig;
import com.example.backend.domain.request.systemconfig.ReqBankAccountConfigDTO;
import com.example.backend.domain.request.systemconfig.ReqEmailSenderConfigDTO;
import com.example.backend.domain.request.systemconfig.ReqMessengerConfigDTO;
import com.example.backend.domain.response.systemconfig.ResBankAccountConfigDTO;
import com.example.backend.domain.response.systemconfig.ResEmailSenderConfigDTO;
import com.example.backend.domain.response.systemconfig.ResMailCredentialDTO;
import com.example.backend.domain.response.systemconfig.ResMessengerConfigDTO;
import com.example.backend.domain.response.systemconfig.ResPaymentBankInfoDTO;
import com.example.backend.domain.response.systemconfig.ResPublicMessengerConfigDTO;
import com.example.backend.repository.BankAccountConfigRepository;
import com.example.backend.repository.EmailSenderConfigRepository;
import com.example.backend.repository.MessengerConfigRepository;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final EmailSenderConfigRepository emailSenderConfigRepository;
    private final BankAccountConfigRepository bankAccountConfigRepository;
    private final MessengerConfigRepository messengerConfigRepository;
    private final SensitiveDataCryptoService sensitiveDataCryptoService;

    private long requireTenant() {
        return TenantContext.requireCurrentTenantId();
    }

    public List<ResEmailSenderConfigDTO> getAllEmailSenderConfigs() {
        return emailSenderConfigRepository.findByTenantIdOrderByIdDesc(requireTenant()).stream()
                .map(this::toEmailRes)
                .toList();
    }

    public List<ResBankAccountConfigDTO> getAllBankAccountConfigs() {
        return bankAccountConfigRepository.findByTenantIdOrderByIdDesc(requireTenant()).stream()
                .map(this::toBankRes)
                .toList();
    }

    public List<ResMessengerConfigDTO> getAllMessengerConfigs() {
        return messengerConfigRepository.findByTenantIdOrderByIdDesc(requireTenant()).stream()
                .map(this::toMessengerRes)
                .toList();
    }

    @Transactional
    public ResEmailSenderConfigDTO createEmailSenderConfig(ReqEmailSenderConfigDTO req) {
        EmailSenderConfig config = new EmailSenderConfig();
        config.setEmail(req.getEmail().trim());
        config.setPasswordEncrypted(sensitiveDataCryptoService.encrypt(req.getPassword().trim()));
        config.setActive(req.getActive() == null || req.getActive());
        return toEmailRes(emailSenderConfigRepository.save(config));
    }

    @Transactional
    public ResEmailSenderConfigDTO updateEmailSenderConfig(Long id, ReqEmailSenderConfigDTO req) throws IdInvalidException {
        long tid = requireTenant();
        EmailSenderConfig config = emailSenderConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Cấu hình email không tồn tại"));
        config.setEmail(req.getEmail().trim());
        config.setPasswordEncrypted(sensitiveDataCryptoService.encrypt(req.getPassword().trim()));
        if (req.getActive() != null) {
            config.setActive(req.getActive());
        }
        return toEmailRes(emailSenderConfigRepository.save(config));
    }

    @Transactional
    public void deleteEmailSenderConfig(Long id) throws IdInvalidException {
        long tid = requireTenant();
        EmailSenderConfig config = emailSenderConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Cấu hình email không tồn tại"));
        emailSenderConfigRepository.delete(config);
    }

    @Transactional
    public ResBankAccountConfigDTO createBankAccountConfig(ReqBankAccountConfigDTO req) {
        BankAccountConfig config = new BankAccountConfig();
        config.setBankCode(req.getBankCode().trim());
        config.setAccountNoEncrypted(sensitiveDataCryptoService.encrypt(req.getAccountNo().trim()));
        config.setAccountNameEncrypted(sensitiveDataCryptoService.encrypt(req.getAccountName().trim()));
        config.setActive(req.getActive() == null || req.getActive());
        return toBankRes(bankAccountConfigRepository.save(config));
    }

    @Transactional
    public ResBankAccountConfigDTO updateBankAccountConfig(Long id, ReqBankAccountConfigDTO req) throws IdInvalidException {
        long tid = requireTenant();
        BankAccountConfig config = bankAccountConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Tài khoản ngân hàng không tồn tại"));
        config.setBankCode(req.getBankCode().trim());
        config.setAccountNoEncrypted(sensitiveDataCryptoService.encrypt(req.getAccountNo().trim()));
        config.setAccountNameEncrypted(sensitiveDataCryptoService.encrypt(req.getAccountName().trim()));
        if (req.getActive() != null) {
            config.setActive(req.getActive());
        }
        return toBankRes(bankAccountConfigRepository.save(config));
    }

    @Transactional
    public void deleteBankAccountConfig(Long id) throws IdInvalidException {
        long tid = requireTenant();
        BankAccountConfig config = bankAccountConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Tài khoản ngân hàng không tồn tại"));
        bankAccountConfigRepository.delete(config);
    }

    public ResPaymentBankInfoDTO getActivePaymentBankInfo() {
        return getActivePaymentBankInfoForTenant(requireTenant());
    }

    public ResPaymentBankInfoDTO getActivePaymentBankInfoForTenant(long tenantId) {
        BankAccountConfig config = bankAccountConfigRepository.findFirstByActiveTrueAndTenantIdOrderByIdDesc(tenantId)
                .orElseThrow(() -> new BadRequestException("Chưa có cấu hình tài khoản ngân hàng đang bật cho tenant này"));
        return new ResPaymentBankInfoDTO(
                config.getBankCode(),
                sensitiveDataCryptoService.decrypt(config.getAccountNameEncrypted()),
                sensitiveDataCryptoService.decrypt(config.getAccountNoEncrypted()));
    }

    public ResMailCredentialDTO getActiveMailCredential() {
        return getActiveMailCredentialForTenant(TenantService.DEFAULT_TENANT_ID);
    }

    public ResMailCredentialDTO getActiveMailCredentialForTenant(long tenantId) {
        EmailSenderConfig config = emailSenderConfigRepository.findFirstByActiveTrueAndTenantIdOrderByIdDesc(tenantId)
                .orElseGet(() -> emailSenderConfigRepository.findFirstByActiveTrueOrderByIdDesc()
                        .orElseThrow(() -> new BadRequestException("Chưa có cấu hình email gửi đang bật")));
        return new ResMailCredentialDTO(
                config.getEmail(),
                sensitiveDataCryptoService.decrypt(config.getPasswordEncrypted()));
    }

    @Transactional
    public ResMessengerConfigDTO createMessengerConfig(ReqMessengerConfigDTO req) {
        MessengerConfig config = new MessengerConfig();
        config.setPageIdEncrypted(sensitiveDataCryptoService.encrypt(req.getPageId().trim()));
        config.setActive(req.getActive() == null || req.getActive());
        return toMessengerRes(messengerConfigRepository.save(config));
    }

    @Transactional
    public ResMessengerConfigDTO updateMessengerConfig(Long id, ReqMessengerConfigDTO req) throws IdInvalidException {
        long tid = requireTenant();
        MessengerConfig config = messengerConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Cấu hình messenger không tồn tại"));
        config.setPageIdEncrypted(sensitiveDataCryptoService.encrypt(req.getPageId().trim()));
        if (req.getActive() != null) {
            config.setActive(req.getActive());
        }
        return toMessengerRes(messengerConfigRepository.save(config));
    }

    @Transactional
    public void deleteMessengerConfig(Long id) throws IdInvalidException {
        long tid = requireTenant();
        MessengerConfig config = messengerConfigRepository.findByIdAndTenantId(id, tid)
                .orElseThrow(() -> new IdInvalidException("Cấu hình messenger không tồn tại"));
        messengerConfigRepository.delete(config);
    }

    public ResPublicMessengerConfigDTO getPublicMessengerConfig(long tenantId) {
        MessengerConfig config = messengerConfigRepository.findFirstByActiveTrueAndTenantIdOrderByIdDesc(tenantId)
                .orElseGet(() -> messengerConfigRepository.findFirstByActiveTrueOrderByIdDesc()
                        .orElseThrow(() -> new BadRequestException("Chưa có cấu hình messenger đang bật")));
        return new ResPublicMessengerConfigDTO(sensitiveDataCryptoService.decrypt(config.getPageIdEncrypted()));
    }

    private ResEmailSenderConfigDTO toEmailRes(EmailSenderConfig config) {
        return ResEmailSenderConfigDTO.builder()
                .id(config.getId())
                .email(config.getEmail())
                .passwordMasked(maskPassword(config.getPasswordEncrypted()))
                .active(config.isActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private ResBankAccountConfigDTO toBankRes(BankAccountConfig config) {
        return ResBankAccountConfigDTO.builder()
                .id(config.getId())
                .bankCode(config.getBankCode())
                .accountNoMasked(maskAccountNo(sensitiveDataCryptoService.decrypt(config.getAccountNoEncrypted())))
                .accountNameMasked(maskName(sensitiveDataCryptoService.decrypt(config.getAccountNameEncrypted())))
                .active(config.isActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private ResMessengerConfigDTO toMessengerRes(MessengerConfig config) {
        return ResMessengerConfigDTO.builder()
                .id(config.getId())
                .pageIdMasked(maskAccountNo(sensitiveDataCryptoService.decrypt(config.getPageIdEncrypted())))
                .active(config.isActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private String maskPassword(String encryptedPassword) {
        String plainPassword = sensitiveDataCryptoService.decrypt(encryptedPassword);
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new BadRequestException("Mật khẩu email không hợp lệ");
        }
        return "********";
    }

    private String maskAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            return "***";
        }
        if (accountNo.length() <= 4) {
            return "***" + accountNo;
        }
        return "***" + accountNo.substring(accountNo.length() - 4);
    }

    private String maskName(String accountName) {
        if (accountName == null || accountName.isBlank()) {
            return "***";
        }
        return accountName.charAt(0) + "***";
    }

}
