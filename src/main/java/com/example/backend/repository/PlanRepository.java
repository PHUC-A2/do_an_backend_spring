package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.Plan;
import com.example.backend.util.constant.subscription.PlanStatusEnum;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByStatusOrderByIdAsc(PlanStatusEnum status);
}
