package com.example.backend.controller.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.auth.ReqLoginDTO;
import com.example.backend.domain.request.auth.ReqRegisterDTO;
import com.example.backend.domain.response.account.AccountUserDTO;
import com.example.backend.domain.response.account.ResAccountDTO;
import com.example.backend.domain.response.common.MessageResponse;
import com.example.backend.domain.response.login.JwtUserDTO;
import com.example.backend.domain.response.login.LoginUserDTO;
import com.example.backend.domain.response.login.ResLoginDTO;
import com.example.backend.domain.response.permission.ResPermissionNestedDTO;
import com.example.backend.domain.response.role.ResRoleNestedDetailDTO;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
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

        @Value("${backend.jwt.refresh-token-validity-in-second}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil, UserService userService, PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
        }

        @PostMapping("/auth/login")
        @ApiMessage("Đăng nhập")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {

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
                User currentUserDB = userService.handleGetUserByUsername(loginDto.getUsername());

                if (currentUserDB != null) {

                        // 6. DTO dùng để trả về cho client (response)
                        LoginUserDTO loginUser = new LoginUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        res.setUser(loginUser);

                        // 7. DTO dùng để nhúng vào JWT (token)
                        // DTO này KHÔNG liên quan đến response
                        JwtUserDTO jwtUser = new JwtUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());

                        // 8. Kiểm tra role
                        List<String> authorities;

                        boolean isAdmin = currentUserDB.getRoles().stream()
                                        .anyMatch(r -> r.getName().equals("ADMIN"));

                        if (isAdmin) {
                                authorities = List.of("ALL"); // ADMIN FULL QUYỀN
                        } else {
                                authorities = currentUserDB.getRoles().stream()
                                                .flatMap(r -> r.getPermissions().stream())
                                                .map(p -> p.getName())
                                                .distinct()
                                                .toList();
                        }

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
                                        .secure(true)
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
                accountUser.setId(user.getId());
                accountUser.setName(user.getName());
                accountUser.setFullName(user.getFullName());
                accountUser.setEmail(user.getEmail());
                accountUser.setPhoneNumber(user.getPhoneNumber());
                accountUser.setAvatarUrl(user.getAvatarUrl());

                // map roles + permissions
                accountUser.setRoles(
                user.getRoles().stream()
                        .map(role -> {
                            ResRoleNestedDetailDTO roleDTO = new ResRoleNestedDetailDTO();
                            roleDTO.setId(role.getId());
                            roleDTO.setName(role.getName());
                            roleDTO.setDescription(role.getDescription());

                            roleDTO.setPermissions(
                                    role.getPermissions().stream()
                                            .map(p -> {
                                                ResPermissionNestedDTO pDTO = new ResPermissionNestedDTO();
                                                pDTO.setId(p.getId());
                                                pDTO.setName(p.getName());
                                                pDTO.setDescription(p.getDescription());
                                                return pDTO;
                                            })
                                            .toList());

                            return roleDTO;
                        })
                        .toList());

                ResAccountDTO res = new ResAccountDTO();
                res.setUser(accountUser);

                return ResponseEntity.ok(res);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Lấy refresh_token của người dùng")
        public ResponseEntity<ResLoginDTO> getRefreshToken(
                        @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken)
                        throws IdInvalidException {

                // 1. Không có refresh token trong cookie
                if ("abc".equals(refreshToken)) {
                        throw new IdInvalidException("Bạn không có refresh token ở cookie");
                }

                // 2. Kiểm tra refresh token có hợp lệ (chữ ký, hết hạn…)
                Jwt decodedToken = securityUtil.checkValidRefreshToken(refreshToken);

                // 3. Lấy email (subject) từ token
                String email = decodedToken.getSubject();

                // 4. Kiểm tra refresh token này có khớp với DB không
                // → tránh trường hợp token cũ / token bị đánh cắp
                User currentUser = userService.getUserByRefreshTokenAndEmail(refreshToken, email);
                if (currentUser == null) {
                        throw new IdInvalidException("Refresh Token không hợp lệ");
                }

                // 5. Chuẩn bị response
                ResLoginDTO res = new ResLoginDTO();

                // 6. Lấy lại user từ DB (để đảm bảo dữ liệu mới nhất)
                User currentUserDB = userService.handleGetUserByUsername(email);

                if (currentUserDB != null) {

                        // 7. DTO trả về cho client
                        LoginUserDTO loginUser = new LoginUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        res.setUser(loginUser);

                        // 8. DTO nhúng vào JWT
                        JwtUserDTO jwtUser = new JwtUserDTO(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());

                        // 9. Kiểm tra role
                        List<String> authorities;

                        boolean isAdmin = currentUserDB.getRoles().stream()
                                        .anyMatch(r -> r.getName().equals("ADMIN"));

                        if (isAdmin) {
                                authorities = List.of("ALL"); // 👈 ADMIN FULL QUYỀN
                        } else {
                                authorities = currentUserDB.getRoles().stream()
                                                .flatMap(r -> r.getPermissions().stream())
                                                .map(p -> p.getName())
                                                .distinct()
                                                .toList();
                        }

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
                                        .secure(true)
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
                                .secure(true)
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
                this.userService.createUserForRegister(user);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new MessageResponse("Đăng ký tài khoản thành công"));
        }

}