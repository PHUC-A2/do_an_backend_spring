package com.example.backend.domain.request.assetusage;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateClientCheckoutDTO {
    private Instant receiveTime;
    private String conditionNote;
}
