package com.example.backend.domain.response.pitch;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchTypeDTO {
    private Long id;
    private String name;
    private String code;
    private Instant createdAt;
    private Instant updatedAt;
}
