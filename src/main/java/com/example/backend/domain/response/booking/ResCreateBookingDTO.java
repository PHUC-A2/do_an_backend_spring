package com.example.backend.domain.response.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.example.backend.util.constant.booking.ShirtOptionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateBookingDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long pitchId;
    private String pitchName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ShirtOptionEnum shirtOption;
    private String contactPhone;
    private Long durationMinutes;
    private BigDecimal totalPrice;
    private Instant createdAt;
    private String createdBy;
}
