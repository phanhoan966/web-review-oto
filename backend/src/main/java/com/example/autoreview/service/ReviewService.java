package com.example.autoreview.service;

import com.example.autoreview.dto.ReviewDto;
import com.example.autoreview.dto.ReviewListResponse;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.ReviewRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getFeed(String brand, String fuelType, String priceSegment, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<com.example.autoreview.entity.Review> reviews = reviewRepository.findByFilters(brand, fuelType, priceSegment, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> mostViewed(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "views"));
        return reviewRepository.findMostViewed(pageable).getContent().stream().map(DtoMapper::toReviewDto).toList();
    }
}
