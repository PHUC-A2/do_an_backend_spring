package com.example.backend.domain.response.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResChatDTO {
    private String reply;
    private String provider; // GROQ / GEMINI / CLOUDFLARE
    private boolean offTopic;
    private int remainingMessages; // số lần chat còn lại trong ngày
}
