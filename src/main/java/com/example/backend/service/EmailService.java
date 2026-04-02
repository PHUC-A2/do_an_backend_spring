package com.example.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final TemplateEngine templateEngine;
    private final SystemConfigService systemConfigService;

    public EmailService(TemplateEngine templateEngine, SystemConfigService systemConfigService) {
        this.templateEngine = templateEngine;
        this.systemConfigService = systemConfigService;
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
        var credential = systemConfigService.getActiveMailCredential();
        JavaMailSenderImpl senderToUse = new JavaMailSenderImpl();
        senderToUse.setHost("smtp.gmail.com");
        senderToUse.setPort(587);
        senderToUse.setUsername(credential.getEmail());
        senderToUse.setPassword(credential.getPassword());
        var props = senderToUse.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        String fromEmailToUse = credential.getEmail();

        MimeMessage mimeMessage = senderToUse.createMimeMessage();

        try {
            String safeToEmail = Objects.requireNonNull(toEmail, "toEmail must not be null");
            String safeSubject = Objects.requireNonNull(subject, "subject must not be null");
            String safeHtmlBody = Objects.requireNonNull(htmlBody, "htmlBody must not be null");

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromEmailToUse, "TBU Sport");
            helper.setTo(safeToEmail);
            helper.setSubject(safeSubject);
            helper.setText(safeHtmlBody, true);

            senderToUse.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }

}
