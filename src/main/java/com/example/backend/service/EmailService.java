package com.example.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp) {

        System.out.println(">>> Sending OTP to: " + toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp + " (hiệu lực 5 phút)");

        mailSender.send(message);

        System.out.println(">>> Mail sent!");
    }

}
