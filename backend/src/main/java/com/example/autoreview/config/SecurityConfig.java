package com.example.autoreview.config;

import com.example.autoreview.security.JwtAuthenticationFilter;
import com.example.autoreview.security.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final AppCorsProperties corsProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsService userDetailsService, AppCorsProperties corsProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/admin/login", "/auth/register", "/auth/forgot-password", "/auth/reset-password", "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/uploads").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/pending").hasAnyAuthority(Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/reviews/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reviews", "/reviews/most-viewed", "/reviews/*/comments", "/reviews/*", "/brands/**", "/reviewers/**", "/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/reviews/**").hasAuthority(Roles.USER)
                        .requestMatchers(HttpMethod.POST, "/reviews/*/comments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/comments/*/like", "/comments/*/unlike").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").hasAnyAuthority(Roles.USER, Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/reviews/*/approve", "/reviews/*/reject").hasAnyAuthority(Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN)
                        .requestMatchers("/admin/users/**").hasAnyAuthority(Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN)
                        .requestMatchers("/admin/**").hasAnyAuthority(Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN)
                        .anyRequest().authenticated())
                .authenticationProvider(daoAuthenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
