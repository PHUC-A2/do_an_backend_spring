package com.example.backend.domain.request.checkout;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateCheckoutDTO {

    @NotNull(message = "Thời điểm nhận không được để trống")
    private Instant receiveTime;

    private String conditionNote;
}
