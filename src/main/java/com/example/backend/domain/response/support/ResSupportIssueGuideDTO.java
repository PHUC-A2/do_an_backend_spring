package com.example.backend.domain.response.support;

import java.util.List;

import com.example.backend.util.constant.support.SupportIssueSeverityEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResSupportIssueGuideDTO {
    private Long id;
    private String title;
    private SupportIssueSeverityEnum severity;
    private List<String> steps;
    private Integer sortOrder;
}
