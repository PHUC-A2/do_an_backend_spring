package com.example.backend.domain.response.bookingequipment;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResBookingEquipmentDTO {
    private Long id;
    private Long bookingId;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentImageUrl;
    private Integer quantity;
    private BookingEquipmentStatusEnum status;
    private Long penaltyAmount; // tiền đền khi mất (0 nếu không mất)
    private Long equipmentPrice; // giá tham chiếu để client tính được tiền đền
    private boolean deletedByClient;

    private EquipmentMobilityEnum equipmentMobility;
    private String borrowConditionNote;
    private String returnConditionNote;

    private Integer quantityReturnedGood;
    private Integer quantityLost;
    private Integer quantityDamaged;
    private String borrowerSignName;
    private String staffSignName;
    /** Họ tên người đặt sân (snapshot khi hoàn tất trả). */
    private String bookingBorrowerSnapshot;
}
