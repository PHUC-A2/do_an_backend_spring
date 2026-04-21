package com.example.backend.controller.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.review.ResReviewDTO;
import com.example.backend.domain.response.timeline.ResPitchTimelineDTO;
import com.example.backend.service.PublicPitchBookingService;
import com.example.backend.service.ReviewService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/client/public")
public class PublicPitchBookingController {

    private final PublicPitchBookingService service;
    private final ReviewService reviewService;

    public PublicPitchBookingController(PublicPitchBookingService service, ReviewService reviewService) {
        this.service = service;
        this.reviewService = reviewService;
    }

    @GetMapping("/pitches/{pitchId}/timeline")
    @ApiMessage("Lấy timeline đặt sân theo ngày")
    public ResponseEntity<ResPitchTimelineDTO> getTimeline(
            @PathVariable @NonNull Long pitchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NonNull LocalDate date) {

        return ResponseEntity.ok(
                service.getPitchTimeline(pitchId, date));
    }

    @GetMapping("/pitches/{pitchId}/reviews")
    @ApiMessage("Lấy đánh giá công khai đã duyệt của sân")
    public ResponseEntity<List<ResReviewDTO>> getPublicPitchReviews(@PathVariable @NonNull Long pitchId)
            throws IdInvalidException {
        return ResponseEntity.ok(reviewService.getPublicApprovedReviewsForPitch(pitchId));
    }
}
