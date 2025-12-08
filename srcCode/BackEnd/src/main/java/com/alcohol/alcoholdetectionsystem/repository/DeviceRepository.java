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
    Page<DeviceEntity> findByStatus(DeviceStatus status, Pageable pageable);
    Page<DeviceEntity> findByStatusAndNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(
            DeviceStatus status, String name, String deviceId, Pageable pageable);
    long countByStatus(DeviceStatus status);
    long countByNextCalibrationBefore(LocalDateTime dateTime);

    Page<DeviceEntity> findByStatusNotAndNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(DeviceStatus deviceStatus, String search, String search1, Pageable pageable);

    Page<DeviceEntity> findByStatusNot(DeviceStatus deviceStatus, Pageable pageable);

    List<DeviceEntity> findByNextCalibrationBeforeAndStatusNot(LocalDateTime now, DeviceStatus deviceStatus);

    List<DeviceEntity> findByDeviceIdInAndStatusNot(List<String> macAddresses, DeviceStatus deviceStatus);

    Optional<DeviceEntity> findByDeviceIdAndStatusNot(String deviceId, DeviceStatus deviceStatus);

    Optional<DeviceEntity> findByDeviceId(String deviceId);
}
