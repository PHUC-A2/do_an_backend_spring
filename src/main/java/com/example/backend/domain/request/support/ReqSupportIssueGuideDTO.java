package com.example.backend.domain.request.support;

import java.util.ArrayList;
import java.util.List;

import com.example.backend.util.constant.support.SupportIssueSeverityEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSupportIssueGuideDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotNull(message = "Mức độ nghiêm trọng là bắt buộc")
    private SupportIssueSeverityEnum severity;

    @NotEmpty(message = "Cần ít nhất một bước xử lý")
    private List<@NotBlank(message = "Mỗi bước không được để trống") String> steps = new ArrayList<>();

    private Integer sortOrder;
}
