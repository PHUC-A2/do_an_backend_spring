package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Review;
import com.example.backend.util.constant.review.ReviewStatusEnum;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    @EntityGraph(attributePaths = { "user", "pitch" })
    @NonNull
    Page<Review> findAll(@Nullable org.springframework.data.jpa.domain.Specification<Review> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = { "user", "pitch" })
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = { "user", "pitch" })
    Optional<Review> findById(Long id);

    @Query("""
            select r.pitch.id, avg(r.rating), count(r.id)
            from Review r
            where r.pitch.id in :pitchIds and r.status = :status
            group by r.pitch.id
            """)
    List<Object[]> findPitchRatingSummaryByPitchIds(
            @Param("pitchIds") List<Long> pitchIds,
            @Param("status") ReviewStatusEnum status);

    long countByStatus(ReviewStatusEnum status);
}
