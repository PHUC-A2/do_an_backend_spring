package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.util.constant.pitch.PitchStatusEnum;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, Long>, JpaSpecificationExecutor<Pitch> {

    List<Pitch> findByTenantId(Long tenantId);

    long countByStatus(PitchStatusEnum status);

    long countByTenantId(long tenantId);

    long countByStatusAndTenantId(PitchStatusEnum status, long tenantId);
}
