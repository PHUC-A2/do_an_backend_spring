package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.User;
import com.example.backend.util.constant.user.UserStatusEnum;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
        boolean existsByEmail(String email);

        @EntityGraph(attributePaths = { "roles", "roles.permissions", "roles.tenant" })
        User findByEmail(String email);

        /** Đăng ký sân: kiểm tra email đã tồn tại, không phân biệt hoa thường. */
        User findByEmailIgnoreCase(String email);

        User findByRefreshTokenAndEmail(String token, String email);

        Optional<User> findByIdAndEmail(Long id, String email);

        @EntityGraph(attributePaths = {
                        "roles",
                        "roles.permissions",
                        "roles.tenant"
        })
        Optional<User> findWithRolesAndPermissionsById(Long id);

        @Override
        @EntityGraph(attributePaths = {
                        "roles",
                        "roles.permissions",
                        "roles.tenant"
        })
        @NonNull
        Page<User> findAll(@Nullable Specification<User> spec, @NonNull Pageable pageable);

        List<User> findDistinctByRoles_Name(String roleName);

        /** User có role ADMIN toàn hệ thống (tenant null), dùng thay cho findDistinctByRoles_Name("ADMIN"). */
        @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN' AND r.tenant IS NULL")
        List<User> findAllWithSystemAdminRole();

        long count();

        /** Số user theo trạng thái tài khoản — phục vụ dashboard. */
        long countByStatus(UserStatusEnum status);

}
