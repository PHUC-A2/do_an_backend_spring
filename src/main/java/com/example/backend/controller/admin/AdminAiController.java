package com.example.backend.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.ai.ReqChatDTO;
import com.example.backend.domain.response.ai.ResChatDTO;
import com.example.backend.service.AiService;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/ai")
public class AdminAiController {

    private final AiService aiService;
    private final UserService userService;

    public AdminAiController(AiService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/chat")
    @ApiMessage("Admin chat with AI")
    public ResponseEntity<ResChatDTO> chat(@Valid @RequestBody ReqChatDTO req) {
        User user = null;
        try {
            String email = SecurityUtil.getCurrentUserLogin().orElse(null);
            if (email != null) {
                user = userService.handleGetUserByUsername(email);
            }
        } catch (Exception ignored) {}

        ResChatDTO response = aiService.adminChat(req, user);
        return ResponseEntity.ok(response);
    }
}
