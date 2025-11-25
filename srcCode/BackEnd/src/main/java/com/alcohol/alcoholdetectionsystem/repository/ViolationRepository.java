package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.ViolationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<ViolationEntity, Long> {
    List<ViolationEntity> findByCreatedAtAfter(LocalDateTime startDate);

    List<ViolationEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
