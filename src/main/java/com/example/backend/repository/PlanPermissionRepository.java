package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.PlanPermission;

public interface PlanPermissionRepository extends JpaRepository<PlanPermission, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PlanPermission pp where pp.plan.id = :planId")
    void deleteAllByPlanId(@Param("planId") Long planId);

    @Query("select p.name from PlanPermission pp join pp.permission p where pp.plan.id = :planId order by p.name")
    List<String> findPermissionNamesByPlanId(@Param("planId") Long planId);
}
