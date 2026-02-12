package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.PasswordResetToken;
import com.example.backend.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public PasswordResetToken createToken(String email) {

        // Xoá OTP cũ trước
        passwordResetTokenRepository.deleteAllByEmail(email);

        String otp = generateOtp();

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        return passwordResetTokenRepository.save(token);
    }

    public Optional<PasswordResetToken> findValidToken(String email, String otp) {
        return passwordResetTokenRepository
                .findByEmailAndOtpAndExpiryTimeAfter(email, otp, LocalDateTime.now());
    }

    public void deleteToken(@NonNull PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}
