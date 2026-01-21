package com.example.autoreview.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.parse(token);
                String subject = claims.getSubject();
                Object rolesObj = claims.get("roles");
                var authorities = rolesObj instanceof Iterable<?> iterable
                        ? buildAuthorities(iterable)
                        : Collections.<SimpleGrantedAuthority>emptyList();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        subject, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "AUTH_TOKEN".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    private java.util.List<SimpleGrantedAuthority> buildAuthorities(Iterable<?> roles) {
        java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (Object role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.toString()));
        }
        return authorities;
    }
}
