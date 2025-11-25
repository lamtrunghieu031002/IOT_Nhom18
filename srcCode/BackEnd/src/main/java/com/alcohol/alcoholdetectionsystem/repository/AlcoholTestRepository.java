package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.AlcoholTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AlcoholTestRepository extends JpaRepository<AlcoholTestEntity, Long> {
    List<AlcoholTestEntity> findByOfficerId(Long officerId);

    List<AlcoholTestEntity> findByDeviceEntityDeviceId(String deviceId);

    List<AlcoholTestEntity> findByCreatedAtAfter(LocalDateTime startDate);

    List<AlcoholTestEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
