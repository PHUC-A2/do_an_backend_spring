package com.example.backend.service.paymentpin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.SecuritySettings;
import com.example.backend.domain.response.security.ResSecuritySettingsDTO;
import com.example.backend.repository.SecuritySettingsRepository;

import lombok.RequiredArgsConstructor;

/**
 * Quản lý bản ghi cấu hình bảo mật singleton (bật/tắt PIN xác nhận thanh toán).
 */
@Service
@RequiredArgsConstructor
public class SecuritySettingsService {

    private final SecuritySettingsRepository securitySettingsRepository;

    @Transactional(readOnly = true)
    public boolean isPaymentConfirmationPinRequired() {
        return securitySettingsRepository.findById(SecuritySettings.SINGLETON_ID)
                .map(s -> Boolean.TRUE.equals(s.getPaymentConfirmationPinRequired()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public ResSecuritySettingsDTO getDto() {
        SecuritySettings row = getOrCreateSingleton();
        return ResSecuritySettingsDTO.builder()
                .paymentConfirmationPinRequired(Boolean.TRUE.equals(row.getPaymentConfirmationPinRequired()))
                .build();
    }

    @Transactional
    public ResSecuritySettingsDTO updatePaymentConfirmationPinRequired(boolean required) {
        SecuritySettings row = getOrCreateSingleton();
        row.setPaymentConfirmationPinRequired(required);
        securitySettingsRepository.save(row);
        return ResSecuritySettingsDTO.builder()
                .paymentConfirmationPinRequired(required)
                .build();
    }

    private SecuritySettings getOrCreateSingleton() {
        return securitySettingsRepository.findById(SecuritySettings.SINGLETON_ID).orElseGet(() -> {
            SecuritySettings created = new SecuritySettings();
            created.setId(SecuritySettings.SINGLETON_ID);
            created.setPaymentConfirmationPinRequired(false);
            return securitySettingsRepository.save(created);
        });
    }
}
