package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository
                extends JpaRepository<PasswordResetToken, Long> {

        Optional<PasswordResetToken> findByEmailAndOtpAndTypeAndExpiryTimeAfter(
                        String email,
                        String otp,
                        String type,
                        LocalDateTime now);

        Optional<PasswordResetToken> findByUserIdAndEmailAndOtpAndTypeAndExpiryTimeAfter(
                        Long userId,
                        String email,
                        String otp,
                        String type,
                        LocalDateTime now);

        Optional<PasswordResetToken> findByEmailAndOtpAndExpiryTimeAfter(
                        String email,
                        String otp,
                        LocalDateTime now);

        Optional<PasswordResetToken> findTopByEmailAndTypeOrderByIdDesc(String email, String type);

        Optional<PasswordResetToken> findTopByUserIdAndEmailAndTypeOrderByIdDesc(Long userId, String email,
                        String type);

        void deleteAllByEmailAndType(String email, String type);

        void deleteAllByUserIdAndEmailAndType(Long userId, String email, String type);

        void deleteAllByEmail(String email);

}
