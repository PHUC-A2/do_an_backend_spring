package com.example.backend.service.paymentpin;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.PasswordResetToken;
import com.example.backend.domain.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.PasswordResetTokenService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.error.BadRequestException;

import lombok.RequiredArgsConstructor;

/**
 * Lớp bảo mật bổ sung: chỉ phục vụ xác nhận thanh toán và tự quản lý PIN của user.
 * PIN lưu dưới dạng hash BCrypt (không lưu plaintext).
 */
@Service
@RequiredArgsConstructor
public class PaymentConfirmationPinService {

    private static final Pattern SIX_DIGITS = Pattern.compile("^\\d{6}$");

    private final SecuritySettingsService securitySettingsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    /**
     * Nếu cấu hình bật: bắt buộc PIN đúng với user đang đăng nhập; nếu tắt thì bỏ qua.
     */
    @Transactional(readOnly = true)
    public void requireValidPinIfConfigured(String pinFromRequest) {
        if (!securitySettingsService.isPaymentConfirmationPinRequired()) {
            return;
        }
        if (pinFromRequest == null || pinFromRequest.isBlank()) {
            throw new BadRequestException("Hệ thống yêu cầu nhập PIN 6 số để xác nhận thanh toán");
        }
        if (!SIX_DIGITS.matcher(pinFromRequest).matches()) {
            throw new BadRequestException("PIN phải gồm đúng 6 chữ số");
        }
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không xác định người dùng hiện tại"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Không tìm thấy tài khoản");
        }
        String hash = user.getPaymentPinHash();
        if (hash == null || hash.isBlank()) {
            throw new BadRequestException(
                    "Bạn chưa thiết lập PIN xác nhận thanh toán. Vui lòng cài đặt trong phần tài khoản.");
        }
        if (!passwordEncoder.matches(pinFromRequest, hash)) {
            throw new BadRequestException("PIN xác nhận thanh toán không đúng");
        }
    }

    /**
     * User tự đặt PIN lần đầu hoặc đổi PIN (cần currentPin nếu đã có PIN).
     */
    @Transactional
    public void setOrUpdateMyPaymentPin(String newPin, String currentPin) {
        if (newPin == null || !SIX_DIGITS.matcher(newPin).matches()) {
            throw new BadRequestException("PIN phải gồm đúng 6 chữ số");
        }
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không xác định người dùng hiện tại"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Không tìm thấy tài khoản");
        }
        String existing = user.getPaymentPinHash();
        if (existing != null && !existing.isBlank()) {
            if (currentPin == null || currentPin.isBlank()) {
                throw new BadRequestException("Vui lòng nhập PIN hiện tại để đổi PIN");
            }
            if (!passwordEncoder.matches(currentPin, existing)) {
                throw new BadRequestException("PIN hiện tại không đúng");
            }
        }
        user.setPaymentPinHash(passwordEncoder.encode(newPin));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean currentUserHasPaymentPin() {
        return SecurityUtil.getCurrentUserLogin()
                .map(email -> {
                    User u = userRepository.findByEmail(email);
                    return u != null && u.getPaymentPinHash() != null && !u.getPaymentPinHash().isBlank();
                })
                .orElse(false);
    }

    /**
     * Gửi OTP về email đăng nhập để đặt lại PIN (giống luồng quên mật khẩu). Chỉ khi đã từng có PIN.
     */
    @Transactional
    public void requestResetPaymentPinOtp() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không xác định người dùng hiện tại"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Không tìm thấy tài khoản");
        }
        String hash = user.getPaymentPinHash();
        if (hash == null || hash.isBlank()) {
            throw new BadRequestException("Bạn chưa từng tạo PIN. Vui lòng tạo PIN mới, không cần quên PIN.");
        }
        long remainSeconds = passwordResetTokenService.getRemainingCooldownSeconds(
                email,
                PasswordResetTokenService.RESET_PAYMENT_PIN_TYPE,
                60);
        if (remainSeconds > 0) {
            throw new BadRequestException("Vui lòng thử lại sau " + remainSeconds + " giây");
        }
        PasswordResetToken token = passwordResetTokenService.createToken(email,
                PasswordResetTokenService.RESET_PAYMENT_PIN_TYPE);
        emailService.sendOtp(
                email,
                token.getOtp(),
                "Đặt lại PIN xác nhận thanh toán",
                "Mã OTP đặt lại PIN xác nhận thanh toán của bạn là: ");
    }

    /**
     * Xác thực OTP email và ghi đè PIN mới (hash BCrypt).
     */
    @Transactional
    public void resetPaymentPinWithOtp(String otp, String newPin, String confirmPin) {
        if (newPin == null || !SIX_DIGITS.matcher(newPin).matches()) {
            throw new BadRequestException("PIN mới phải gồm đúng 6 chữ số");
        }
        if (!newPin.equals(confirmPin)) {
            throw new BadRequestException("PIN mới và nhập lại PIN không khớp");
        }
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không xác định người dùng hiện tại"));
        PasswordResetToken token = passwordResetTokenService
                .findValidToken(email, otp, PasswordResetTokenService.RESET_PAYMENT_PIN_TYPE)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ hoặc đã hết hạn"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Không tìm thấy tài khoản");
        }
        user.setPaymentPinHash(passwordEncoder.encode(newPin));
        userRepository.save(user);
        passwordResetTokenService.deleteToken(token);
    }
}
