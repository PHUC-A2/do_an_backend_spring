package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cấu hình bảo mật theo từng tenant (PIN khi xác nhận thanh toán).
 */
@Entity
@Table(name = "security_settings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_security_settings_tenant", columnNames = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
public class SecuritySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "payment_confirmation_pin_required", nullable = false)
    private Boolean paymentConfirmationPinRequired = Boolean.FALSE;
}
