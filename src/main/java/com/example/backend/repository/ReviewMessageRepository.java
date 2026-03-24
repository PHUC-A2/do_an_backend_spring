package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.ReviewMessage;

@Repository
public interface ReviewMessageRepository extends JpaRepository<ReviewMessage, Long> {

    @EntityGraph(attributePaths = { "sender" })
    List<ReviewMessage> findByReviewIdOrderByCreatedAtAsc(Long reviewId);
}
