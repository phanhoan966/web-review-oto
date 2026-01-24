package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.ReviewListResponse;
import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.publicsite.service.ReviewService;
import com.example.autoreview.publicsite.service.ReviewerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final ReviewerService reviewerService;
    private final ReviewService reviewService;

    public UserController(ReviewerService reviewerService, ReviewService reviewService) {
        this.reviewerService = reviewerService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerDto> profile(@PathVariable Long id) {
        return ResponseEntity.ok(reviewerService.getById(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewListResponse> reviews(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.listPublicByAuthor(id, page, size));
    }
}
