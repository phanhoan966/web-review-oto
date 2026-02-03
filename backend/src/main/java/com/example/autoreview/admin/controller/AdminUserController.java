package com.example.autoreview.admin.controller;

import com.example.autoreview.admin.dto.request.CreateUserRequest;
import com.example.autoreview.admin.dto.request.UpdateUserRequest;
import com.example.autoreview.admin.dto.request.UpdateUserRolesRequest;
import com.example.autoreview.admin.dto.response.AdminUserDto;
import com.example.autoreview.admin.service.AdminUserService;
import com.example.autoreview.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final CurrentUserResolver currentUserResolver;

    public AdminUserController(AdminUserService adminUserService, CurrentUserResolver currentUserResolver) {
        this.adminUserService = adminUserService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> list(@AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.ok(adminUserService.listUsers(email));
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<AdminUserDto>> deleted(@AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.ok(adminUserService.listDeletedUsers(email));
    }

    @PostMapping
    public ResponseEntity<AdminUserDto> create(@Valid @RequestBody CreateUserRequest request, @AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(email, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDto> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request, @AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.ok(adminUserService.updateUser(email, id, request));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserDto> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request, @AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.ok(adminUserService.updateRoles(email, id, request.getRoles()));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<AdminUserDto> restore(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        return ResponseEntity.ok(adminUserService.restoreUser(email, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest httpServletRequest) {
        String email = currentUserResolver.resolveEmail(principal, httpServletRequest);
        adminUserService.deleteUser(email, id);
        return ResponseEntity.noContent().build();
    }
}
