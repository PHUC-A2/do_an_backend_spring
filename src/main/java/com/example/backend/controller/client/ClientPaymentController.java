package com.example.backend.controller.client;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.request.payment.ReqCreatePaymentDTO;
import com.example.backend.domain.response.payment.ResPaymentQRDTO;
import com.example.backend.service.PaymentService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/client")
public class ClientPaymentController {

    private final PaymentService paymentService;

    public ClientPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Tạo payment + trả QR
     * FE gọi khi user bấm "Thanh toán"
     */
    @PostMapping("/payments")
    @ApiMessage("Tạo thanh toán cho booking và trả QR")
    public ResponseEntity<ResPaymentQRDTO> createPayment(
            @Valid @RequestBody ReqCreatePaymentDTO req)
            throws IdInvalidException {

        Payment payment = paymentService.createPayment(
                req.getBookingId(),
                req.getMethod());

        ResPaymentQRDTO res = paymentService.buildQR(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lấy lại QR theo paymentCode
     * (reload trang, mở lại app)
     */
    @GetMapping("/payments/{paymentCode}/qr")
    @ApiMessage("Lấy QR thanh toán theo mã payment")
    public ResponseEntity<ResPaymentQRDTO> getQR(
            @PathVariable String paymentCode) {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        Payment payment = paymentService.getByCodeForUser(paymentCode, email);
        ResPaymentQRDTO res = paymentService.buildQR(payment);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/payments/{paymentId}/proof")
    @ApiMessage("Upload ảnh minh chứng thanh toán")
    public ResponseEntity<Void> uploadPaymentProof(
            @PathVariable long paymentId,
            @RequestParam String proofUrl) {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        paymentService.attachProof(paymentId, proofUrl, email);
        return ResponseEntity.ok().build();
    }

}
