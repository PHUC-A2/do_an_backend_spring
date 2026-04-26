package com.example.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.Review;
import com.example.backend.domain.entity.ReviewMessage;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.review.ReqCreateReviewDTO;
import com.example.backend.domain.request.review.ReqReviewMessageDTO;
import com.example.backend.domain.request.review.ReqUpdateReviewStatusDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.review.ResReviewDTO;
import com.example.backend.domain.response.review.ResReviewMessageDTO;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.ReviewMessageRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TenantUserRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.RoleSecurityUtil;
import com.example.backend.util.SecurityUtil;
import com.example.backend.websocket.ReviewChatSocketHandler;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.constant.review.ReviewTargetTypeEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.websocket.NotificationSocketHandler;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMessageRepository reviewMessageRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;
    private final TenantUserRepository tenantUserRepository;
    private final ReviewChatSocketHandler reviewChatSocketHandler;
    private final NotificationSocketHandler notificationSocketHandler;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewMessageRepository reviewMessageRepository,
            UserRepository userRepository,
            PitchRepository pitchRepository,
            TenantUserRepository tenantUserRepository,
            @Lazy ReviewChatSocketHandler reviewChatSocketHandler,
            NotificationSocketHandler notificationSocketHandler) {
        this.reviewRepository = reviewRepository;
        this.reviewMessageRepository = reviewMessageRepository;
        this.userRepository = userRepository;
        this.pitchRepository = pitchRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.reviewChatSocketHandler = reviewChatSocketHandler;
        this.notificationSocketHandler = notificationSocketHandler;
    }

    @Transactional
    public ResReviewDTO createMyReview(@NonNull ReqCreateReviewDTO req) throws IdInvalidException {
        User currentUser = getCurrentUserRequired();
        Pitch pitch = null;

        if (req.getTargetType() == ReviewTargetTypeEnum.PITCH) {
            if (req.getPitchId() == null) {
                throw new BadRequestException("Đánh giá sân bắt buộc truyền pitchId");
            }
            pitch = pitchRepository.findById(req.getPitchId())
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy sân với ID = " + req.getPitchId()));
        }
        if (req.getTargetType() == ReviewTargetTypeEnum.SYSTEM && req.getPitchId() != null) {
            throw new BadRequestException("Đánh giá hệ thống không cần pitchId");
        }

        Review review = new Review();
        review.setUser(currentUser);
        review.setPitch(pitch);
        review.setTargetType(req.getTargetType());
        review.setRating(req.getRating());
        review.setContent(req.getContent().trim());
        review.setStatus(ReviewStatusEnum.PENDING);
        if (pitch != null) {
            review.setTenantId(pitch.getTenantId());
        } else {
            review.setTenantId(TenantContext.requireCurrentTenantId());
        }

        return toResReviewDTO(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ResReviewDTO> getMyReviews() {
        User currentUser = getCurrentUserRequired();
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResReviewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResReviewDTO> getPublicApprovedReviewsForPitch(@NonNull Long pitchId) throws IdInvalidException {
        if (!pitchRepository.existsById(pitchId)) {
            throw new IdInvalidException("Không tìm thấy sân với ID = " + pitchId);
        }
        return reviewRepository
                .findByPitch_IdAndStatusOrderByCreatedAtDesc(pitchId, ReviewStatusEnum.APPROVED)
                .stream()
                .map(this::toResReviewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllReviews(Specification<Review> spec, Pageable pageable) {
        long tId = TenantContext.requireCurrentTenantId();
        Specification<Review> tspec = (root, q, cb) -> cb.equal(root.get("tenantId"), tId);
        Specification<Review> combined = spec == null ? tspec : spec.and(tspec);
        Page<Review> pageReview = reviewRepository.findAll(combined, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageReview.getTotalPages());
        mt.setTotal(pageReview.getTotalElements());
        rs.setMeta(mt);

        List<ResReviewDTO> result = new ArrayList<>();
        for (Review review : pageReview.getContent()) {
            result.add(toResReviewDTO(review));
        }
        rs.setResult(result);
        return rs;
    }

    @Transactional
    public ResReviewDTO updateReviewStatus(Long reviewId, ReqUpdateReviewStatusDTO req) throws IdInvalidException {
        long tId = TenantContext.requireCurrentTenantId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đánh giá với ID = " + reviewId));
        if (review.getTenantId() == null || !review.getTenantId().equals(tId)) {
            throw new BadRequestException("Đánh giá không thuộc tenant hiện tại");
        }
        review.setStatus(req.getStatus());
        ResReviewDTO dto = toResReviewDTO(reviewRepository.save(review));
        if (review.getPitch() != null) {
            try {
                notificationSocketHandler.broadcastPitchReviewsUpdated(review.getPitch().getId());
            } catch (Exception ex) {
                log.warn("[review] broadcast cập nhật đánh giá sân thất bại: {}", ex.getMessage());
            }
        }
        return dto;
    }

    @Transactional
    public List<ResReviewMessageDTO> getReviewMessages(Long reviewId) throws IdInvalidException {
        User currentUser = getCurrentUserRequired();
        Review review = getReviewByIdRequired(reviewId);
        ensureCanAccessReview(review, currentUser);

        List<ReviewMessage> messages = reviewMessageRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
        Instant now = Instant.now();
        boolean changed = false;
        for (ReviewMessage message : messages) {
            boolean isIncoming = !message.getSender().getId().equals(currentUser.getId());
            if (isIncoming) {
                if (message.getDeliveredAt() == null) {
                    message.setDeliveredAt(now);
                    changed = true;
                }
                if (message.getReadAt() == null) {
                    message.setReadAt(now);
                    changed = true;
                }
            }
        }
        if (changed) {
            reviewMessageRepository.saveAll(messages);
        }

        return messages
                .stream()
                .map(this::toResReviewMessageDTO)
                .toList();
    }

    @Transactional
    public ResReviewMessageDTO addReviewMessage(Long reviewId, ReqReviewMessageDTO req) throws IdInvalidException {
        User currentUser = getCurrentUserRequired();
        return addReviewMessageByUser(reviewId, currentUser, req.getContent());
    }

    @Transactional
    public ResReviewMessageDTO addReviewMessageByUser(Long reviewId, User sender, String content) throws IdInvalidException {
        Review review = getReviewByIdRequired(reviewId);
        ensureCanAccessReview(review, sender);

        String cleanContent = content == null ? "" : content.trim();
        if (cleanContent.isBlank()) {
            throw new BadRequestException("Nội dung chat không được để trống");
        }

        ReviewMessage message = new ReviewMessage();
        message.setReview(review);
        message.setSender(sender);
        message.setContent(cleanContent);
        message.setDeliveredAt(null);
        message.setReadAt(null);

        ResReviewMessageDTO dto = toResReviewMessageDTO(reviewMessageRepository.save(message));
        // Phát realtime tới mọi client trong phòng chat (admin + user), kể cả khi gửi qua REST
        try {
            reviewChatSocketHandler.broadcastToRoom(reviewId, dto);
        } catch (Exception ex) {
            log.warn("[review-chat] Broadcast WebSocket thất bại, tin đã lưu DB: {}", ex.getMessage());
        }
        // Chuông đối phương qua /ws/notifications (một nguồn âm, trùng với Header/AdminSidebar)
        pushReviewChatRingToCounterpart(review, sender);
        return dto;
    }

    /** Gửi sự kiện ring tới khách (admin gửi) hoặc tới các admin đang online (khách gửi). */
    private void pushReviewChatRingToCounterpart(Review review, User sender) {
        try {
            User senderManaged = userRepository.findById(sender.getId()).orElse(sender);
            boolean senderIsAdmin = senderManaged.getRoles().stream()
                    .anyMatch(RoleSecurityUtil::isGlobalSystemAllRole);
            if (senderIsAdmin) {
                User owner = review.getUser();
                if (owner != null && owner.getEmail() != null
                        && !owner.getEmail().equalsIgnoreCase(senderManaged.getEmail())) {
                    notificationSocketHandler.sendRingToUser(owner.getEmail());
                }
                return;
            }
            userRepository.findAllWithSystemAdminRole().stream()
                    .filter(admin -> admin.getStatus() == UserStatusEnum.ACTIVE)
                    .filter(admin -> admin.getEmail() != null
                            && !admin.getEmail().equalsIgnoreCase(senderManaged.getEmail()))
                    .forEach(admin -> notificationSocketHandler.sendRingToUser(admin.getEmail()));
        } catch (Exception ex) {
            log.warn("[review-chat] Gửi chuông tới đối phương thất bại: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public User getCurrentUserRequired() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new BadRequestException("Không xác định được người dùng hiện tại");
        }
        return currentUser;
    }

    @Transactional(readOnly = true)
    public Review getReviewByIdRequired(Long reviewId) throws IdInvalidException {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đánh giá với ID = " + reviewId));
    }

    @Transactional(readOnly = true)
    public void ensureCanAccessReview(Review review, User actor) {
        boolean isOwner = review.getUser() != null && review.getUser().getId().equals(actor.getId());
        if (isOwner) {
            return;
        }
        if (actor.getRoles().stream().anyMatch(RoleSecurityUtil::isGlobalSystemAllRole)) {
            return;
        }
        if (review.getPitch() == null) {
            throw new BadRequestException("Bạn không có quyền truy cập đoạn chat này");
        }
        long pitchTid = review.getPitch().getTenantId();
        if (tenantUserRepository.existsByUser_IdAndTenant_Id(actor.getId(), pitchTid)) {
            return;
        }
        throw new BadRequestException("Bạn không có quyền truy cập đoạn chat này");
    }

    @Transactional(readOnly = true)
    public ResReviewDTO toResReviewDTO(Review review) {
        ResReviewDTO dto = new ResReviewDTO();
        dto.setId(review.getId());
        dto.setTargetType(review.getTargetType());
        dto.setPitchId(review.getPitch() != null ? review.getPitch().getId() : null);
        dto.setPitchName(review.getPitch() != null ? review.getPitch().getName() : null);
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setStatus(review.getStatus());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName());
        dto.setUserFullName(review.getUser().getFullName());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public ResReviewMessageDTO toResReviewMessageDTO(ReviewMessage message) {
        ResReviewMessageDTO dto = new ResReviewMessageDTO();
        dto.setId(message.getId());
        dto.setReviewId(message.getReview().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName());
        dto.setSenderFullName(message.getSender().getFullName());
        dto.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        dto.setContent(message.getContent());
        dto.setDeliveredAt(message.getDeliveredAt());
        dto.setReadAt(message.getReadAt());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
