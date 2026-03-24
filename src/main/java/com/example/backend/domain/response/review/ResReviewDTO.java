package com.example.backend.domain.response.review;

import java.time.Instant;

import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.constant.review.ReviewTargetTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResReviewDTO {
    private Long id;
    private ReviewTargetTypeEnum targetType;
    private Long pitchId;
    private String pitchName;
    private Integer rating;
    private String content;
    private ReviewStatusEnum status;
    private Long userId;
    private String userName;
    private String userFullName;
    private Instant createdAt;
    private Instant updatedAt;
}
