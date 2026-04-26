package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String name);

    /** @deprecated Dùng {@link #findByNameAndTenantIsNull} — tên có thể trùng giữa các tenant. */
    @Deprecated
    Role findByName(String name);

    Optional<Role> findByNameAndTenantIsNull(String name);

    boolean existsByNameAndTenantIsNull(String name);

    boolean existsByNameAndTenant_Id(String name, Long tenantId);

    boolean existsByNameAndTenantIsNullAndIdNot(String name, Long id);

    boolean existsByNameAndTenant_IdAndIdNot(String name, Long tenantId, Long id);

    long countByTenant_Id(Long tenantId);

    @EntityGraph(attributePaths = { "permissions", "tenant" })
    Optional<Role> findWithPermissionsById(Long id);

    @Override
    @EntityGraph(attributePaths = { "permissions", "tenant" })
    @NonNull
    Page<Role> findAll(@Nullable Specification<Role> spec, @NonNull Pageable pageable);
}
