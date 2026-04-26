package com.example.backend.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.Subscription;
import com.example.backend.util.constant.subscription.SubscriptionStatusEnum;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    void deleteByTenant_Id(Long tenantId);

    long countByPlan_Id(Long planId);

    @Query("select s from Subscription s join fetch s.plan p where s.tenant.id = :tid and s.status = :st "
            + "and s.endDate > :now order by s.endDate desc")
    List<Subscription> findActiveForTenant(
            @Param("tid") Long tenantId, @Param("st") SubscriptionStatusEnum status, @Param("now") Instant now);

    @Query("select s from Subscription s join fetch s.tenant t join fetch s.plan p where t.id = :tid order by s.id desc")
    List<Subscription> findByTenant_IdOrderByIdDesc(@Param("tid") Long tenantId);

    @Query("select s from Subscription s join fetch s.tenant t join fetch s.plan p order by s.id desc")
    List<Subscription> findAllForList();
}
