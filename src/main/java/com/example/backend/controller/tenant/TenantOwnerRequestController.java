package com.example.backend.controller.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Tenant;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.tenant.ReqOwnerTenantRequestDTO;
import com.example.backend.domain.response.common.MessageResponse;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TenantService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
public class TenantOwnerRequestController {

    private final TenantService tenantService;
    private final UserRepository userRepository;

    @PostMapping("/owner-request")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Gửi yêu cầu đăng ký làm chủ sân (chờ duyệt)")
    public ResponseEntity<MessageResponse> requestOwner(
            @Valid @RequestBody ReqOwnerTenantRequestDTO body) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Chưa đăng nhập"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Không tìm thấy tài khoản");
        }
        Tenant t = tenantService.requestOwnerTenant(user.getId(), body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Đã gửi yêu cầu. Mã yêu cầu tenant: " + t.getId() + " — chờ quản trị duyệt."));
    }
}
