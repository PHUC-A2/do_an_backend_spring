package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.backend.websocket.ReviewChatSocketHandler;

@Configuration
@EnableWebSocket
public class ReviewWebSocketConfig implements WebSocketConfigurer {

    private final ReviewChatSocketHandler reviewChatSocketHandler;

    public ReviewWebSocketConfig(ReviewChatSocketHandler reviewChatSocketHandler) {
        this.reviewChatSocketHandler = reviewChatSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(reviewChatSocketHandler, "/ws/reviews/*")
                .setAllowedOriginPatterns("*");
    }
}
