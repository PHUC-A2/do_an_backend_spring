package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Checkout;
import com.example.backend.domain.request.checkout.ReqCreateCheckoutDTO;
import com.example.backend.domain.request.checkout.ReqUpdateCheckoutDTO;
import com.example.backend.domain.response.checkout.ResCheckoutDetailDTO;
import com.example.backend.domain.response.checkout.ResCreateCheckoutDTO;
import com.example.backend.domain.response.checkout.ResUpdateCheckoutDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.CheckoutService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkouts")
    @ApiMessage("Tạo phiếu nhận tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('CHECKOUT_CREATE')")
    public ResponseEntity<ResCreateCheckoutDTO> createCheckout(
            @Valid @RequestBody @NonNull ReqCreateCheckoutDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutService.createCheckout(dto));
    }

    @GetMapping("/checkouts")
    @ApiMessage("Lấy danh sách phiếu nhận tài sản")
    public ResponseEntity<ResultPaginationDTO> getAllCheckouts(
            @Filter Specification<Checkout> spec,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(checkoutService.getAllCheckouts(spec, pageable));
    }

    @GetMapping("/checkouts/{id}")
    @ApiMessage("Lấy chi tiết phiếu nhận tài sản")
    public ResponseEntity<ResCheckoutDetailDTO> getCheckoutById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        return ResponseEntity.ok(checkoutService.getCheckoutDetailById(id));
    }

    @PutMapping("/checkouts/{id}")
    @ApiMessage("Cập nhật phiếu nhận tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('CHECKOUT_UPDATE')")
    public ResponseEntity<ResUpdateCheckoutDTO> updateCheckout(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqUpdateCheckoutDTO dto) throws IdInvalidException {
        return ResponseEntity.ok(checkoutService.updateCheckout(id, dto));
    }

    @DeleteMapping("/checkouts/{id}")
    @ApiMessage("Xóa phiếu nhận tài sản")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('CHECKOUT_DELETE')")
    public ResponseEntity<Void> deleteCheckout(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        checkoutService.deleteCheckout(id);
        return ResponseEntity.ok().build();
    }
}
