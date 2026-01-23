package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.publicsite.service.ReviewerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @GetMapping("/top")
    public ResponseEntity<List<ReviewerDto>> top(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reviewerService.topReviewers(limit));
    }
}
