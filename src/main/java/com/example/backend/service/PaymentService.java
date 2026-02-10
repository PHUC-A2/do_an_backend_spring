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
import com.example.backend.util.constant.booking.BookingStatusEnum;
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

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatusEnum.PAID);

        paymentRepository.save(payment);
    }

    /* ========= QR ========= */
    public ResPaymentQRDTO buildQR(Payment payment) {

        if (payment.getMethod() == PaymentMethodEnum.CASH) {
            throw new BadRequestException(
                    "Vui lòng thanh toán tiền mặt, không hỗ trợ QR cho hình thức này");
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
