package com.example.backend.repository.v2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.v2.RoomDeviceCatalog;

@Repository
public interface RoomDeviceCatalogRepository
        extends JpaRepository<RoomDeviceCatalog, Long>, JpaSpecificationExecutor<RoomDeviceCatalog> {
}
