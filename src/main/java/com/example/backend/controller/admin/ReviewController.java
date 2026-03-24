package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.Review;
import com.example.backend.domain.request.review.ReqReviewMessageDTO;
import com.example.backend.domain.request.review.ReqUpdateReviewStatusDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.review.ResReviewDTO;
import com.example.backend.domain.response.review.ResReviewMessageDTO;
import com.example.backend.service.ReviewService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews")
    @ApiMessage("Lấy danh sách đánh giá")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllReviews(
            @Filter Specification<Review> spec,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(spec, pageable));
    }

    @PatchMapping("/reviews/{reviewId}/status")
    @ApiMessage("Cập nhật trạng thái đánh giá")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_UPDATE')")
    public ResponseEntity<ResReviewDTO> updateStatus(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReqUpdateReviewStatusDTO req) throws IdInvalidException {
        return ResponseEntity.ok(reviewService.updateReviewStatus(reviewId, req));
    }

    @GetMapping("/reviews/{reviewId}/messages")
    @ApiMessage("Lấy chat trong đánh giá")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_VIEW_LIST')")
    public ResponseEntity<List<ResReviewMessageDTO>> getMessages(@PathVariable("reviewId") Long reviewId)
            throws IdInvalidException {
        return ResponseEntity.ok(reviewService.getReviewMessages(reviewId));
    }

    @PostMapping("/reviews/{reviewId}/messages")
    @ApiMessage("Admin gửi chat trong đánh giá")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_UPDATE')")
    public ResponseEntity<ResReviewMessageDTO> sendMessage(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReqReviewMessageDTO req) throws IdInvalidException {
        return ResponseEntity.ok(reviewService.addReviewMessage(reviewId, req));
    }
}
