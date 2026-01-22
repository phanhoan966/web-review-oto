package com.example.autoreview.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserResolver {

    private final JwtUtil jwtUtil;

    public CurrentUserResolver(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String resolveEmail(UserDetails userDetails, HttpServletRequest request) {
        return resolveEmail((Object) userDetails, request);
    }

    public String resolveEmail(Object principal, HttpServletRequest request) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String subject) {
            return subject;
        }
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            try {
                return jwtUtil.parse(bearer.substring(7)).getSubject();
            } catch (Exception ignored) {
            }
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    try {
                        return jwtUtil.parse(cookie.getValue()).getSubject();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }
}
