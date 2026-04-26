package com.example.backend.controller.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.account.ReqUpdateAccountDTO;
import com.example.backend.domain.request.security.ReqResetPaymentPinWithOtpDTO;
import com.example.backend.domain.request.security.ReqSetPaymentPinDTO;
import com.example.backend.domain.request.auth.ReqLoginDTO;
import com.example.backend.domain.request.auth.ReqResendOtpByEmailDTO;
import com.example.backend.domain.request.auth.ReqResendOtpDTO;
import com.example.backend.domain.request.auth.ReqRegisterDTO;
import com.example.backend.domain.request.auth.ReqSwitchTenantDTO;
import com.example.backend.domain.request.auth.ReqVerifyEmailDTO;
import com.example.backend.domain.request.auth.resetpw.ReqForgotPasswordDTO;
import com.example.backend.domain.request.auth.resetpw.ReqResetPasswordDTO;
import com.example.backend.domain.response.account.AccountUserDTO;
import com.example.backend.domain.response.account.ResAccountDTO;
import com.example.backend.domain.response.account.ResUpdateAccountDTO;
import com.example.backend.domain.response.common.MessageResponse;
import com.example.backend.domain.response.common.RestResponse;
import com.example.backend.domain.response.login.JwtUserDTO;
import com.example.backend.domain.response.login.LoginUserDTO;
import com.example.backend.domain.response.login.ResLoginDTO;
import com.example.backend.domain.response.login.ResTenantDTO;
import com.example.backend.domain.response.permission.ResPermissionNestedDTO;
import com.example.backend.domain.response.role.ResRoleNestedDetailDTO;
import com.example.backend.repository.RoleRepository;
import com.example.backend.service.AuthService;
import com.example.backend.service.EmailService;
import com.example.backend.service.SubscriptionService;
import com.example.backend.service.TenantService;
import com.example.backend.service.UserService;
import com.example.backend.service.paymentpin.PaymentConfirmationPinService;
import com.example.backend.service.paymentpin.SecuritySettingsService;
import com.example.backend.util.RoleSecurityUtil;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.constant.user.NotificationSoundPresetEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.EmailInvalidException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final RoleRepository roleRepository;
        private final AuthService authService;
        private final EmailService emailService;
        private final PaymentConfirmationPinService paymentConfirmationPinService;
        private final SecuritySettingsService securitySettingsService;
        private final TenantService tenantService;
        private final SubscriptionService subscriptionService;

        @Value("${backend.jwt.refresh-token-validity-in-second}")
        private long refreshTokenExpiration;

        public AuthController(
                        AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil,
                        UserService userService,
                        PasswordEncoder passwordEncoder,
                        RoleRepository roleRepository,
                        AuthService authService,
                        EmailService emailService,
                        PaymentConfirmationPinService paymentConfirmationPinService,
                        SecuritySettingsService securitySettingsService,
                        TenantService tenantService,
                        SubscriptionService subscriptionService) {

                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
                this.roleRepository = roleRepository;
                this.authService = authService;
                this.emailService = emailService;
                this.paymentConfirmationPinService = paymentConfirmationPinService;
                this.securitySettingsService = securitySettingsService;
                this.tenantService = tenantService;
                this.subscriptionService = subscriptionService;
        }

        @PostMapping("/auth/login")
        @ApiMessage("Đăng nhập")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {

                User currentUserDB = userService.handleGetUserByUsername(loginDto.getUsername());
                if (currentUserDB != null && currentUserDB.getStatus() != UserStatusEnum.ACTIVE) {
                        String msg = switch (currentUserDB.getStatus()) {
                                case PENDING_VERIFICATION ->
                                        "Tài khoản chưa xác thực email. Vui lòng kiểm tra hộp thư.";
                                case BANNED -> "Tài khoản đã bị khóa.";
                                case INACTIVE -> "Tài khoản đã bị vô hiệu hóa.";
                                default -> "Tài khoản không hợp lệ.";
                        };
                        throw new BadRequestException(msg);
                }

                // 1. Đưa username + password vào Spring Security để xác thực
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDto.getUsername(),
                                loginDto.getPassword());

                // 2. Gọi AuthenticationManager để xác thực
                Authentication authentication = authenticationManagerBuilder
                                .getObject()
                                .authenticate(authenticationToken);

                // 3. Lưu thông tin đăng nhập vào SecurityContext
                // (để dùng cho các request sau)
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 4. Chuẩn bị response trả về cho client
                ResLoginDTO res = new ResLoginDTO();

                // 5. Lấy thông tin user từ database
                if (currentUserDB != null) {

                        // 6. DTO dùng để trả về cho client (response)
                        LoginUserDTO loginUser = new LoginUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getName(),
                                        currentUserDB.getEmail());
                        res.setUser(loginUser);

                        // 7. DTO dùng để nhúng vào JWT (token)
                        Long currentTenantId = tenantService.resolveTenantIdForLogin(currentUserDB);
                        List<String> authorities = subscriptionService.resolveAuthorityNamesForToken(currentUserDB,
                                        currentTenantId);
                        JwtUserDTO jwtUser = buildJwtUser(currentUserDB, currentTenantId, authorities);
                        res.setCurrentTenantId(currentTenantId);

                        // 8.1 Tạo access token
                        String accessToken = securityUtil.createAccessToken(
                                        authentication.getName(), // email / username
                                        jwtUser,
                                        authorities);

                        // 9. Tạo refresh token
                        String refreshToken = securityUtil.createRefreshToken(
                                        authentication.getName(),
                                        jwtUser);

                        // 10. Gắn access token vào response
                        res.setAccessToken(accessToken);

                        // 11. Lưu refresh token vào DB để quản lý phiên đăng nhập
                        userService.updateUserToken(refreshToken, loginDto.getUsername());

                        // 12. Set refresh token vào cookie (httpOnly)
                        ResponseCookie resCookies = ResponseCookie
                                        .from("refresh_token", refreshToken)
                                        .httpOnly(true)
                                        .secure(false)
                                        .sameSite("Lax")
                                        .path("/")
                                        .maxAge(refreshTokenExpiration)
                                        .build();

                        // 13. Trả response cho client
                        return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                        .body(res);
                }

                // Trường hợp không tìm thấy user (hiếm khi xảy ra)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        @GetMapping("/auth/account")
        @ApiMessage("Lấy tài khoản")
        public ResponseEntity<ResAccountDTO> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                User user = userService.handleGetUserByUsername(email);

                AccountUserDTO accountUser = new AccountUserDTO();
                accountUser.setLinkedTenantCount(
                                (int) tenantService.listTenantsForUser(user.getId()).stream()
                                                .filter(t -> t.getId() != com.example.backend.service.TenantService.DEFAULT_TENANT_ID)
                                                .count());
                accountUser.setId(user.getId());
                accountUser.setName(user.getName());
                accountUser.setFullName(user.getFullName());
                accountUser.setEmail(user.getEmail());
                accountUser.setPhoneNumber(user.getPhoneNumber());
                accountUser.setAvatarUrl(user.getAvatarUrl());
                accountUser.setNotificationSoundEnabled(Boolean.TRUE.equals(user.getNotificationSoundEnabled()));
                accountUser.setNotificationSoundPreset(
                                user.getNotificationSoundPreset() != null ? user.getNotificationSoundPreset()
                                                : NotificationSoundPresetEnum.DEFAULT);

                // Chỉ trả cờ PIN cho tài khoản có quyền xác nhận thanh toán (không áp dụng UI
                // user VIEW).
                if (currentAuthenticationHasAnyAuthority("ALL", "PAYMENT_UPDATE")) {
                        accountUser.setPaymentPinConfigured(paymentConfirmationPinService.currentUserHasPaymentPin());
                        accountUser.setPaymentConfirmationPinRequiredBySystem(
                                        securitySettingsService.isPaymentConfirmationPinRequired());
                }

                // map roles + permissions
                accountUser.setRoles(
                                user.getRoles().stream()
                                                .map(role -> {
                                                        ResRoleNestedDetailDTO roleDTO = new ResRoleNestedDetailDTO();
                                                        roleDTO.setId(role.getId());
                                                        roleDTO.setTenantId(role.getTenant() == null ? null
                                                                        : role.getTenant().getId());
                                                        roleDTO.setName(role.getName());
                                                        roleDTO.setDescription(role.getDescription());

                                                        roleDTO.setPermissions(
                                                                        role.getPermissions().stream()
                                                                                        .map(p -> {
                                                                                                ResPermissionNestedDTO pDTO = new ResPermissionNestedDTO();
                                                                                                pDTO.setId(p.getId());
                                                                                                pDTO.setName(p.getName());
                                                                                                pDTO.setDescription(p
                                                                                                                .getDescription());
                                                                                                return pDTO;
                                                                                        })
                                                                                        .toList());

                                                        return roleDTO;
                                                })
                                                .toList());

                Long tidFromToken = null;
                Authentication authn = SecurityContextHolder.getContext().getAuthentication();
                if (authn instanceof JwtAuthenticationToken jat) {
                        tidFromToken = readTenantIdFromUserClaimInJwt(jat.getToken());
                }
                long effTid = tidFromToken != null ? tidFromToken : tenantService.resolveTenantIdForLogin(user);
                if (!currentAuthenticationHasAnyAuthority("ALL")
                                && effTid == com.example.backend.service.SubscriptionService.DEFAULT_TENANT_ID) {
                        effTid = tenantService.preferredNonSystemShopTenantId(user.getId())
                                        .orElse(effTid);
                }
                List<String> eff = subscriptionService.resolveAuthorityNamesForToken(user, effTid);
                accountUser.setEffectivePermissionNames(eff);
                accountUser.setCurrentPlan(subscriptionService.resolvePlanNameForToken(user, effTid));
                if (effTid == SubscriptionService.DEFAULT_TENANT_ID) {
                        accountUser.setSubscriptionActive(true);
                } else {
                        accountUser.setSubscriptionActive(subscriptionService.isTenantSubscriptionActive(effTid));
                }
                subscriptionService.getActiveSubscription(effTid).ifPresent(s -> accountUser.setSubscriptionEndAt(s.getEndDate()));

                ResAccountDTO res = new ResAccountDTO();
                res.setUser(accountUser);

                return ResponseEntity.ok(res);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Lấy refresh_token của người dùng")
        public ResponseEntity<?> getRefreshToken(
                        // @CookieValue(name = "refresh_token", defaultValue = "abc") String
                        // refreshToken)
                        @CookieValue(name = "refresh_token", required = false) String refreshToken) {

                // 1. Không có refresh token trong cookie
                // if ("abc".equals(refreshToken)) {
                // throw new IdInvalidException("Bạn không có refresh token ở cookie");
                // }
                if (refreshToken == null) {
                        return ResponseEntity.noContent().build(); // 204
                }

                Jwt decodedToken;
                String email;

                try {
                        // 2. Kiểm tra refresh token có hợp lệ (chữ ký, hết hạn…)
                        decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
                        // 3. Lấy email (subject) từ token
                        email = decodedToken.getSubject();

                        // 4. Kiểm tra refresh token này có khớp với DB không
                        // → tránh trường hợp token cũ / token bị đánh cắp
                        User currentUser = userService.getUserByRefreshTokenAndEmail(refreshToken, email);
                        if (currentUser == null) {
                                ResponseCookie deleteCookie = ResponseCookie
                                                .from("refresh_token", "")
                                                .httpOnly(true)
                                                .secure(false)
                                                .sameSite("Lax")
                                                .path("/")
                                                .maxAge(0)
                                                .build();

                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                                .build();
                        }

                        if (currentUser.getStatus() == UserStatusEnum.BANNED) {
                                ResponseCookie deleteCookie = ResponseCookie
                                                .from("refresh_token", "")
                                                .httpOnly(true)
                                                .secure(false)
                                                .sameSite("Lax")
                                                .path("/")
                                                .maxAge(0)
                                                .build();

                                RestResponse<Object> response = new RestResponse<>();
                                response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                                response.setError("Unauthorized");
                                response.setMessage("Tài khoản đã bị khóa.");
                                response.setData(null);

                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                                .body(response);
                        }
                } catch (Exception e) {
                        ResponseCookie deleteCookie = ResponseCookie
                                        .from("refresh_token", "")
                                        .httpOnly(true)
                                        .secure(false)
                                        .sameSite("Lax")
                                        .path("/")
                                        .maxAge(0)
                                        .build();

                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                        .build();
                }

                // 5. Chuẩn bị response
                ResLoginDTO res = new ResLoginDTO();

                // 6. Lấy lại user từ DB (để đảm bảo dữ liệu mới nhất)
                User currentUserDB = userService.handleGetUserByUsername(email);

                if (currentUserDB != null) {

                        // 7. DTO trả về cho client
                        LoginUserDTO loginUser = new LoginUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getName(),
                                        currentUserDB.getEmail());
                        res.setUser(loginUser);

                        // 8. DTO nhúng vào JWT — giữ tenant từ refresh token nếu có
                        Long fromRefresh = readTenantIdFromUserClaimInJwt(decodedToken);
                        Long currentTenantId = fromRefresh != null
                                        ? fromRefresh
                                        : tenantService.resolveTenantIdForLogin(currentUserDB);
                        boolean isSystemUser = currentUserDB.getRoles() != null
                                        && currentUserDB.getRoles().stream()
                                                        .anyMatch(RoleSecurityUtil::isGlobalSystemAllRole);
                        if (!isSystemUser && currentTenantId != null
                                        && currentTenantId.longValue() == com.example.backend.service.SubscriptionService.DEFAULT_TENANT_ID) {
                                currentTenantId = tenantService.preferredNonSystemShopTenantId(currentUserDB.getId())
                                                .orElse(currentTenantId);
                        }
                        List<String> authorities = subscriptionService.resolveAuthorityNamesForToken(currentUserDB,
                                        currentTenantId);
                        JwtUserDTO jwtUser = buildJwtUser(currentUserDB, currentTenantId, authorities);
                        res.setCurrentTenantId(currentTenantId);

                        // 9. Tạo access token mới
                        String newAccessToken = securityUtil.createAccessToken(
                                        email,
                                        jwtUser,
                                        authorities);
                        res.setAccessToken(newAccessToken);

                        // 10. Tạo refresh token mới
                        String newRefreshToken = securityUtil.createRefreshToken(
                                        email,
                                        jwtUser);

                        // 11. Cập nhật refresh token mới vào DB
                        userService.updateUserToken(newRefreshToken, email);

                        // 12. Set refresh token mới vào cookie
                        ResponseCookie resCookies = ResponseCookie
                                        .from("refresh_token", newRefreshToken)
                                        .httpOnly(true)
                                        .secure(false)
                                        .sameSite("Lax")
                                        .path("/")
                                        .maxAge(refreshTokenExpiration)
                                        .build();

                        // 13. Trả response
                        return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                        .body(res);
                }

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        @PostMapping("/auth/logout")
        @ApiMessage("Đăng xuất")
        public ResponseEntity<Void> logout() throws IdInvalidException {

                // 1. Lấy email từ access token
                String email = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new IdInvalidException("Access Token không hợp lệ"));

                // 2. Xóa refresh token trong DB
                // → logout tất cả session của user
                userService.updateUserToken(null, email);

                // 3. Xóa refresh token trong cookie
                ResponseCookie deleteCookie = ResponseCookie
                                .from("refresh_token", "")
                                .httpOnly(true)
                                .secure(false)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(0)
                                .build();

                // 4. Trả response
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                .build();
        }

        @PostMapping("/auth/register")
        @ApiMessage("Đăng ký tài khoản")
        public ResponseEntity<MessageResponse> register(@Valid @RequestBody ReqRegisterDTO dto)
                        throws EmailInvalidException {

                User user = this.userService.convertToReqRegisterDTO(dto);

                boolean isEmailExists = this.userService.isEmailExists(user.getEmail());
                if (isEmailExists) {
                        throw new EmailInvalidException("Email: " + user.getEmail() + " đã tồn tại");
                }
                // hardpasswd
                String hardPassword = this.passwordEncoder.encode(dto.getPassword());
                user.setPassword(hardPassword);
                user.setStatus(UserStatusEnum.PENDING_VERIFICATION);

                // Lấy role VIEW và gắn mặc định
                Role viewRole = this.roleRepository.findByNameAndTenantIsNull("VIEW").orElse(null);
                if (viewRole != null) {
                        Set<Role> roles = new HashSet<>();
                        roles.add(viewRole);
                        user.setRoles(roles);
                }

                User savedUser = this.userService.createUserForRegister(user);
                if (dto.getShopName() != null && !dto.getShopName().isBlank()) {
                        tenantService.createOwnerTenantForUser(savedUser.getId(), dto.getShopName());
                }
                this.authService.sendEmailVerificationOtp(savedUser.getId(), savedUser.getEmail());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new MessageResponse(Map.of(
                                                "message",
                                                "Đăng ký tài khoản thành công. Vui lòng xác thực email bằng OTP đã gửi.",
                                                "userId", savedUser.getId(),
                                                "email", savedUser.getEmail())));
        }

        @PostMapping("/auth/verify-email")
        @ApiMessage("Xác thực email")
        public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody ReqVerifyEmailDTO request)
                        throws EmailInvalidException {
                authService.verifyEmail(request.getUserId(), request.getEmail(), request.getOtp());
                return ResponseEntity.ok(new MessageResponse("Xác thực email thành công. Vui lòng đăng nhập."));
        }

        @PostMapping("/auth/resend-otp")
        @ApiMessage("Gửi lại OTP xác thực email")
        public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody ReqResendOtpDTO request) {
                authService.resendVerificationOtp(request.getUserId(), request.getEmail());
                return ResponseEntity.ok(new MessageResponse("Đã gửi lại OTP xác thực email"));
        }

        @PostMapping("/auth/resend-otp-by-email")
        @ApiMessage("Gửi lại OTP xác thực email theo email")
        public ResponseEntity<MessageResponse> resendOtpByEmail(
                        @Valid @RequestBody ReqResendOtpByEmailDTO request) {
                User user = authService.resendVerificationOtpByEmail(request.getEmail());
                return ResponseEntity.ok(new MessageResponse(Map.of(
                                "userId", user.getId(),
                                "email", user.getEmail())));
        }

        @PatchMapping("/auth/account/me")
        @ApiMessage("Cập nhật tài khoản")
        public ResponseEntity<ResUpdateAccountDTO> updateAccount(@Valid @RequestBody ReqUpdateAccountDTO dto) {
                return ResponseEntity.ok(this.userService.updateAccount(dto));
        }

        @PutMapping("/auth/account/payment-pin")
        @ApiMessage("Đặt hoặc đổi PIN xác nhận thanh toán (6 số)")
        @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_UPDATE')")
        public ResponseEntity<Void> setPaymentPin(@Valid @RequestBody ReqSetPaymentPinDTO dto) {
                paymentConfirmationPinService.setOrUpdateMyPaymentPin(dto.getPin(), dto.getCurrentPin());
                return ResponseEntity.ok().build();
        }

        @PostMapping("/auth/account/payment-pin/forgot-otp")
        @ApiMessage("Gửi OTP email đặt lại PIN xác nhận thanh toán")
        @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_UPDATE')")
        public ResponseEntity<MessageResponse> requestForgotPaymentPinOtp() {
                paymentConfirmationPinService.requestResetPaymentPinOtp();
                return ResponseEntity.ok(new MessageResponse("OTP đã gửi về email đăng nhập của bạn"));
        }

        @PatchMapping("/auth/account/payment-pin/reset-with-otp")
        @ApiMessage("Đặt lại PIN xác nhận thanh toán bằng OTP email")
        @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_UPDATE')")
        public ResponseEntity<MessageResponse> resetPaymentPinWithOtp(
                        @Valid @RequestBody ReqResetPaymentPinWithOtpDTO dto) {
                paymentConfirmationPinService.resetPaymentPinWithOtp(dto.getOtp(), dto.getNewPin(),
                                dto.getConfirmPin());
                return ResponseEntity.ok(new MessageResponse("Đã đặt lại PIN xác nhận thanh toán"));
        }

        // Quên pass
        @PostMapping("/auth/forgot-password")
        @ApiMessage("Quên mật khẩu")
        public ResponseEntity<MessageResponse> forgot(@RequestBody ReqForgotPasswordDTO request) {
                authService.forgotPassword(request.getEmail());
                return ResponseEntity.ok(new MessageResponse("Nếu email tồn tại, OTP đã được gửi"));
        }

        @PatchMapping("/auth/reset-password")
        @ApiMessage("Đặt lại mật khẩu")
        public ResponseEntity<MessageResponse> resetPassword(
                        @RequestBody ReqResetPasswordDTO request)
                        throws EmailInvalidException {

                authService.resetPassword(
                                request.getEmail(),
                                request.getOtp(),
                                request.getNewPassword());

                return ResponseEntity.ok(
                                new MessageResponse("Đổi mật khẩu thành công"));
        }

        @PostMapping("/test-mail")
        public ResponseEntity<Void> testMail() {
                this.emailService.sendOtp("phucbv.k63cntt-b@utb.edu.vn", "123456");
                return ResponseEntity.ok(null);
        }

        private boolean currentAuthenticationHasAnyAuthority(String... names) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                        return false;
                }
                for (String name : names) {
                        boolean hit = auth.getAuthorities().stream()
                                        .anyMatch(a -> name.equals(a.getAuthority()));
                        if (hit) {
                                return true;
                        }
                }
                return false;
        }

        @GetMapping("/auth/tenants")
        @ApiMessage("Danh sách tenant của tài khoản")
        public ResponseEntity<List<ResTenantDTO>> listMyTenants() {
                String email = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new BadRequestException("Chưa đăng nhập"));
                User user = userService.handleGetUserByUsername(email);
                var stream = tenantService.listTenantsForUser(user.getId()).stream();
                if (!currentAuthenticationHasAnyAuthority("ALL")) {
                        stream = stream.filter(t -> t.getId() != com.example.backend.service.TenantService.DEFAULT_TENANT_ID);
                }
                List<ResTenantDTO> list = stream
                                .map(t -> {
                                        ResTenantDTO r = new ResTenantDTO();
                                        r.setId(t.getId());
                                        r.setSlug(t.getSlug());
                                        r.setName(t.getName());
                                        r.setStatus(t.getStatus().name());
                                        return r;
                                })
                                .toList();
                return ResponseEntity.ok(list);
        }

        @PostMapping("/auth/switch-tenant")
        @ApiMessage("Đổi tenant (cấp token mới)")
        public ResponseEntity<ResLoginDTO> switchTenant(@Valid @RequestBody ReqSwitchTenantDTO dto) {
                String email = SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new BadRequestException("Chưa đăng nhập"));
                User currentUserDB = userService.handleGetUserByUsername(email);
                if (!tenantService.isUserInTenant(currentUserDB.getId(), dto.getTenantId())) {
                        throw new BadRequestException("Bạn không thuộc tenant này");
                }
                if (!currentAuthenticationHasAnyAuthority("ALL")
                                && dto.getTenantId() != null
                                && dto.getTenantId()
                                                .longValue() == com.example.backend.service.TenantService.DEFAULT_TENANT_ID) {
                        throw new BadRequestException(
                                        "Ngữ cảnh hệ thống (mặc định) chỉ dành cho quản trị hệ thống. Vui lòng chọn cửa hàng.");
                }
                Long tid = dto.getTenantId();
                List<String> authorities = subscriptionService.resolveAuthorityNamesForToken(currentUserDB, tid);
                JwtUserDTO jwtUser = buildJwtUser(currentUserDB, tid, authorities);

                String accessToken = securityUtil.createAccessToken(email, jwtUser, authorities);
                String refreshToken = securityUtil.createRefreshToken(email, jwtUser);
                userService.updateUserToken(refreshToken, email);

                ResLoginDTO res = new ResLoginDTO();
                res.setAccessToken(accessToken);
                res.setUser(new LoginUserDTO(
                                currentUserDB.getId(),
                                currentUserDB.getName(),
                                currentUserDB.getEmail()));
                res.setCurrentTenantId(tid);

                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .httpOnly(true)
                                .secure(false)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(res);
        }

        private JwtUserDTO buildJwtUser(User u, Long tenantId, List<String> authorities) {
                JwtUserDTO j = new JwtUserDTO();
                j.setId(u.getId());
                j.setEmail(u.getEmail());
                j.setName(u.getName());
                j.setTenantId(tenantId);
                j.setPlan(subscriptionService.resolvePlanNameForToken(u, tenantId));
                j.setPermissions(new java.util.ArrayList<>(authorities));
                return j;
        }

        private static Long readTenantIdFromUserClaimInJwt(Jwt jwt) {
                if (jwt == null) {
                        return null;
                }
                Object u = jwt.getClaim("user");
                if (u instanceof Map<?, ?> m) {
                        Object t = m.get("tenantId");
                        if (t instanceof Number n) {
                                return n.longValue();
                        }
                }
                return null;
        }

}