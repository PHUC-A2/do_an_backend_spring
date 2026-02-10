package com.example.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.response.payment.ResPaymentQRDTO;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.util.constant.payment.PaymentMethodEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Value("${payment.bank.code}")
    private String bankCode;

    @Value("${payment.bank.account-no}")
    private String accountNo;

    @Value("${payment.bank.account-name}")
    private String accountName;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    public Payment createPayment(long bookingId, PaymentMethodEnum method) throws IdInvalidException {

        Booking booking = bookingService.getBookingById(bookingId);

        boolean existed = paymentRepository.existsByBooking_IdAndStatusIn(
                bookingId,
                List.of(PaymentStatusEnum.PENDING, PaymentStatusEnum.PAID));

        if (existed) {
            throw new BadRequestException("Booking đã có payment");
        }

        // Status mặc định = PENDING (đã set trong entity)
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setMethod(method);
        payment.setContent("BOOKING_" + booking.getId());
        payment.setPaymentCode(generatePaymentCode());

        return paymentRepository.save(payment);
    }

    public Payment getByCode(String code) {
        return paymentRepository.findByPaymentCode(code)
                .orElseThrow(() -> new BadRequestException("Payment không tồn tại"));
    }

    public void markPaid(String paymentCode) {

        Payment payment = getByCode(paymentCode);

        if (payment.getStatus() == PaymentStatusEnum.PAID)
            return;

        payment.setStatus(PaymentStatusEnum.PAID);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);
    }

    /* ========= QR ========= */
    public ResPaymentQRDTO buildQR(Payment payment) {

        if (payment.getMethod() == PaymentMethodEnum.CASH) {
            throw new BadRequestException(
                    "Thanh toán tiền mặt không hỗ trợ QR");
        }

        if (payment.getStatus() == PaymentStatusEnum.PAID) {
            throw new BadRequestException("Payment đã thanh toán");
        }

        String vietQrUrl = "https://img.vietqr.io/image/"
                + bankCode + "-" + accountNo + "-compact.png"
                + "?amount=" + payment.getAmount()
                + "&addInfo=" + URLEncoder.encode(payment.getContent(), StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(accountName, StandardCharsets.UTF_8);

        return ResPaymentQRDTO.builder()
                .paymentCode(payment.getPaymentCode())
                .bankCode(bankCode)
                .accountNo(accountNo)
                .accountName(accountName)
                .amount(payment.getAmount())
                .content(payment.getContent())
                .vietQrUrl(vietQrUrl)
                .build();
    }

    private String generatePaymentCode() {
        return "PAY_" + Instant.now().toEpochMilli() + "_" + (int) (Math.random() * 1000);
    }

    public Payment getByCodeForUser(String paymentCode, String email) {

        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new BadRequestException("Payment không tồn tại"));

        Booking booking = payment.getBooking();

        if (!email.equals(booking.getCreatedBy())) {
            throw new BadRequestException("Bạn không có quyền xem payment này");
        }

        return payment;
    }

}
