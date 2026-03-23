package com.example.backend.domain.response.checkout;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateCheckoutDTO {

    private Long id;
    private Long assetUsageId;
    private Instant receiveTime;
    private String conditionNote;
    private Instant updatedAt;
    private String updatedBy;
}
