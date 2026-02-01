package com.example.backend.domain.response.timeline;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimeRange {
    private LocalDateTime start;
    private LocalDateTime end;
}