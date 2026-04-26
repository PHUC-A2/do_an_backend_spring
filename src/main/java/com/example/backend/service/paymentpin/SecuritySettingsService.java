package com.example.backend.service.paymentpin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.SecuritySettings;
import com.example.backend.domain.response.security.ResSecuritySettingsDTO;
import com.example.backend.repository.SecuritySettingsRepository;
import com.example.backend.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecuritySettingsService {

    private final SecuritySettingsRepository securitySettingsRepository;

    @Transactional(readOnly = true)
    public boolean isPaymentConfirmationPinRequired() {
        return Boolean.TRUE.equals(loadOrNullForContext().getPaymentConfirmationPinRequired());
    }

    @Transactional(readOnly = true)
    public ResSecuritySettingsDTO getDto() {
        SecuritySettings row = loadOrNullForContext();
        return ResSecuritySettingsDTO.builder()
                .paymentConfirmationPinRequired(Boolean.TRUE.equals(row.getPaymentConfirmationPinRequired()))
                .build();
    }

    @Transactional
    public ResSecuritySettingsDTO updatePaymentConfirmationPinRequired(boolean required) {
        SecuritySettings row = getOrCreateMutable();
        row.setPaymentConfirmationPinRequired(required);
        securitySettingsRepository.save(row);
        return ResSecuritySettingsDTO.builder()
                .paymentConfirmationPinRequired(Boolean.TRUE.equals(row.getPaymentConfirmationPinRequired()))
                .build();
    }

    private SecuritySettings loadOrNullForContext() {
        long tid = TenantContext.requireCurrentTenantId();
        return securitySettingsRepository.findByTenantId(tid)
                .orElseGet(() -> {
                    SecuritySettings s = new SecuritySettings();
                    s.setTenantId(tid);
                    s.setPaymentConfirmationPinRequired(false);
                    return s;
                });
    }

    private SecuritySettings getOrCreateMutable() {
        long tid = TenantContext.requireCurrentTenantId();
        return securitySettingsRepository.findByTenantId(tid)
                .orElseGet(() -> {
                    SecuritySettings s = new SecuritySettings();
                    s.setTenantId(tid);
                    s.setPaymentConfirmationPinRequired(false);
                    return securitySettingsRepository.save(s);
                });
    }
}
