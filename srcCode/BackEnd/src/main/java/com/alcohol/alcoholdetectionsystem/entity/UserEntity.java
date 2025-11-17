package com.alcohol.alcoholdetectionsystem.entity;

import com.alcohol.alcoholdetectionsystem.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String fullName;

    @Column(nullable = false)
    private RoleEnum role;

    @Column
    private boolean isDeleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}