package com.example.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.payment.ResPaymentDTO;
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

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking đã bị huỷ, không thể tạo payment");
        }

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
                .paymentId(payment.getId())
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

    @Transactional
    public void adminConfirmPaid(long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment không tồn tại"));

        if (payment.getStatus() == PaymentStatusEnum.PAID) {
            return;
        }

        // 2. Update booking
        Booking booking = payment.getBooking();

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking đã bị huỷ, không thể xác nhận thanh toán");
        }

        payment.setStatus(PaymentStatusEnum.PAID);
        payment.setPaidAt(Instant.now());
        booking.setStatus(BookingStatusEnum.PAID);
        paymentRepository.save(payment);
    }

    // get all
      public ResultPaginationDTO getAllPayments(Specification<Payment> spec, @NonNull Pageable pageable) {
        Page<Payment> pagePayment = paymentRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pagePayment.getTotalPages());
        meta.setTotal(pagePayment.getTotalElements());
        result.setMeta(meta);

        List<ResPaymentDTO> resList = new ArrayList<>();
        for (Payment b : pagePayment.getContent()) {
            resList.add(this.convertToResPaymentDTO(b));
        }
        result.setResult(resList);

        return result;
    }

    // convert
    public ResPaymentDTO convertToResPaymentDTO(Payment dto) {
        return ResPaymentDTO.builder()
                .id(dto.getId())
                .bookingId(dto.getBooking().getId())
                .proofUrl(dto.getProofUrl())
                .paymentCode(dto.getPaymentCode())
                .amount(dto.getAmount())
                .content(dto.getContent())
                .status(dto.getStatus())
                .method(dto.getMethod())
                .paidAt(dto.getPaidAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    @Transactional
    public void attachProof(long paymentId, String proofUrl, String email) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment không tồn tại"));

        Booking booking = payment.getBooking();

        if (!email.equals(booking.getCreatedBy())) {
            throw new BadRequestException("Bạn không có quyền upload minh chứng");
        }

        if (payment.getStatus() == PaymentStatusEnum.PAID) {
            throw new BadRequestException("Payment đã thanh toán, không thể upload");
        }

        payment.setProofUrl(proofUrl);
        paymentRepository.save(payment);
    }

}
