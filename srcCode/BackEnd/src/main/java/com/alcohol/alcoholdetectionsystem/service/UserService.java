package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.request.UpdateUserRequest;
import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import com.alcohol.alcoholdetectionsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAllByIsDeletedFalse();
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserEntity updateUser(Long id, UpdateUserRequest updateUserRequest) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updateUserRequest.getPassword() != null) {
            userEntity.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }

        if (updateUserRequest.getFullName() != null) {
            userEntity.setFullName(updateUserRequest.getFullName());
        }

        if (updateUserRequest.getRole() != null) {
            userEntity.setRole(updateUserRequest.getRole());
        }

        return userRepository.save(userEntity);
    }

    public void deleteUser(Long id) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        userEntity.setDeleted(true);
        userRepository.save(userEntity);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
