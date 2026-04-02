package com.example.backend.domain.request.systemconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqMessengerConfigDTO {

    @NotBlank(message = "Messenger Page ID không được để trống")
    private String pageId;

    private Boolean active;
}
