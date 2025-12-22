package com.example.backend.controller;

import org.springframework.http.HttpStatus;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Đăng ký tài khoản thành công"));
    }
}
