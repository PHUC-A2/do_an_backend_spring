package com.example.backend.domain.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqChatDTO {

    @NotBlank(message = "Tin nhắn không được trống")
    @Size(max = 2000, message = "Tin nhắn tối đa 2000 ký tự")
    private String message;

    // Danh sách lịch sử chat (tối đa 10 tin nhắn gần nhất)
    private java.util.List<MessageDTO> history;

    @Getter
    @Setter
    public static class MessageDTO {
        private String role; // "user" or "assistant"
        private String content;
    }
}
