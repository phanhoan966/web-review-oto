package com.example.autoreview.controller;

import com.example.autoreview.dto.CommentDto;
import com.example.autoreview.dto.CreateCommentRequest;
import com.example.autoreview.dto.CreateReviewRequest;
import com.example.autoreview.dto.ReviewDto;
import com.example.autoreview.dto.ReviewListResponse;
import com.example.autoreview.dto.UpdateReviewRequest;
import com.example.autoreview.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
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

    @GetMapping("/most-viewed")
    public ResponseEntity<List<ReviewDto>> mostViewed(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reviewService.mostViewed(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getPublic(id));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.create(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateOwn(id, userDetails.getUsername(), request));
    }

    @GetMapping("/pending")
    public ResponseEntity<ReviewListResponse> pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<ReviewDto> pending = reviewService.listPending(page, size);
        return ResponseEntity.ok(new ReviewListResponse(pending.getContent(), pending.getTotalElements()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.approve(id, userDetails.getUsername(), true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.approve(id, userDetails.getUsername(), false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(reviewService.addComment(id, userDetails.getUsername(), request));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> listComments(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.listComments(id));
    }
}
