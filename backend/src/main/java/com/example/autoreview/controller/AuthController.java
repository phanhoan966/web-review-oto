package com.example.autoreview.controller;

import com.example.autoreview.dto.AuthResponse;
import com.example.autoreview.dto.ForgotPasswordRequest;
import com.example.autoreview.dto.LoginRequest;
import com.example.autoreview.dto.PasswordChangeRequest;
import com.example.autoreview.dto.RegisterRequest;
import com.example.autoreview.dto.ResetPasswordRequest;
import com.example.autoreview.security.JwtUtil;
import com.example.autoreview.service.AuthService;
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
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final String cookieDomain;
    private final JwtUtil jwtUtil;

    public AuthController(
          AuthService authService,
          @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
          @Value("${app.cookie.secure:false}") boolean cookieSecure,
          @Value("${app.cookie.same-site:Lax}") String cookieSameSite,
          @Value("${app.cookie.domain:}") String cookieDomain,
          JwtUtil jwtUtil
    ) {
        this.authService = authService;
        this.expirationMinutes = expirationMinutes;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
        this.cookieDomain = cookieDomain;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        String token = authService.issueToken(request.getEmail());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, buildAuthCookie(token)).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        String token = authService.issueToken(request.getEmail());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, buildAuthCookie(token)).body(response);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginAdmin(request);
        String token = authService.issueToken(request.getEmail());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, buildAuthCookie(token)).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("AUTH_TOKEN", "")
            .path("/")
            .httpOnly(true)
            .secure(cookieSecure)
            .maxAge(0)
            .sameSite(cookieSameSite);
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }
        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        String email = userDetails != null ? userDetails.getUsername() : extractEmailFromRequest(request);
        if (!StringUtils.hasText(email)) {
            ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("AUTH_TOKEN", "")
                    .path("/")
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .maxAge(0)
                    .sameSite(cookieSameSite);
            if (StringUtils.hasText(cookieDomain)) {
                builder.domain(cookieDomain);
            }
            response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
            return ResponseEntity.status(401).build();
        }
        AuthResponse authResponse = authService.me(email);
        String token = authService.issueToken(email);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, buildAuthCookie(token)).body(authResponse);
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

    private String extractEmailFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("AUTH_TOKEN".equals(cookie.getName())) {
                try {
                    return jwtUtil.parse(cookie.getValue()).getSubject();
                } catch (Exception ignored) {
                    return null;
                }
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
}
