package com.example.autoreview.admin.controller;

import com.example.autoreview.domain.ReviewStatus;
import com.example.autoreview.publicsite.dto.response.ReviewDto;
import com.example.autoreview.publicsite.dto.response.ReviewListResponse;
import com.example.autoreview.publicsite.service.ReviewService;
import com.example.autoreview.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;

    public AdminReviewController(ReviewService reviewService, CurrentUserResolver currentUserResolver) {
        this.reviewService = reviewService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public ResponseEntity<ReviewListResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) ReviewStatus status) {
        if (status == null) {
            return ResponseEntity.ok(reviewService.listAll(page, size));
        }
        return ResponseEntity.ok(reviewService.listByStatus(status, page, size));
    }

    @GetMapping("/pending")
    public ResponseEntity<ReviewListResponse> pending(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) int size) {
        return ResponseEntity.ok(reviewService.listByStatus(ReviewStatus.PENDING, page, size));
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<List<ReviewDto>> mostViewed(@RequestParam(defaultValue = "5") @Min(1) int limit) {
        return ResponseEntity.ok(reviewService.mostViewed(limit, null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.approve(id, email, true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getAdmin(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.approve(id, email, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.restoreRejected(id, email);
        return ResponseEntity.noContent().build();
    }
}
