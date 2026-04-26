package com.example.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Plan;
import com.example.backend.domain.entity.Subscription;
import com.example.backend.domain.entity.Tenant;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.subscription.ReqAssignPlanToTenantDTO;
import com.example.backend.domain.request.subscription.ReqRenewSubscriptionDTO;
import com.example.backend.domain.request.subscription.ReqUpgradePlanDTO;
import com.example.backend.domain.response.subscription.ResSubscriptionListDTO;
import com.example.backend.repository.PlanPermissionRepository;
import com.example.backend.repository.PlanRepository;
import com.example.backend.repository.SubscriptionRepository;
import com.example.backend.repository.TenantRepository;
import com.example.backend.repository.TenantUserRepository;
import com.example.backend.util.constant.subscription.PlanStatusEnum;
import com.example.backend.util.constant.subscription.SubscriptionStatusEnum;
import com.example.backend.util.constant.tenant.TenantStatusEnum;
import com.example.backend.util.RoleSecurityUtil;
import com.example.backend.util.error.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    public static final long DEFAULT_TENANT_ID = 1L;

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanPermissionRepository planPermissionRepository;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;

    @Transactional
    public void deleteAllByTenantId(@NonNull Long tenantId) {
        subscriptionRepository.deleteByTenant_Id(tenantId);
    }

    @Transactional(readOnly = true)
    public boolean isTenantSubscriptionActive(@NonNull Long tenantId) {
        if (tenantId == DEFAULT_TENANT_ID) {
            return true;
        }
        return !subscriptionRepository
                .findActiveForTenant(tenantId, SubscriptionStatusEnum.ACTIVE, Instant.now())
                .isEmpty();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Subscription> getActiveSubscription(@NonNull Long tenantId) {
        var list = subscriptionRepository.findActiveForTenant(tenantId, SubscriptionStatusEnum.ACTIVE, Instant.now());
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    @Transactional(readOnly = true)
    public boolean isExpired(@NonNull Long tenantId) {
        if (tenantId == DEFAULT_TENANT_ID) {
            return false;
        }
        return getActiveSubscription(tenantId).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<String> resolveAuthorityNamesForToken(@NonNull User user, Long tenantIdFromToken) {
        if (user.getRoles().stream().anyMatch(RoleSecurityUtil::isGlobalSystemAllRole)) {
            return List.of("ALL");
        }
        long tid = tenantIdFromToken != null ? tenantIdFromToken : DEFAULT_TENANT_ID;
        if (tid == DEFAULT_TENANT_ID) {
            return globalRolePermissionNames(user);
        }
        if (!tenantUserRepository.existsByUser_IdAndTenant_Id(user.getId(), tid)) {
            return List.of();
        }
        Tenant t = tenantRepository.findById(tid).orElse(null);
        if (t == null || t.getStatus() != TenantStatusEnum.APPROVED) {
            return List.of();
        }
        return permissionNamesForActivePlan(tid);
    }

    @Transactional(readOnly = true)
    public String resolvePlanNameForToken(@NonNull User user, Long tenantIdFromToken) {
        if (user.getRoles().stream().anyMatch(RoleSecurityUtil::isGlobalSystemAllRole)) {
            return null;
        }
        long tid = tenantIdFromToken != null ? tenantIdFromToken : DEFAULT_TENANT_ID;
        if (tid == DEFAULT_TENANT_ID) {
            return null;
        }
        return getActiveSubscription(tid).map(s -> s.getPlan().getName()).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<String> planPermissionNamesForUi(@NonNull User user, Long tenantIdFromToken) {
        return List.copyOf(resolveAuthorityNamesForToken(user, tenantIdFromToken));
    }

    private List<String> permissionNamesForActivePlan(long tenantId) {
        return getActiveSubscription(tenantId)
                .map(s -> planPermissionRepository.findPermissionNamesByPlanId(s.getPlan().getId()))
                .orElseGet(List::of);
    }

    private static List<String> globalRolePermissionNames(User user) {
        return user.getRoles().stream()
                .flatMap(r -> r.getPermissions() == null ? Stream.empty() : r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public void assignPlanToTenant(@NonNull ReqAssignPlanToTenantDTO req) {
        Tenant tenant = tenantRepository.findById(req.getTenantId())
                .orElseThrow(() -> new BadRequestException("Tenant không tồn tại"));
        if (tenant.getStatus() != TenantStatusEnum.APPROVED) {
            throw new BadRequestException("Chỉ gán gói cho tenant đã duyệt");
        }
        Plan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new BadRequestException("Gói không tồn tại"));
        if (plan.getStatus() != PlanStatusEnum.ACTIVE) {
            throw new BadRequestException("Gói không còn hiệu lực");
        }
        expireActiveSubscriptionsForTenant(tenant.getId());
        Instant start = req.getStartDate() != null ? req.getStartDate() : Instant.now();
        Instant end = start.plus(plan.getDurationDays(), ChronoUnit.DAYS);
        Subscription s = new Subscription();
        s.setTenant(tenant);
        s.setPlan(plan);
        s.setStartDate(start);
        s.setEndDate(end);
        s.setStatus(SubscriptionStatusEnum.ACTIVE);
        subscriptionRepository.save(s);
    }

    private void expireActiveSubscriptionsForTenant(long tenantId) {
        for (Subscription s : subscriptionRepository.findByTenant_IdOrderByIdDesc(tenantId)) {
            if (s.getStatus() == SubscriptionStatusEnum.ACTIVE) {
                s.setStatus(SubscriptionStatusEnum.EXPIRED);
                subscriptionRepository.save(s);
            }
        }
    }

    @Transactional
    public void renew(@NonNull ReqRenewSubscriptionDTO req) {
        Subscription s = subscriptionRepository.findById(req.getSubscriptionId())
                .orElseThrow(() -> new BadRequestException("Subscription không tồn tại"));
        Plan plan = s.getPlan();
        int days = plan.getDurationDays();
        Instant from = s.getEndDate().isAfter(Instant.now()) ? s.getEndDate() : Instant.now();
        s.setEndDate(from.plus(days, ChronoUnit.DAYS));
        s.setStatus(SubscriptionStatusEnum.ACTIVE);
        subscriptionRepository.save(s);
    }

    @Transactional
    public void upgradePlan(@NonNull ReqUpgradePlanDTO req) {
        Plan newPlan = planRepository.findById(req.getNewPlanId())
                .orElseThrow(() -> new BadRequestException("Gói mới không tồn tại"));
        if (newPlan.getStatus() != PlanStatusEnum.ACTIVE) {
            throw new BadRequestException("Gói mới không khả dụng");
        }
        ReqAssignPlanToTenantDTO a = new ReqAssignPlanToTenantDTO();
        a.setTenantId(req.getTenantId());
        a.setPlanId(req.getNewPlanId());
        a.setStartDate(Instant.now());
        assignPlanToTenant(a);
    }

    @Transactional
    public void downgradePlan(@NonNull ReqUpgradePlanDTO req) {
        upgradePlan(req);
    }

    @Transactional(readOnly = true)
    public List<ResSubscriptionListDTO> listAll() {
        return subscriptionRepository.findAllForList().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    private ResSubscriptionListDTO toListDto(Subscription s) {
        return ResSubscriptionListDTO.builder()
                .id(s.getId())
                .tenantId(s.getTenant().getId())
                .tenantName(s.getTenant().getName())
                .planId(s.getPlan().getId())
                .planName(s.getPlan().getName())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .build();
    }
}
