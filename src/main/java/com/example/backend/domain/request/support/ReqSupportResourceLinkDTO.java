package com.example.backend.domain.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSupportResourceLinkDTO {

    @NotBlank(message = "Nhãn không được để trống")
    private String label;

    @NotBlank(message = "URL không được để trống")
    private String url;

    private String color;

    private Integer sortOrder;
}
