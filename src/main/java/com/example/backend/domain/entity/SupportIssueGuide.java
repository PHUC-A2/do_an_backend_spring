package com.example.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.backend.domain.entity.base.BaseTenantEntity;
import com.example.backend.util.constant.support.SupportIssueSeverityEnum;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_issue_guides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class SupportIssueGuide extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SupportIssueSeverityEnum severity = SupportIssueSeverityEnum.MEDIUM;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "support_issue_guide_steps", joinColumns = @JoinColumn(name = "guide_id"))
    @OrderColumn(name = "line_idx")
    @Column(name = "step_line", columnDefinition = "TEXT")
    private List<String> steps = new ArrayList<>();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
