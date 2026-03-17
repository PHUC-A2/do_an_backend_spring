package com.example.backend.domain.request.equipment;

import java.math.BigDecimal;

import com.example.backend.util.constant.equipment.EquipmentStatusEnum;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateEquipmentDTO {

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Tổng số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer totalQuantity;

    @NotNull(message = "Giá thiết bị không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải >= 0")
    private BigDecimal price;

    private String imageUrl; // tên file ảnh, ví dụ: ball.jpg

    private EquipmentStatusEnum status = EquipmentStatusEnum.ACTIVE;
}
