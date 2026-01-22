package com.example.autoreview.service;

import com.example.autoreview.dto.CommentDto;
import com.example.autoreview.dto.CreateCommentRequest;
import com.example.autoreview.dto.CreateReviewRequest;
import com.example.autoreview.dto.ReviewDto;
import com.example.autoreview.dto.ReviewListResponse;
import com.example.autoreview.dto.UpdateReviewRequest;
import com.example.autoreview.entity.Comment;
import com.example.autoreview.entity.Review;
import com.example.autoreview.entity.ReviewStatus;
import com.example.autoreview.entity.User;
import com.example.autoreview.entity.VehicleBrand;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.CommentRepository;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.repository.VehicleBrandRepository;
import com.example.autoreview.security.Roles;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public ReviewService(ReviewRepository reviewRepository, VehicleBrandRepository vehicleBrandRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.reviewRepository = reviewRepository;
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getFeed(String brand, String fuelType, String priceSegment, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByFilters(ReviewStatus.APPROVED, brand, fuelType, priceSegment, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listByAuthor(String email, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByAuthorEmailOrderByCreatedAtDesc(email, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> mostViewed(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "views"));
        return reviewRepository.findMostViewed(pageable).getContent().stream().map(DtoMapper::toReviewDto).toList();
    }

    @Transactional
    public ReviewDto getPublic(Long id) {
        Review review = reviewRepository.findByIdAndStatus(id, ReviewStatus.APPROVED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setViews((review.getViews() == null ? 0 : review.getViews()) + 1);
        reviewRepository.save(review);
        return DtoMapper.toReviewDto(review);
    }

    @Transactional
    public ReviewDto create(String authorEmail, CreateReviewRequest request) {
        User author = userRepository.findByEmail(authorEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        VehicleBrand brand = vehicleBrandRepository.findById(request.getBrandId()).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Brand not found"));
        Review review = new Review();
        review.setTitle(request.getTitle());
        review.setExcerpt(request.getExcerpt());
        review.setContent(request.getContent());
        review.setHeroImageUrl(request.getHeroImageUrl());
        review.setVehicleModel(request.getVehicleModel());
        review.setVehicleYear(request.getVehicleYear());
        review.setFuelType(request.getFuelType());
        review.setPriceSegment(request.getPriceSegment());
        review.setBrand(brand);
        review.setAuthor(author);
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        review.setLikes(0);
        review.setCommentsCount(0);
        review.setViews(0);
        reviewRepository.save(review);
        author.setReviewCount((author.getReviewCount() == null ? 0 : author.getReviewCount()) + 1);
        userRepository.save(author);
        return DtoMapper.toReviewDto(review);
    }

    @Transactional
    public ReviewDto updateOwn(Long id, String email, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending review can be edited");
        }
        review.setTitle(request.getTitle());
        review.setExcerpt(request.getExcerpt());
        review.setContent(request.getContent());
        review.setHeroImageUrl(request.getHeroImageUrl());
        review.setVehicleModel(request.getVehicleModel());
        review.setVehicleYear(request.getVehicleYear());
        review.setFuelType(request.getFuelType());
        review.setPriceSegment(request.getPriceSegment());
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
        return DtoMapper.toReviewDto(review);
    }

    @Transactional
    public void approve(Long id, String approverEmail, boolean approve) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User approver = userRepository.findByEmail(approverEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (!hasAdminRole(approver)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        review.setStatus(approve ? ReviewStatus.APPROVED : ReviewStatus.REJECTED);
        review.setPublishedAt(approve ? Instant.now() : null);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> listPending(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByStatus(ReviewStatus.PENDING, pageable).map(DtoMapper::toReviewDto);
    }

    @Transactional
    public CommentDto addComment(Long reviewId, String authorEmail, CreateCommentRequest request) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot comment on unapproved review");
        }
        User author = null;
        if (authorEmail != null) {
            author = userRepository.findByEmail(authorEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        }
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setReview(review);
        comment.setCreatedAt(Instant.now());
        Comment saved = commentRepository.save(comment);
        review.setCommentsCount((review.getCommentsCount() == null ? 0 : review.getCommentsCount()) + 1);
        reviewRepository.save(review);
        return DtoMapper.toCommentDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comments unavailable for unapproved review");
        }
        return commentRepository.findByReviewOrderByCreatedAtAsc(review).stream().map(DtoMapper::toCommentDto).toList();
    }

    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        boolean canDelete = hasAdminRole(user) || comment.getAuthor().getEmail().equals(email);
        if (!canDelete) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        Review review = comment.getReview();
        commentRepository.delete(comment);
        if (review.getCommentsCount() != null && review.getCommentsCount() > 0) {
            review.setCommentsCount(review.getCommentsCount() - 1);
            reviewRepository.save(review);
        }
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().contains(Roles.ADMIN) || user.getRoles().contains(Roles.MANAGER) || user.getRoles().contains(Roles.SYSTEM_ADMIN);
    }
}
