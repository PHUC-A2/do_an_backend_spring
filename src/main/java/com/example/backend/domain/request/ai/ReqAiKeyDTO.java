package com.example.backend.domain.request.ai;

import com.example.backend.util.constant.ai.AiProviderEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqAiKeyDTO {

    @NotNull(message = "Provider không được để trống")
    private AiProviderEnum provider;

    @NotBlank(message = "API Key không được để trống")
    private String apiKey;

    private String label;
}
