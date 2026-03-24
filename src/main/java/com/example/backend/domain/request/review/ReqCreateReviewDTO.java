package com.example.backend.domain.request.review;

import com.example.backend.util.constant.review.ReviewTargetTypeEnum;

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
public class ReqCreateReviewDTO {

    @NotNull(message = "Loại đánh giá không được để trống")
    private ReviewTargetTypeEnum targetType;

    private Long pitchId;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer rating;

    @NotBlank(message = "Nội dung nhận xét không được để trống")
    private String content;
}
