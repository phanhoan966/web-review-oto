package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.AuthResponse;
import com.example.autoreview.publicsite.dto.request.ForgotPasswordRequest;
import com.example.autoreview.publicsite.dto.request.LoginRequest;
import com.example.autoreview.publicsite.dto.request.RegisterRequest;
import com.example.autoreview.publicsite.dto.request.ResetPasswordRequest;
import com.example.autoreview.publicsite.dto.response.UserProfileDto;
import com.example.autoreview.domain.PasswordResetToken;
import com.example.autoreview.domain.RefreshToken;
import com.example.autoreview.domain.User;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.PasswordResetTokenRepository;
import com.example.autoreview.repository.RefreshTokenRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.security.JwtUtil;
import com.example.autoreview.security.Roles;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final long refreshExpirationDays;

    public AuthService(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager, @Value("${app.jwt.refresh-days:30}") long refreshExpirationDays) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshExpirationDays = refreshExpirationDays;
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
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return buildAuthResponse(user);
    }

    public AuthResponse loginAdmin(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!hasAdminRole(user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse me(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return buildAuthResponse(user);
    }

    public String issueToken(String email) {
        return jwtUtil.generateToken(email, findUserRoles(email));
    }

    @Transactional
    public String issueRefreshToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        refreshTokenRepository.deleteByUser(user);
        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(raw));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
        return raw;
    }

    @Transactional
    public String consumeRefreshToken(String rawToken) {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String email = token.getUser().getEmail();
        refreshTokenRepository.delete(token);
        return email;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(refreshTokenRepository::delete);
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

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password invalid");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private boolean hasAdminRole(User user) {
        Set<String> roles = user.getRoles();
        return roles.contains(Roles.ADMIN) || roles.contains(Roles.MANAGER) || roles.contains(Roles.SYSTEM_ADMIN);
    }

    private Set<String> findUserRoles(String email) {
        return userRepository.findByEmail(email)
                .map(User::getRoles)
                .orElse(Set.of());
    }

    private AuthResponse buildAuthResponse(User user) {
        AuthResponse response = new AuthResponse(DtoMapper.toUserProfile(user));
        response.getUser().setAvatarUrl(user.getAvatarUrl());
        response.getUser().setFollowers(user.getFollowers());
        response.getUser().setRating(user.getRating());
        response.getUser().setReviewCount(user.getReviewCount());
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
