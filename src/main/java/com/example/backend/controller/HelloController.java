package com.example.backend.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.common.MessageResponse;
import com.example.backend.util.annotation.ApiMessage;

@RestController
public class HelloController {
    @PostMapping("/")
    @ApiMessage("Trang chủ")
    public ResponseEntity<MessageResponse> hello() {

        LocalDateTime ldt = LocalDateTime.now();
        LocalTime lt = LocalTime.now();
        Instant it = Instant.now();

        Map<String, String> data = Map.of(
                "localDateTime", ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "localTime", lt.format(DateTimeFormatter.ISO_LOCAL_TIME),
                "instant", it.toString());

        return ResponseEntity.ok(new MessageResponse(data));
    }

}
