package com.example.backend.service;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.PasswordResetToken;
import com.example.backend.domain.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.EmailInvalidException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordResetTokenService passwordResetTokenService,
            EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void forgotPassword(String email) {

        boolean exists = userRepository.existsByEmail(email);

        if (!exists) {
            return; // tránh lộ email
        }

        PasswordResetToken token = passwordResetTokenService.createToken(email,
                PasswordResetTokenService.RESET_PASSWORD_TYPE);

        emailService.sendOtp(email, token.getOtp());
    }

    public void resetPassword(String email, String otp, String newPassword) throws EmailInvalidException {

        PasswordResetToken token = passwordResetTokenService
                .findValidToken(email, otp, PasswordResetTokenService.RESET_PASSWORD_TYPE)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new EmailInvalidException("OTP đã hết hạn");
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new BadRequestException("Email không tồn tại");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenService.deleteToken(token);
    }

    public void sendEmailVerificationOtp(Long userId, String email) {
        PasswordResetToken token = passwordResetTokenService.createToken(userId, email,
                PasswordResetTokenService.EMAIL_VERIFICATION_TYPE);

        emailService.sendOtp(
                email,
                token.getOtp(),
                "Xác thực email tài khoản",
                "Mã OTP xác thực email của bạn là: ");
    }

    public void verifyEmail(Long userId, String email, String otp) throws EmailInvalidException {

        PasswordResetToken token = passwordResetTokenService
                .findValidToken(userId, email, otp, PasswordResetTokenService.EMAIL_VERIFICATION_TYPE)
                .orElse(null);

        if (token == null) {
            PasswordResetToken latestToken = passwordResetTokenService
                    .findLatestTokenByType(userId, email, PasswordResetTokenService.EMAIL_VERIFICATION_TYPE)
                    .orElse(null);

            if (latestToken != null && otp.equals(latestToken.getOtp())
                    && latestToken.getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new EmailInvalidException("OTP đã hết hạn");
            }

            throw new BadRequestException("OTP không hợp lệ");
        }

        User user = userRepository.findByIdAndEmail(userId, email)
                .orElseThrow(() -> new BadRequestException("Thông tin userId hoặc email không hợp lệ"));

        if (user.getStatus() != UserStatusEnum.PENDING_VERIFICATION) {
            throw new BadRequestException("Tài khoản không ở trạng thái chờ xác thực email");
        }

        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);

        passwordResetTokenService.deleteToken(token);
    }

    public void resendVerificationOtp(Long userId, String email) {
        User user = userRepository.findByIdAndEmail(userId, email)
                .orElseThrow(() -> new BadRequestException("Thông tin userId hoặc email không hợp lệ"));

        if (user.getStatus() != UserStatusEnum.PENDING_VERIFICATION) {
            throw new BadRequestException("Tài khoản không ở trạng thái chờ xác thực email");
        }

        long remainSeconds = passwordResetTokenService.getRemainingCooldownSeconds(
                userId,
                email,
                PasswordResetTokenService.EMAIL_VERIFICATION_TYPE,
                60);

        if (remainSeconds > 0) {
            throw new BadRequestException("Vui lòng thử lại sau " + remainSeconds + " giây");
        }

        sendEmailVerificationOtp(user.getId(), user.getEmail());
    }
}
