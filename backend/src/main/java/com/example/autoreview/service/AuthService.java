package com.example.autoreview.service;

import com.example.autoreview.dto.AuthResponse;
import com.example.autoreview.dto.ForgotPasswordRequest;
import com.example.autoreview.dto.LoginRequest;
import com.example.autoreview.dto.RegisterRequest;
import com.example.autoreview.dto.ResetPasswordRequest;
import com.example.autoreview.dto.UserProfileDto;
import com.example.autoreview.entity.PasswordResetToken;
import com.example.autoreview.entity.User;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.PasswordResetTokenRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.security.JwtUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already in use");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add("ROLE_USER");
        user.setFollowers(0);
        user.setRating(5.0);
        user.setReviewCount(0);
        user.setAvatarUrl("https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=200&q=60");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        UserProfileDto profile = DtoMapper.toUserProfile(user);
        return new AuthResponse(profile);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String token = jwtUtil.generateToken(authentication.getName(), findUserRoles(request.getEmail()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        AuthResponse response = new AuthResponse(DtoMapper.toUserProfile(user));
        response.getUser().setAvatarUrl(user.getAvatarUrl());
        response.getUser().setFollowers(user.getFollowers());
        response.getUser().setRating(user.getRating());
        response.getUser().setReviewCount(user.getReviewCount());
        return responseWithToken(response, token);
    }

    public String issueToken(String email) {
        return jwtUtil.generateToken(email, findUserRoles(email));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
        PasswordResetToken resetToken = new PasswordResetToken();
        String token = UUID.randomUUID().toString();
        resetToken.setTokenHash(hash(token));
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsedFalse(hash(request.getToken()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Token expired");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        token.setUsed(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

    private Set<String> findUserRoles(String email) {
        return userRepository.findByEmail(email)
                .map(User::getRoles)
                .orElse(Set.of());
    }

    private AuthResponse responseWithToken(AuthResponse response, String token) {
        UserProfileDto profile = response.getUser();
        profile.setAvatarUrl(profile.getAvatarUrl());
        return response;
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing not available", e);
        }
    }
}
