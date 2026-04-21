package com.example.backend.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.review.ReqCreateReviewDTO;
import com.example.backend.domain.request.review.ReqReviewMessageDTO;
import com.example.backend.domain.response.review.ResReviewDTO;
import com.example.backend.domain.response.review.ResReviewMessageDTO;
import com.example.backend.service.ReviewService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/client/reviews")
public class ClientReviewController {

    private final ReviewService reviewService;

    public ClientReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ApiMessage("Tạo đánh giá mới")
    public ResponseEntity<ResReviewDTO> createReview(@Valid @RequestBody ReqCreateReviewDTO req) throws IdInvalidException {
        return ResponseEntity.ok(reviewService.createMyReview(req));
    }

    @GetMapping("/my")
    @ApiMessage("Lấy danh sách đánh giá của tôi")
    public ResponseEntity<List<ResReviewDTO>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }

    @GetMapping("/pitch/{pitchId}/approved")
    @ApiMessage("Lấy đánh giá đã duyệt của sân")
    public ResponseEntity<List<ResReviewDTO>> getApprovedReviewsForPitch(@PathVariable("pitchId") Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(reviewService.getPublicApprovedReviewsForPitch(pitchId));
    }

    @GetMapping("/{reviewId}/messages")
    @ApiMessage("Lấy chat trong đánh giá")
    public ResponseEntity<List<ResReviewMessageDTO>> getMessages(@PathVariable("reviewId") Long reviewId)
            throws IdInvalidException {
        return ResponseEntity.ok(reviewService.getReviewMessages(reviewId));
    }

    @PostMapping("/{reviewId}/messages")
    @ApiMessage("Gửi chat trong đánh giá")
    public ResponseEntity<ResReviewMessageDTO> sendMessage(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReqReviewMessageDTO req) throws IdInvalidException {
        return ResponseEntity.ok(reviewService.addReviewMessage(reviewId, req));
    }
}
