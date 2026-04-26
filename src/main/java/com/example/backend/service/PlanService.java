package com.example.backend.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.entity.Plan;
import com.example.backend.domain.entity.PlanPermission;
import com.example.backend.domain.request.plan.ReqCreatePlanDTO;
import com.example.backend.domain.request.plan.ReqPlanAssignPermissionsDTO;
import com.example.backend.domain.request.plan.ReqUpdatePlanDTO;
import com.example.backend.domain.response.plan.ResPlanDTO;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.PlanPermissionRepository;
import com.example.backend.repository.PlanRepository;
import com.example.backend.repository.SubscriptionRepository;
import com.example.backend.util.constant.subscription.PlanStatusEnum;
import com.example.backend.util.error.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanPermissionRepository planPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public ResPlanDTO create(@NonNull ReqCreatePlanDTO req) {
        Plan p = new Plan();
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setDurationDays(req.getDurationDays());
        p.setStatus(req.getStatus() != null ? req.getStatus() : PlanStatusEnum.ACTIVE);
        p = planRepository.save(p);
        // Mặc định: không gắn quyền — quản trị bật từng quyền ở /admin/plans (drawer) hoặc gọi API full-catalog.
        return toDto(p);
    }

    @Transactional
    public ResPlanDTO update(@NonNull Long id, @NonNull ReqUpdatePlanDTO req) {
        Plan p = planRepository.findById(id).orElseThrow(() -> new BadRequestException("Gói không tồn tại"));
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setDurationDays(req.getDurationDays());
        p.setStatus(req.getStatus());
        return toDto(planRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ResPlanDTO> getAll() {
        return planRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissions(@NonNull Long planId, @NonNull ReqPlanAssignPermissionsDTO req) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BadRequestException("Gói không tồn tại"));
        planPermissionRepository.deleteAllByPlanId(planId);
        if (req.getPermissionIds() == null || req.getPermissionIds().isEmpty()) {
            return;
        }
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        for (Long raw : req.getPermissionIds()) {
            if (raw != null) {
                uniqueIds.add(raw);
            }
        }
        for (Long pid : uniqueIds) {
            Permission perm = permissionRepository.findById(pid)
                    .orElseThrow(() -> new BadRequestException("Quyền không tồn tại: " + pid));
            PlanPermission pp = new PlanPermission();
            pp.setPlan(plan);
            pp.setPermission(perm);
            planPermissionRepository.save(pp);
        }
    }

    /**
     * Gắn toàn bộ quyền trong catalog ({@code permissions}) cho gói — tương đương tập quyền
     * mà trang /api/v1/... dùng (trừ {@code ALL} từ role ADMIN, không tồn tại bảng permission).
     * Mỗi tenant đăng ký gói này sẽ có thể dùng đủ tính năng quản lý nội bộ, cô lập theo
     * {@code TenantContext}.
     */
    @Transactional
    public void replacePlanPermissionsWithFullCatalog(@NonNull Long planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new BadRequestException("Gói không tồn tại"));
        planPermissionRepository.deleteAllByPlanId(planId);
        for (Permission perm : permissionRepository.findAll()) {
            PlanPermission pp = new PlanPermission();
            pp.setPlan(plan);
            pp.setPermission(perm);
            planPermissionRepository.save(pp);
        }
    }

    /** Đồng bộ mọi gói (vd. dữ liệu cũ thiếu quyền) — chạy khi init DB hoặc từ API quản trị. */
    @Transactional
    public void syncAllPlansWithFullPermissionCatalog() {
        for (Plan p : planRepository.findAll()) {
            replacePlanPermissionsWithFullCatalog(p.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionNamesForPlan(@NonNull Long planId) {
        if (!planRepository.existsById(planId)) {
            throw new BadRequestException("Gói không tồn tại");
        }
        return planPermissionRepository.findPermissionNamesByPlanId(planId);
    }

    @Transactional
    public void deleteById(@NonNull Long id) {
        if (!planRepository.existsById(id)) {
            throw new BadRequestException("Gói không tồn tại");
        }
        if (subscriptionRepository.countByPlan_Id(id) > 0) {
            throw new BadRequestException("Không thể xóa: gói đã gắn thuê bao. Hãy vô hiệu hóa gói (trạng thái Tắt) thay vì xóa.");
        }
        planPermissionRepository.deleteAllByPlanId(id);
        planRepository.deleteById(id);
    }

    private ResPlanDTO toDto(Plan p) {
        return ResPlanDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .durationDays(p.getDurationDays())
                .status(p.getStatus())
                .build();
    }
}
