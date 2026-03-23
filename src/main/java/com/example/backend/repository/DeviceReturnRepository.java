package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.DeviceReturn;

public interface DeviceReturnRepository extends JpaRepository<DeviceReturn, Long>, JpaSpecificationExecutor<DeviceReturn> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM DeviceReturn r WHERE r.checkout.id = :checkoutId")
    boolean existsByCheckoutId(@Param("checkoutId") Long checkoutId);

    @Query("SELECT r FROM DeviceReturn r WHERE r.checkout.id = :checkoutId")
    Optional<DeviceReturn> findByCheckoutId(@Param("checkoutId") Long checkoutId);
}
