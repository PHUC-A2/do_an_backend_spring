package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    User findByRefreshTokenAndEmail(String token, String email);
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
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
