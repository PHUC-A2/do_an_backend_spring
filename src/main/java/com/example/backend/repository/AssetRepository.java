package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Asset;

/**
 * Repository JPA cho Asset — cùng pattern PitchRepository (spec + phân trang).
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    long count(); // dùng khi cần thống kê (đồng bộ style PitchRepository)
}
