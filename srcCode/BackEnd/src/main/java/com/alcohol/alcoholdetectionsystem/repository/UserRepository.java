package com.alcohol.alcoholdetectionsystem.repository;

import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username); // Giữ lại nếu cần, nhưng nên dùng cái dưới

    Optional<UserEntity> findByUsernameAndIsDeletedFalse(String username); // Dùng cho login và findByUsername

    Optional<UserEntity> findByIdAndIsDeletedFalse(Long id); // Dùng cho getById, update, delete

    List<UserEntity> findAllByIsDeletedFalse();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
