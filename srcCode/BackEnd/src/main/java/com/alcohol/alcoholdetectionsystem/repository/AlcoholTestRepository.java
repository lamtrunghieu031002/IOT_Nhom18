package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.AlcoholTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlcoholTestRepository extends JpaRepository<AlcoholTestEntity, Long> {
}
