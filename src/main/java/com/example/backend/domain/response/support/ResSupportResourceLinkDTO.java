package com.example.backend.domain.response.support;

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
public class ResSupportResourceLinkDTO {
    private Long id;
    private String label;
    private String url;
    private String color;
    private Integer sortOrder;
}
