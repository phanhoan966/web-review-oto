package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.NotificationDto;
import com.example.autoreview.publicsite.service.NotificationService;
import com.example.autoreview.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserResolver currentUserResolver;

    public NotificationController(NotificationService notificationService, CurrentUserResolver currentUserResolver) {
        this.notificationService = notificationService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(@AuthenticationPrincipal Object principal, HttpServletRequest request, @RequestParam(defaultValue = "20") int limit) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(notificationService.list(email, limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        long count = notificationService.countUnread(email);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        notificationService.markRead(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        notificationService.markAllRead(email);
        return ResponseEntity.noContent().build();
    }
}
