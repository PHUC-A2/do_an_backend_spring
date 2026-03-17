package com.example.backend.domain.request.bookingequipment;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateBookingEquipmentStatusDTO {

    @NotNull(message = "Trạng thái không được để trống")
    private BookingEquipmentStatusEnum status;
}
