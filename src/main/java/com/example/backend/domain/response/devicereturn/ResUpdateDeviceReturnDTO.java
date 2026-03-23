package com.example.backend.domain.response.devicereturn;

import java.time.Instant;

import com.example.backend.util.constant.devicereturn.DeviceCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateDeviceReturnDTO {

    private Long id;
    private Long checkoutId;
    private Instant returnTime;
    private DeviceCondition deviceStatus;
    private Instant updatedAt;
    private String updatedBy;
}
