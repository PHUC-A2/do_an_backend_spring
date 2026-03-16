package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Objects;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendOtp(String toEmail, String otp) {
        sendOtp(toEmail, otp, "Reset mật khẩu", "Mã OTP đặt lại mật khẩu của bạn là: ");
    }

    public void sendOtp(String toEmail, String otp, String subject, String contentPrefix) {

        System.out.println(">>> Sending OTP to: " + toEmail);

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("subtitle", "Đặt sân bóng nhanh và dễ dàng cùng TUB Sport.");
        context.setVariable("greeting", "Xin chào,");
        context.setVariable("message", contentPrefix);
        context.setVariable("otp", otp);
        context.setVariable("expiryNotice", "Mã OTP có hiệu lực trong 5 phút.");
        context.setVariable("footer", "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.");
        context.setVariable("websiteUrl", "http://tbusport.utb.edu.vn/");

        String htmlBody = templateEngine.process("otp-email", context);

        sendHtmlEmail(toEmail, subject, htmlBody);

        System.out.println(">>> Mail sent!");
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            String safeToEmail = Objects.requireNonNull(toEmail, "toEmail must not be null");
            String safeSubject = Objects.requireNonNull(subject, "subject must not be null");
            String safeHtmlBody = Objects.requireNonNull(htmlBody, "htmlBody must not be null");

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(safeToEmail);
            helper.setSubject(safeSubject);
            helper.setText(safeHtmlBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }

}
