package com.example.backend.domain.request.pitch;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.pitch.PitchTypeEnum;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdatePitchDTO {

    @NotBlank(message = "Tên sân không được để trống")
    private String name;

    @NotNull(message = "Loại sân không được để trống")
    private PitchTypeEnum pitchType;

    @NotNull(message = "Giá theo giờ không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal pricePerHour;

    private String pitchUrl;

    private LocalTime openTime;
    private LocalTime closeTime;

    private boolean open24h;

    @NotNull(message = "Trạng thái sân không được để trống")
    private PitchStatusEnum status;

    @NotBlank(message = "Địa chỉ sân không được để trống")
    private String address;

    @NotNull(message = "Vĩ độ không được để trống")
    @Min(value = -90, message = "Vĩ độ tối thiểu là -90")
    @Max(value = 90, message = "Vĩ độ tối đa là 90")
    private Double latitude;

    @NotNull(message = "Kinh độ không được để trống")
    @Min(value = -180, message = "Kinh độ tối thiểu là -180")
    @Max(value = 180, message = "Kinh độ tối đa là 180")
    private Double longitude;
}
