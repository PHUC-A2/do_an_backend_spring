package com.example.backend.websocket;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.response.notification.ResNotificationDTO;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class NotificationSocketHandler extends TextWebSocketHandler {

    private static final String ATTR_USER_EMAIL = "userEmail";
    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public NotificationSocketHandler(ObjectMapper objectMapper, SecurityUtil securityUtil, UserService userService) {
        this.objectMapper = objectMapper;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session.getUri());
        Optional<String> emailOpt = securityUtil.extractSubjectFromToken(token == null ? "" : token);
        if (emailOpt.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token không hợp lệ"));
            return;
        }

        User user = userService.handleGetUserByUsername(emailOpt.get());
        if (user == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Không tìm thấy người dùng"));
            return;
        }

        // Khóa map theo email chữ thường để khớp khi broadcast (JWT/DB có thể khác hoa thường)
        String email = canonicalEmail(user.getEmail());
        session.getAttributes().put(ATTR_USER_EMAIL, email);
        userSessions.computeIfAbsent(email, key -> ConcurrentHashMap.newKeySet()).add(session);
        sendEvent(session, "connected", "ok");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Giữ kết nối sống khi client chủ động ping.
        sendEvent(session, "pong", "ok");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = (String) session.getAttributes().get(ATTR_USER_EMAIL);
        if (email == null) {
            return;
        }
        unregisterSession(email, session);
    }

    public void sendNotificationToUser(String email, ResNotificationDTO dto) {
        if (email == null || email.isBlank()) {
            return;
        }
        broadcastToUser(email, "notification", dto);
    }

    public void sendRingToUser(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        broadcastToUser(email, "ring", "actor");
    }

    private void broadcastToUser(String email, String event, Object payload) {
        String key = canonicalEmail(email);
        if (key.isEmpty()) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(key);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        Set<WebSocketSession> closed = ConcurrentHashMap.newKeySet();
        for (WebSocketSession ws : sessions) {
            try {
                if (ws.isOpen()) {
                    sendEvent(ws, event, payload);
                } else {
                    closed.add(ws);
                }
            } catch (Exception ex) {
                closed.add(ws);
            }
        }
        sessions.removeAll(closed);
        if (sessions.isEmpty()) {
            userSessions.remove(email);
        }
    }

    private void sendEvent(WebSocketSession session, String event, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("event", event, "data", payload));
        session.sendMessage(new TextMessage(json));
    }

    private void unregisterSession(String email, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(canonicalEmail(email));
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            userSessions.remove(email);
        }
    }

    private String extractToken(URI uri) {
        if (uri == null) {
            return null;
        }
        return UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
    }

    /** Chuẩn hóa email làm khóa session map để tránh lệch key khi so khớp broadcast. */
    private static String canonicalEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
