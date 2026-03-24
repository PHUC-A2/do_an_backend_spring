package com.example.backend.domain.request.review;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqReviewMessageDTO {

    @NotBlank(message = "Nội dung chat không được để trống")
    private String content;
}
