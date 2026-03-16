package com.example.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.PasswordResetToken;
import com.example.backend.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {

    public static final String RESET_PASSWORD_TYPE = "RESET_PASSWORD";
    public static final String EMAIL_VERIFICATION_TYPE = "EMAIL_VERIFICATION";

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public PasswordResetToken createToken(String email) {
        return createToken(email, RESET_PASSWORD_TYPE);
    }

    @Transactional
    public PasswordResetToken createToken(String email, String type) {

        // Xóa OTP cũ cùng loại trước khi tạo OTP mới
        passwordResetTokenRepository.deleteAllByEmailAndType(email, type);

        String otp = generateOtp();

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setType(type);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        return passwordResetTokenRepository.save(token);
    }

    @Transactional
    public PasswordResetToken createToken(Long userId, String email, String type) {

        // Xóa OTP cũ cùng loại của đúng user trước khi tạo OTP mới
        passwordResetTokenRepository.deleteAllByUserIdAndEmailAndType(userId, email, type);

        String otp = generateOtp();

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(userId);
        token.setEmail(email);
        token.setOtp(otp);
        token.setType(type);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        return passwordResetTokenRepository.save(token);
    }

    public Optional<PasswordResetToken> findValidToken(String email, String otp) {
        return findValidToken(email, otp, RESET_PASSWORD_TYPE);
    }

    public Optional<PasswordResetToken> findValidToken(String email, String otp, String type) {
        return passwordResetTokenRepository
                .findByEmailAndOtpAndTypeAndExpiryTimeAfter(email, otp, type, LocalDateTime.now());
    }

    public Optional<PasswordResetToken> findValidToken(Long userId, String email, String otp, String type) {
        return passwordResetTokenRepository
                .findByUserIdAndEmailAndOtpAndTypeAndExpiryTimeAfter(userId, email, otp, type, LocalDateTime.now());
    }

    public Optional<PasswordResetToken> findLatestTokenByType(String email, String type) {
        return passwordResetTokenRepository.findTopByEmailAndTypeOrderByIdDesc(email, type);
    }

    public Optional<PasswordResetToken> findLatestTokenByType(Long userId, String email, String type) {
        return passwordResetTokenRepository.findTopByUserIdAndEmailAndTypeOrderByIdDesc(userId, email, type);
    }

    public long getRemainingCooldownSeconds(String email, String type, long cooldownSeconds) {
        Optional<PasswordResetToken> latestTokenOpt = findLatestTokenByType(email, type);
        if (latestTokenOpt.isEmpty()) {
            return 0;
        }

        PasswordResetToken latestToken = latestTokenOpt.get();
        if (latestToken.getExpiryTime() == null) {
            return 0;
        }

        LocalDateTime issuedAt = latestToken.getExpiryTime().minusMinutes(5);
        LocalDateTime availableAt = issuedAt.plusSeconds(cooldownSeconds);
        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(availableAt)) {
            return 0;
        }

        return Duration.between(now, availableAt).toSeconds() + 1;
    }

    public long getRemainingCooldownSeconds(Long userId, String email, String type, long cooldownSeconds) {
        Optional<PasswordResetToken> latestTokenOpt = findLatestTokenByType(userId, email, type);
        if (latestTokenOpt.isEmpty()) {
            return 0;
        }

        PasswordResetToken latestToken = latestTokenOpt.get();
        if (latestToken.getExpiryTime() == null) {
            return 0;
        }

        LocalDateTime issuedAt = latestToken.getExpiryTime().minusMinutes(5);
        LocalDateTime availableAt = issuedAt.plusSeconds(cooldownSeconds);
        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(availableAt)) {
            return 0;
        }

        return Duration.between(now, availableAt).toSeconds() + 1;
    }

    @Transactional
    public void deleteToken(@NonNull PasswordResetToken token) {
        passwordResetTokenRepository.delete(token);
    }
}
