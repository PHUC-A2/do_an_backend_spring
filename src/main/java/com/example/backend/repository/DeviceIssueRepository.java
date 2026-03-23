package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend.domain.entity.DeviceIssue;

public interface DeviceIssueRepository extends JpaRepository<DeviceIssue, Long>, JpaSpecificationExecutor<DeviceIssue> {
}
