package com.example.backend.domain.response.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateBookingDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long pitchId;
    private String pitchName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String contactPhone;
    private Long durationMinutes;
    private BigDecimal totalPrice;
    private Instant updatedAt;
    private String updatedBy;
}
