package com.example.backend.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.systemconfig.ResPublicMessengerConfigDTO;
import com.example.backend.service.SystemConfigService;
import com.example.backend.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/client/public/system-config")
@RequiredArgsConstructor
public class ClientPublicSystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/messenger")
    @ApiMessage("Lấy cấu hình messenger public")
    public ResponseEntity<ResPublicMessengerConfigDTO> getPublicMessengerConfig() {
        return ResponseEntity.ok(systemConfigService.getPublicMessengerConfig());
    }
}
