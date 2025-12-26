package com.example.backend.domain.request.booking;

import java.time.LocalDateTime;

import com.example.backend.util.constant.booking.ShirtOptionEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateBookingDTO {

    @NotNull(message = "pitchId không được để trống")
    private Long pitchId;

    private Long userId; // thêm cho admin

    @NotNull(message = "thời gian bắt đầu không được để trống")
    private LocalDateTime startDateTime;

    @NotNull(message = "thời gian kết thúc không được để trống")
    private LocalDateTime endDateTime;

    private ShirtOptionEnum shirtOption;

    private String contactPhone;
}
