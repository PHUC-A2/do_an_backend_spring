package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.PaymentService;
import com.example.backend.util.annotation.ApiMessage;
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
    public ResponseEntity<ResultPaginationDTO> getAllPayments(
            @Filter Specification<Payment> spec,
            @NonNull Pageable pageable) {

        return ResponseEntity.ok(this.paymentService.getAllPayments(spec, pageable));
    }

    @PutMapping("/payments/{id}/confirm")
    @ApiMessage("Admin xác nhận payment đã thanh toán")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long id) {

        this.paymentService.adminConfirmPaid(id);
        return ResponseEntity.ok().build();
    }
}
