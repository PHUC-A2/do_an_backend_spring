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

    private Integer quantityReturnedGood;
    private Integer quantityLost;
    private Integer quantityDamaged;

    private String borrowerSignName;
    private String staffSignName;

    private String returnerNameSnapshot;
    private String returnerPhoneSnapshot;
    private String receiverNameSnapshot;
    private String receiverPhoneSnapshot;

    private String returnConditionNote;
    private Boolean returnReportPrintOptIn;

    private Instant updatedAt;
    private String updatedBy;
}
