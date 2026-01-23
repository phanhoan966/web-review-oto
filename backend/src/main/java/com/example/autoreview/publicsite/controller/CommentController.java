package com.example.autoreview.controller;

import com.example.autoreview.security.CurrentUserResolver;
import com.example.autoreview.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final ReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;

    public CommentController(ReviewService reviewService, CurrentUserResolver currentUserResolver) {
        this.reviewService = reviewService;
        this.currentUserResolver = currentUserResolver;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.deleteComment(id, email);
        return ResponseEntity.noContent().build();
    }
}
