package com.example.autoreview.admin.service;

import com.example.autoreview.admin.dto.request.CreateUserRequest;
import com.example.autoreview.admin.dto.request.UpdateUserRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private static final Set<String> ALLOWED_ROLES = Set.of(Roles.USER, Roles.ADMIN, Roles.MANAGER, Roles.SYSTEM_ADMIN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserDto> listUsers(String actorEmail) {
        User actor = requireActor(actorEmail);
        enforcePermission(actor, null, null, Action.LIST);
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toDto)
                .toList();
    }

    public AdminUserDto createUser(String actorEmail, CreateUserRequest request) {
        User actor = requireActor(actorEmail);
        validateRoles(request.getRoles());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        Set<String> nextRoles = normalizeRoles(request.getRoles());
        enforcePermission(actor, null, nextRoles, Action.CREATE);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(nextRoles);
        user.setFollowers(0);
        user.setRating(0.0);
        user.setReviewCount(0);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return toDto(user);
    }

    public AdminUserDto updateUser(String actorEmail, Long id, UpdateUserRequest request) {
        User actor = requireActor(actorEmail);
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Set<String> nextRoles = request.getRoles() != null ? normalizeRoles(request.getRoles()) : user.getRoles();
        enforcePermission(actor, user, nextRoles, Action.UPDATE);
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setRoles(nextRoles);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return toDto(user);
    }

    public void deleteUser(String actorEmail, Long id) {
        User actor = requireActor(actorEmail);
        User target = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        enforcePermission(actor, target, target.getRoles(), Action.DELETE);
        userRepository.delete(target);
    }

    public AdminUserDto updateRoles(String actorEmail, Long id, Set<String> roles) {
        validateRoles(roles);
        User actor = requireActor(actorEmail);
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Set<String> next = normalizeRoles(roles);
        enforcePermission(actor, user, next, Action.UPDATE);
        user.setRoles(next);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return toDto(user);
    }

    private User requireActor(String email) {
        if (email == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private void validateRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one role is required");
        }
        if (!ALLOWED_ROLES.containsAll(roles)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role provided");
        }
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        Set<String> next = new HashSet<>(roles);
        next.add(Roles.USER);
        return next;
    }

    private boolean hasRole(User user, String role) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }

    private void enforcePermission(User actor, User target, Set<String> nextRoles, Action action) {
        boolean isSystem = hasRole(actor, Roles.SYSTEM_ADMIN);
        boolean isManager = hasRole(actor, Roles.MANAGER);
        boolean isAdmin = hasRole(actor, Roles.ADMIN);
        if (!isSystem && !isManager && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        boolean targetIsSelf = target != null && actor.getId().equals(target.getId());
        boolean targetSystem = target != null && hasRole(target, Roles.SYSTEM_ADMIN);
        boolean targetManager = target != null && hasRole(target, Roles.MANAGER);
        boolean targetAdmin = target != null && hasRole(target, Roles.ADMIN);
        boolean nextHasSystem = nextRoles != null && nextRoles.contains(Roles.SYSTEM_ADMIN);
        boolean nextHasManager = nextRoles != null && nextRoles.contains(Roles.MANAGER);
        boolean nextHasAdmin = nextRoles != null && nextRoles.contains(Roles.ADMIN);

        if (isSystem) {
            return;
        }

        if (isAdmin) {
            if (action == Action.LIST) {
                return;
            }
            if (action == Action.UPDATE && targetIsSelf) {
                if (nextHasSystem || nextHasManager) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
                }
                return;
            }
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }

        if (action == Action.LIST) {
            return;
        }
        if (action == Action.CREATE) {
            if (nextHasManager || nextHasAdmin || nextHasSystem) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            return;
        }
        if (action == Action.UPDATE) {
            if (!targetIsSelf && (targetManager || targetAdmin || targetSystem)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            if (!targetIsSelf && (nextHasManager || nextHasAdmin || nextHasSystem)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            if (targetIsSelf && (nextHasAdmin || nextHasSystem)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            return;
        }
        if (action == Action.DELETE) {
            if (targetIsSelf || targetManager || targetAdmin || targetSystem) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
            }
            return;
        }

        throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
    }

    private enum Action {
        LIST,
        CREATE,
        UPDATE,
        DELETE
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
