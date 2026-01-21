package com.example.autoreview.controller;

import com.example.autoreview.dto.ReviewDto;
import com.example.autoreview.dto.ReviewListResponse;
import com.example.autoreview.service.ReviewService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
