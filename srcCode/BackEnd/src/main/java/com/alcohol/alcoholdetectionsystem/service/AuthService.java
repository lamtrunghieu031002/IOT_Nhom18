package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.request.LoginRequest;
import com.alcohol.alcoholdetectionsystem.dto.request.RegisterRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.LoginResponse;
import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import com.alcohol.alcoholdetectionsystem.enums.RoleEnum;
import com.alcohol.alcoholdetectionsystem.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    @NonFinal
    @Value("${jwt.secretKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.expirationMs}")
    protected long EXPIRATION;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String generateToken(UserEntity userEntity){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userEntity.getUsername())
                .issuer("iot.system.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(userEntity))
                .claim("userId", userEntity.getId())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(UserEntity userEntity) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        stringJoiner.add("ROLE_" + userEntity.getRole());
        return stringJoiner.toString();
    }


    public void register(RegisterRequest register) {
        if (userRepository.existsByUsername(register.getUsername())){
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(register.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        String registerRole = register.getRole();
        RoleEnum role = RoleEnum.OFFICER;
        if (registerRole != null && !registerRole.isBlank()) {
            try {
                role = RoleEnum.valueOf(registerRole.toUpperCase());
            } catch (Exception ignored) {}
        }
        UserEntity userEntity = UserEntity.builder()
                .username(register.getUsername())
                .email(register.getEmail())
                .password(passwordEncoder.encode(register.getPassword()))
                .fullName(register.getFullName())
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userEntity);
    }

    public LoginResponse login(LoginRequest login) {
        UserEntity userEntity = userRepository.findByUsername(login.getUsername()).orElseThrow(() -> new IllegalArgumentException("Username not found"));
        if (!passwordEncoder.matches(login.getPassword(), userEntity.getPassword())){
            throw new IllegalArgumentException("Wrong password");
        }
        String token = generateToken(userEntity);
        return LoginResponse.builder()
                .accessToken(token)
                .userId(userEntity.getId())
                .role(userEntity.getRole())
                .build();
    }
}
