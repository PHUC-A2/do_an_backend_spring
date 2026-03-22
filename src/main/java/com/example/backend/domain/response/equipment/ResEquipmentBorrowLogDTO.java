package com.example.backend.domain.response.equipment;

import java.time.Instant;

import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentBorrowLogDTO {
    private Long id;
    private Long bookingEquipmentId;
    private Long bookingId;
    private Long equipmentId;
    private String equipmentName;
    private EquipmentBorrowLogTypeEnum logType;
    private String notes;
    private Instant createdAt;
    private String createdBy;

    /** Người đặt booking (tham chiếu). */
    private String bookingUserName;
    private String bookingUserPhone;

    private String pitchName;

    /** Người thực hiện ghi nhận mượn/trả tại thời điểm log. */
    private String actorName;
    private String actorPhone;

    private Boolean borrowConditionAcknowledged;
    private Boolean borrowReportPrintOptIn;
    private String returnerNameSnapshot;
    private String returnerPhoneSnapshot;
    private Boolean returnReportPrintOptIn;

    private String receiverNameSnapshot;
    private String receiverPhoneSnapshot;

    private Boolean returnAdminConfirmed;
}
