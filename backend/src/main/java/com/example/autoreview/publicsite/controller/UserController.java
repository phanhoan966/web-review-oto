package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.ReviewListResponse;
import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.publicsite.service.ReviewService;
import com.example.autoreview.publicsite.service.ReviewerService;
import com.example.autoreview.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ReviewerService reviewerService;
    private final ReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;

    public UserController(ReviewerService reviewerService, ReviewService reviewService, CurrentUserResolver currentUserResolver) {
        this.reviewerService = reviewerService;
        this.reviewService = reviewService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDto> profile(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewerService.getById(id, email));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewListResponse> reviews(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.listPublicByAuthor(id, page, size, email));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ReviewerDto> profileByUsername(@PathVariable String username, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewerService.getByUsername(username, email));
    }

    @GetMapping("/username/{username}/reviews")
    public ResponseEntity<ReviewListResponse> reviewsByUsername(@PathVariable String username, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.listPublicByAuthorUsername(username, page, size, email));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> follow(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewerService.follow(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollow(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewerService.unfollow(id, email);
        return ResponseEntity.noContent().build();
    }
}
