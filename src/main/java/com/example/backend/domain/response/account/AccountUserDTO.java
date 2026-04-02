package com.example.backend.domain.response.account;

import java.util.List;

import com.example.backend.domain.response.role.ResRoleNestedDetailDTO;
import com.example.backend.util.constant.user.NotificationSoundPresetEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUserDTO {
    private Long id;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private Boolean notificationSoundEnabled;

    private NotificationSoundPresetEnum notificationSoundPreset;

    private List<ResRoleNestedDetailDTO> roles;

    /** User đã lưu PIN xác nhận thanh toán hay chưa (không trả giá trị PIN). */
    private Boolean paymentPinConfigured;

    /** Hệ thống có đang bật bắt buộc PIN khi xác nhận thanh toán hay không. */
    private Boolean paymentConfirmationPinRequiredBySystem;
}
