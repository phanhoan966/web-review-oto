package com.example.autoreview.controller;

import com.example.autoreview.dto.AdminUserDto;
import com.example.autoreview.dto.UpdateUserRolesRequest;
import com.example.autoreview.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> list() {
        return ResponseEntity.ok(adminUserService.listUsers());
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserDto> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminUserService.updateRoles(id, request.getRoles()));
    }
}
