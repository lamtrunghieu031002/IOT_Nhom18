package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.DeviceEntity;
import com.alcohol.alcoholdetectionsystem.enums.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    boolean existsByDeviceId(String deviceId);
    Optional<DeviceEntity> findByDeviceId(String deviceId);
    Page<DeviceEntity> findByStatus(DeviceStatus status, Pageable pageable);
    Page<DeviceEntity> findByNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(
            String name, String deviceId, Pageable pageable);
    Page<DeviceEntity> findByStatusAndNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(
            DeviceStatus status, String name, String deviceId, Pageable pageable);
    long countByStatus(DeviceStatus status);
    List<DeviceEntity> findByNextCalibrationBefore(LocalDateTime dateTime);
    long countByNextCalibrationBefore(LocalDateTime dateTime);
}
