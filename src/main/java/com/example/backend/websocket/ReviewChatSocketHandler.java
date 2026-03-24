package com.example.backend.websocket;

import java.net.URI;
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
import com.example.backend.domain.request.review.ReqReviewMessageDTO;
import com.example.backend.domain.response.review.ResReviewMessageDTO;
import com.example.backend.service.ReviewService;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.error.IdInvalidException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReviewChatSocketHandler extends TextWebSocketHandler {

    private static final String ATTR_REVIEW_ID = "reviewId";
    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_USER_EMAIL = "userEmail";

    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final ReviewService reviewService;
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ReviewChatSocketHandler(
            ObjectMapper objectMapper,
            SecurityUtil securityUtil,
            UserService userService,
            ReviewService reviewService) {
        this.objectMapper = objectMapper;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long reviewId = extractReviewId(session.getUri());
        if (reviewId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String token = extractToken(session.getUri());
        Optional<String> emailOpt = securityUtil.extractSubjectFromToken(token == null ? "" : token);
        if (emailOpt.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token không hợp lệ"));
            return;
        }

        User actor = userService.handleGetUserByUsername(emailOpt.get());
        if (actor == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Không tìm thấy người dùng"));
            return;
        }

        try {
            reviewService.ensureCanAccessReview(reviewService.getReviewByIdRequired(reviewId), actor);
        } catch (IdInvalidException ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Review không tồn tại"));
            return;
        } catch (RuntimeException ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Không có quyền truy cập"));
            return;
        }

        session.getAttributes().put(ATTR_REVIEW_ID, reviewId);
        session.getAttributes().put(ATTR_USER_ID, actor.getId());
        session.getAttributes().put(ATTR_USER_EMAIL, actor.getEmail());

        roomSessions.computeIfAbsent(reviewId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long reviewId = (Long) session.getAttributes().get(ATTR_REVIEW_ID);
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        String userEmail = (String) session.getAttributes().get(ATTR_USER_EMAIL);

        if (reviewId == null || userId == null || userEmail == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        ReqReviewMessageDTO payload = objectMapper.readValue(message.getPayload(), ReqReviewMessageDTO.class);
        User actor = userService.handleGetUserByUsername(userEmail);
        if (actor == null || !actor.getId().equals(userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Phiên đăng nhập không hợp lệ"));
            return;
        }

        ResReviewMessageDTO saved = reviewService.addReviewMessageByUser(reviewId, actor, payload.getContent());
        broadcastToRoom(reviewId, saved);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long reviewId = (Long) session.getAttributes().get(ATTR_REVIEW_ID);
        if (reviewId == null) {
            return;
        }
        Set<WebSocketSession> sessions = roomSessions.get(reviewId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            roomSessions.remove(reviewId);
        }
    }

    private void broadcastToRoom(Long reviewId, ResReviewMessageDTO payload) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(reviewId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String json = objectMapper.writeValueAsString(payload);
        TextMessage wsMessage = new TextMessage(json);
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                ws.sendMessage(wsMessage);
            }
        }
    }

    private Long extractReviewId(URI uri) {
        if (uri == null || uri.getPath() == null) {
            return null;
        }
        String[] parts = uri.getPath().split("/");
        if (parts.length == 0) {
            return null;
        }
        String last = parts[parts.length - 1];
        try {
            return Long.parseLong(last);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractToken(URI uri) {
        if (uri == null) {
            return null;
        }
        return UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
    }
}
