package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bản ghi singleton (id=1): cờ bật/tắt yêu cầu PIN khi xác nhận thanh toán.
 * Admin chỉ chỉnh cờ; không lưu PIN người dùng tại đây.
 */
@Entity
@Table(name = "security_settings")
@Getter
@Setter
@NoArgsConstructor
public class SecuritySettings {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "payment_confirmation_pin_required", nullable = false)
    private Boolean paymentConfirmationPinRequired = Boolean.FALSE;
}
