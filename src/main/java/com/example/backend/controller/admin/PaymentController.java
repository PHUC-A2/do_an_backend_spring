package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.payment.ReqConfirmPaymentDTO;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.PaymentService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments")
    @ApiMessage("Danh sách payment chờ xác nhận")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllPayments(
            @Filter Specification<Payment> spec,
            @NonNull Pageable pageable) {

        return ResponseEntity.ok(this.paymentService.getAllPayments(spec, pageable));
    }

    @PutMapping("/payments/{id}/confirm")
    @ApiMessage("Admin xác nhận payment đã thanh toán")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_UPDATE')")
    public ResponseEntity<Void> confirmPayment(
            @PathVariable Long id,
            @RequestBody(required = false) ReqConfirmPaymentDTO body) {

        String pin = body != null ? body.getPin() : null;
        this.paymentService.adminConfirmPaid(id, pin);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/payments/{id}/reject")
    @ApiMessage("Admin từ chối xác nhận payment")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('PAYMENT_UPDATE')")
    public ResponseEntity<Void> rejectPayment(@PathVariable Long id) {
        this.paymentService.adminRejectPayment(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/payments/{id}/delete-booking")
    @ApiMessage("Admin xóa booking từ payment")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_DELETE')")
    public ResponseEntity<Void> deleteBookingFromPayment(@PathVariable Long id) throws IdInvalidException {
        this.paymentService.adminDeleteBookingFromPayment(id);
        return ResponseEntity.ok().build();
    }
}
