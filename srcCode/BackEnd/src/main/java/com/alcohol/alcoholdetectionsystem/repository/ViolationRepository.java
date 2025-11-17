package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.ViolationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViolationRepository extends JpaRepository<ViolationEntity, Long> {
}
