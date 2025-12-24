package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Pitch;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, Long>, JpaSpecificationExecutor<Pitch> {

}
