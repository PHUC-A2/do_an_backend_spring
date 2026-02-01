package com.example.backend.controller.client;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.timeline.ResPitchTimelineDTO;
import com.example.backend.service.PublicPitchBookingService;
import com.example.backend.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/client/public")
public class PublicPitchBookingController {

    private final PublicPitchBookingService service;

    public PublicPitchBookingController(PublicPitchBookingService service) {
        this.service = service;
    }

    @GetMapping("/pitches/{pitchId}/timeline")
    @ApiMessage("Lấy timeline đặt sân theo ngày")
    public ResponseEntity<ResPitchTimelineDTO> getTimeline(
            @PathVariable @NonNull Long pitchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NonNull LocalDate date) {

        return ResponseEntity.ok(
                service.getPitchTimeline(pitchId, date));
    }
}
