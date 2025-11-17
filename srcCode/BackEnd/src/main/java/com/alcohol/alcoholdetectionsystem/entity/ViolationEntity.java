package com.alcohol.alcoholdetectionsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "violations", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_payment_deadline", columnList = "paymentDeadline")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private AlcoholTestEntity alcoholTest;

    @ManyToOne
    @JoinColumn(name = "processed_by")
    private UserEntity processedBy;

    @Column(nullable = false)
    private String level;

    @Column(length = 20)
    private String violationCode;

    @Column(nullable = false)
    private Double fineAmount;

    @Column(nullable = false)
    private String status = "unpaid";

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paymentDeadline;
    private LocalDateTime paymentDate;
    private LocalDateTime paidAt;

    @Column(columnDefinition = "boolean default false")
    private Boolean licenseConfiscated = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean vehicleDetained = false;

    public ViolationEntity(AlcoholTestEntity test, String level, Double fineAmount) {
        this.alcoholTest = test;
        this.level = level;
        this.fineAmount = fineAmount;
    }
}