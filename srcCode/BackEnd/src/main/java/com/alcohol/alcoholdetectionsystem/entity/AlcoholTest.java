package com.alcohol.alcoholdetectionsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "alcohol_tests", indexes = {
        @Index(name = "idx_test_time", columnList = "testTime"),
        @Index(name = "idx_subject_id", columnList = "subjectId"),
        @Index(name = "idx_officer_id", columnList = "officer_id"),
        @Index(name = "idx_device_id", columnList = "device_id")
})
@Data
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class AlcoholTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private String subjectId;

    private Integer subjectAge;

    @Column(length = 10)
    private String subjectGender;

    @Column(nullable = false)
    private Double alcoholLevel;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private String vehiclePlate;

    @Column(nullable = false)
    private String location;

    private String locationCoordinates;

    @Column(nullable = false)
    private String violationLevel;

    @Column
    private String notes;

    @Column(length = 20)
    private String status;

    @CreatedDate
    private LocalDateTime testTime;

    @OneToOne(mappedBy = "alcoholTest", cascade = CascadeType.ALL)
    private Violation violation;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
