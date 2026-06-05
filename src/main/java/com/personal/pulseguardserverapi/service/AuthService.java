package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.request.LoginRequest;
import com.personal.pulseguardserverapi.dto.request.RegisterRequest;
import com.personal.pulseguardserverapi.dto.response.AuthResponse;
import com.personal.pulseguardserverapi.entity.Consent;
import com.personal.pulseguardserverapi.entity.Credential;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.BadRequestException;
import com.personal.pulseguardserverapi.exception.UnauthorizedException;
import com.personal.pulseguardserverapi.repository.ConsentRepository;
import com.personal.pulseguardserverapi.repository.CredentialRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import com.personal.pulseguardserverapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final ConsentRepository consentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .sex(request.getSex())
                .age(request.getAge())
                .build();
        user = userRepository.save(user);

        Credential credential = Credential.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .lastChangedAt(LocalDateTime.now())
                .build();
        credentialRepository.save(credential);

        Consent consent = Consent.builder()
                .user(user)
                .consentGiven(request.isConsentGiven())
                .revokedAt(request.isConsentGiven() ? null : LocalDateTime.now())
                .build();
        consentRepository.save(consent);

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());
        return buildAuthResponse(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        Credential credential = credentialRepository.findByUser(user)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
