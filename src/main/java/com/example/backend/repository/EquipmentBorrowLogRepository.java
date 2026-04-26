package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.EquipmentBorrowLog;

@Repository
public interface EquipmentBorrowLogRepository extends JpaRepository<EquipmentBorrowLog, Long> {

    long countByTenantId(long tenantId);

    List<EquipmentBorrowLog> findByBookingEquipmentIdOrderByCreatedAtDesc(Long bookingEquipmentId);

    List<EquipmentBorrowLog> findTop200ByOrderByCreatedAtDesc();
}
