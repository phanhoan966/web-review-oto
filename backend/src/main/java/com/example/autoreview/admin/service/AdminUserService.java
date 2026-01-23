package com.example.autoreview.admin.service;

import com.example.autoreview.admin.dto.response.AdminUserDto;
import com.example.autoreview.domain.User;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.security.Roles;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private static final Set<String> ALLOWED_ROLES = Set.of(Roles.USER, Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN);

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserDto> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toDto)
                .toList();
    }

    public AdminUserDto updateRoles(Long id, Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one role is required");
        }
        if (!ALLOWED_ROLES.containsAll(roles)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role provided");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Set<String> next = new HashSet<>(roles);
        next.add(Roles.USER);
        user.setRoles(next);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return toDto(user);
    }

    private AdminUserDto toDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setFollowers(user.getFollowers());
        dto.setRating(user.getRating());
        dto.setReviewCount(user.getReviewCount());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
