package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.backend.websocket.NotificationSocketHandler;

@Configuration
@EnableWebSocket
public class NotificationWebSocketConfig implements WebSocketConfigurer {

    private final NotificationSocketHandler notificationSocketHandler;

    public NotificationWebSocketConfig(NotificationSocketHandler notificationSocketHandler) {
        this.notificationSocketHandler = notificationSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationSocketHandler, "/ws/notifications")
                .setAllowedOriginPatterns("*");
    }
}
