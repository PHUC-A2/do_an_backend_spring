package com.example.backend.service;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.PasswordResetToken;
import com.example.backend.domain.entity.User;
import com.example.backend.repository.UserRepository;
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

        PasswordResetToken token = passwordResetTokenService.createToken(email);

        emailService.sendOtp(email, token.getOtp());
    }

    public void resetPassword(String email, String otp, String newPassword) throws EmailInvalidException {

        PasswordResetToken token = passwordResetTokenService
                .findValidToken(email, otp)
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
}
