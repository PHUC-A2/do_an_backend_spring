package com.example.backend.domain.response.bookingequipment;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;

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
}
