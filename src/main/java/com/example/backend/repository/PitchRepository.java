package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.util.constant.pitch.PitchStatusEnum;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, Long>, JpaSpecificationExecutor<Pitch> {

    long countByStatus(PitchStatusEnum status);
}
