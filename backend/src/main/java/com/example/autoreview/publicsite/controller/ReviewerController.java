package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.publicsite.service.ReviewerService;
import com.example.autoreview.security.CurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviewers")
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final CurrentUserResolver currentUserResolver;

    public ReviewerController(ReviewerService reviewerService, CurrentUserResolver currentUserResolver) {
        this.reviewerService = reviewerService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/top")
    public ResponseEntity<List<ReviewerDto>> top(@RequestParam(defaultValue = "5") int limit, @AuthenticationPrincipal Object principal, HttpServletRequest request) {
        String email = currentUserResolver.resolveEmail(principal, request);
        return ResponseEntity.ok(reviewerService.topReviewers(limit, email));
    }
}
