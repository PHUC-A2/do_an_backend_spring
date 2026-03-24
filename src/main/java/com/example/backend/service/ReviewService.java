package com.example.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.Review;
import com.example.backend.domain.entity.ReviewMessage;
import com.example.backend.domain.entity.Role;
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
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.constant.review.ReviewTargetTypeEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMessageRepository reviewMessageRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewMessageRepository reviewMessageRepository,
            UserRepository userRepository,
            PitchRepository pitchRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMessageRepository = reviewMessageRepository;
        this.userRepository = userRepository;
        this.pitchRepository = pitchRepository;
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
    public ResultPaginationDTO getAllReviews(Specification<Review> spec, Pageable pageable) {
        Page<Review> pageReview = reviewRepository.findAll(spec, pageable);
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
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đánh giá với ID = " + reviewId));
        review.setStatus(req.getStatus());
        return toResReviewDTO(reviewRepository.save(review));
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

        return toResReviewMessageDTO(reviewMessageRepository.save(message));
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
        boolean isAdmin = actor.getRoles().stream().map(Role::getName).anyMatch("ADMIN"::equals);
        if (!isOwner && !isAdmin) {
            throw new BadRequestException("Bạn không có quyền truy cập đoạn chat này");
        }
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
