package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.TenantUser;

public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {

    void deleteByTenant_Id(Long tenantId);

    List<TenantUser> findByUser_IdOrderByIdAsc(Long userId);

    List<TenantUser> findByUser_IdOrderByIdDesc(Long userId);

    List<TenantUser> findByTenant_Id(Long tenantId);

    Optional<TenantUser> findFirstByTenant_IdAndRoleOrderByIdAsc(Long tenantId, String role);

    boolean existsByUser_IdAndTenant_Id(Long userId, Long tenantId);

    @Query("select tu.user.id from TenantUser tu where tu.tenant.id = :tenantId")
    List<Long> findUserIdsByTenantId(@Param("tenantId") Long tenantId);
}
