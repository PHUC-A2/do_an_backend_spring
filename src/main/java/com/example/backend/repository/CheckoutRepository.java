package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.Checkout;

public interface CheckoutRepository extends JpaRepository<Checkout, Long>, JpaSpecificationExecutor<Checkout> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Checkout c WHERE c.assetUsage.id = :assetUsageId")
    boolean existsByAssetUsageId(@Param("assetUsageId") Long assetUsageId);

    @Query("SELECT c FROM Checkout c WHERE c.assetUsage.id = :assetUsageId")
    Optional<Checkout> findByAssetUsageId(@Param("assetUsageId") Long assetUsageId);
}
