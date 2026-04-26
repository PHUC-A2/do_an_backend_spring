package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.subscription.ReqAssignPlanToTenantDTO;
import com.example.backend.domain.request.subscription.ReqRenewSubscriptionDTO;
import com.example.backend.domain.request.subscription.ReqUpgradePlanDTO;
import com.example.backend.domain.response.subscription.ResSubscriptionListDTO;
import com.example.backend.service.SubscriptionService;
import com.example.backend.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Danh sách subscription")
    public ResponseEntity<List<ResSubscriptionListDTO>> list() {
        return ResponseEntity.ok(subscriptionService.listAll());
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Gán gói cho tenant")
    public ResponseEntity<Void> assign(@Valid @RequestBody ReqAssignPlanToTenantDTO body) {
        subscriptionService.assignPlanToTenant(body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/renew")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Gia hạn subscription")
    public ResponseEntity<Void> renew(@Valid @RequestBody ReqRenewSubscriptionDTO body) {
        subscriptionService.renew(body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upgrade")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Nâng cấp gói")
    public ResponseEntity<Void> upgrade(@Valid @RequestBody ReqUpgradePlanDTO body) {
        subscriptionService.upgradePlan(body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/downgrade")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Hạ gói")
    public ResponseEntity<Void> downgrade(@Valid @RequestBody ReqUpgradePlanDTO body) {
        subscriptionService.downgradePlan(body);
        return ResponseEntity.noContent().build();
    }
}
