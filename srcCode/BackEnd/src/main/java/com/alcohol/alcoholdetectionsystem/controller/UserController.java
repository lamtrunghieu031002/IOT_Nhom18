package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.request.UpdateUserRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.ApiResponse;
import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import com.alcohol.alcoholdetectionsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserEntity>>> getAllUsers() {
        try {
            List<UserEntity> userEntities = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, null, userEntities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.claims['userId']")
    public ResponseEntity<ApiResponse<UserEntity>> getUserById(@PathVariable Long id) {
        try {
            UserEntity userEntity = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, userEntity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.claims['userId']")
    public ResponseEntity<ApiResponse<UserEntity>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest updateUserRequest) {
        try {
            UserEntity updatedUserEntity = userService.updateUser(id, updateUserRequest);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User updated successfully", updatedUserEntity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserEntity>> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserEntity userEntity = userService.findByUsername(username);
            return ResponseEntity.ok(new ApiResponse<>(true, null, userEntity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
