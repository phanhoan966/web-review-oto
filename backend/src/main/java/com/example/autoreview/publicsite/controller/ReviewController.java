package com.example.autoreview.controller;

import com.example.autoreview.dto.CommentDto;
import com.example.autoreview.dto.CreateCommentRequest;
import com.example.autoreview.dto.CreateReviewRequest;
import com.example.autoreview.dto.ReviewDto;
import com.example.autoreview.dto.ReviewListResponse;
import com.example.autoreview.dto.UpdateReviewRequest;
import com.example.autoreview.security.CurrentUserResolver;
import com.example.autoreview.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;

    public ReviewController(ReviewService reviewService, CurrentUserResolver currentUserResolver) {
        this.reviewService = reviewService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public ResponseEntity<ReviewListResponse> feed(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String priceSegment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getFeed(brand, fuelType, priceSegment, page, size));
    }

    @GetMapping("/mine")
    public ResponseEntity<ReviewListResponse> mine(
            @AuthenticationPrincipal Object principal,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.listByAuthor(email, page, size));
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<List<ReviewDto>> mostViewed(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reviewService.mostViewed(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getPublic(id));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@AuthenticationPrincipal Object principal, HttpServletRequest request, @Valid @RequestBody CreateReviewRequest requestBody) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.create(email, requestBody));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request, @Valid @RequestBody UpdateReviewRequest requestBody) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.updateOwn(id, email, requestBody));
    }

    @GetMapping("/pending")
    public ResponseEntity<ReviewListResponse> pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<ReviewDto> pending = reviewService.listPending(page, size);
        return ResponseEntity.ok(new ReviewListResponse(pending.getContent(), pending.getTotalElements()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.approve(id, email, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        reviewService.approve(id, email, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long id, @AuthenticationPrincipal Object principal, HttpServletRequest request, @Valid @RequestBody CreateCommentRequest commentRequest) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewService.addComment(id, email, commentRequest));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> listComments(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.listComments(id, page, size));
    }

}
