package com.alcohol.alcoholdetectionsystem.entity;

import com.alcohol.alcoholdetectionsystem.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "devices")
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String name;

    private String model;

    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.INACTIVE;

    @ManyToOne
    @JoinColumn(name = "registered_by", nullable = false)
    private UserEntity registeredBy;

    private LocalDateTime lastCalibration;
    private LocalDateTime nextCalibration;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "deviceEntity",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<AlcoholTestEntity> alcoholTests;

}