package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.AuthResponse;
import com.example.autoreview.publicsite.dto.request.ForgotPasswordRequest;
import com.example.autoreview.publicsite.dto.request.LoginRequest;
import com.example.autoreview.publicsite.dto.request.PasswordChangeRequest;
import com.example.autoreview.publicsite.dto.request.RegisterRequest;
import com.example.autoreview.publicsite.dto.request.ResetPasswordRequest;
import com.example.autoreview.security.CurrentUserResolver;
import com.example.autoreview.publicsite.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final long expirationMinutes;
    private final long refreshDays;
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final String cookieDomain;
    private final CurrentUserResolver currentUserResolver;

    public AuthController(
          AuthService authService,
          @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
          @Value("${app.jwt.refresh-days:30}") long refreshDays,
          @Value("${app.cookie.secure:false}") boolean cookieSecure,
          @Value("${app.cookie.same-site:Lax}") String cookieSameSite,
          @Value("${app.cookie.domain:}") String cookieDomain,
          CurrentUserResolver currentUserResolver
    ) {
        this.authService = authService;
        this.expirationMinutes = expirationMinutes;
        this.refreshDays = refreshDays;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
        this.cookieDomain = cookieDomain;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        String email = authResponse.getUser().getEmail();
        String accessToken = authService.issueToken(email);
        String refreshToken = authService.issueRefreshToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(accessToken));
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        String email = authResponse.getUser().getEmail();
        String accessToken = authService.issueToken(email);
        String refreshToken = authService.issueRefreshToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(accessToken));
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.loginAdmin(request);
        String email = authResponse.getUser().getEmail();
        String accessToken = authService.issueToken(email);
        String refreshToken = authService.issueRefreshToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(accessToken));
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refresh = extractRefreshToken(request);
        if (StringUtils.hasText(refresh)) {
            authService.revokeRefreshToken(refresh);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, buildClearAuthCookie());
        response.addHeader(HttpHeaders.SET_COOKIE, buildClearRefreshCookie());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefresh = extractRefreshToken(request);
        if (!StringUtils.hasText(rawRefresh)) {
            return ResponseEntity.status(401).build();
        }
        String email = authService.consumeRefreshToken(rawRefresh);
        AuthResponse authResponse = authService.me(email);
        String accessToken = authService.issueToken(email);
        String refreshToken = authService.issueRefreshToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(accessToken));
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken));
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        String email = currentUserResolver.resolveEmail(userDetails, request);
        if (!StringUtils.hasText(email)) {
            response.addHeader(HttpHeaders.SET_COOKIE, buildClearAuthCookie());
            response.addHeader(HttpHeaders.SET_COOKIE, buildClearRefreshCookie());
            return ResponseEntity.status(401).build();
        }
        AuthResponse authResponse = authService.me(email);
        String token = authService.issueToken(email);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(token));
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("REFRESH_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String buildAuthCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("AUTH_TOKEN", token)
            .path("/")
            .httpOnly(true)
            .secure(cookieSecure)
            .maxAge(expirationMinutes * 60)
            .sameSite(cookieSameSite);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        return builder.build().toString();
    }

    private String buildRefreshCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("REFRESH_TOKEN", token)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .maxAge(refreshDays * 24 * 60 * 60)
                .sameSite(cookieSameSite);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        return builder.build().toString();
    }

    private String buildClearAuthCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("AUTH_TOKEN", "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .maxAge(0)
                .sameSite(cookieSameSite);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        return builder.build().toString();
    }

    private String buildClearRefreshCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("REFRESH_TOKEN", "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .maxAge(0)
                .sameSite(cookieSameSite);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        return builder.build().toString();
    }
}
