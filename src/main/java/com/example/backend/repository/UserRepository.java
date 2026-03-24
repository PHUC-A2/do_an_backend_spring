package com.example.backend.repository;

import java.util.List;
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

import com.example.backend.domain.entity.User;
import com.example.backend.util.constant.user.UserStatusEnum;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
        boolean existsByEmail(String email);

        User findByEmail(String email);

        User findByRefreshTokenAndEmail(String token, String email);

        Optional<User> findByIdAndEmail(Long id, String email);

        @EntityGraph(attributePaths = {
                        "roles",
                        "roles.permissions"
        })
        Optional<User> findWithRolesAndPermissionsById(Long id);

        @Override
        @EntityGraph(attributePaths = {
                        "roles",
                        "roles.permissions"
        })
        @NonNull
        Page<User> findAll(@Nullable Specification<User> spec, @NonNull Pageable pageable);

        List<User> findDistinctByRoles_Name(String roleName);

        long count();

        /** Số user theo trạng thái tài khoản — phục vụ dashboard. */
        long countByStatus(UserStatusEnum status);

}
