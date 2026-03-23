package com.example.backend.domain.response.devicereturn;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;
import com.example.backend.util.constant.devicereturn.DeviceCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResDeviceReturnDetailDTO {

    private Long id;
    private Long checkoutId;
    private Long assetUsageId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long assetId;
    private String assetName;
    private AssetUsageType usageType;
    private LocalDate usageDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private AssetUsageStatus assetUsageStatus;
    private Instant receiveTime;
    private String checkoutConditionNote;
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
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
