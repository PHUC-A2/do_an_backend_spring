package com.example.backend.domain.request.review;

import com.example.backend.util.constant.review.ReviewStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateReviewStatusDTO {

    @NotNull(message = "Trạng thái không được để trống")
    private ReviewStatusEnum status;
}
