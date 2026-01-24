package com.example.autoreview.mapper;

import com.example.autoreview.publicsite.dto.response.BrandDto;
import com.example.autoreview.publicsite.dto.response.CommentDto;
import com.example.autoreview.publicsite.dto.response.ReviewDto;
import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.publicsite.dto.response.UserProfileDto;
import com.example.autoreview.domain.Comment;
import com.example.autoreview.domain.Review;
import com.example.autoreview.domain.User;
import com.example.autoreview.domain.VehicleBrand;

public class DtoMapper {

    private DtoMapper() {
    }

    public static UserProfileDto toUserProfile(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setFollowers(user.getFollowers());
        dto.setRating(user.getRating());
        dto.setReviewCount(user.getReviewCount());
        return dto;
    }

    public static ReviewDto toReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setExcerpt(review.getExcerpt());
        dto.setContent(review.getContent());
        dto.setHeroImageUrl(review.getHeroImageUrl());
        dto.setBrand(review.getBrand() != null ? review.getBrand().getName() : null);
        dto.setVehicleModel(review.getVehicleModel());
        dto.setVehicleYear(review.getVehicleYear());
        dto.setLikes(review.getLikes());
        dto.setCommentsCount(review.getCommentsCount());
        dto.setViews(review.getViews());
        dto.setFuelType(review.getFuelType());
        dto.setPriceSegment(review.getPriceSegment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setPublishedAt(review.getPublishedAt());
        dto.setStatus(review.getStatus() != null ? review.getStatus().name() : null);
        if (review.getAuthor() != null) {
            dto.setAuthorName(review.getAuthor().getUsername());
            dto.setAuthorAvatar(review.getAuthor().getAvatarUrl());
        }
        return dto;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setAnonymous(comment.isAnonymous());
        if (!comment.isAnonymous() && comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getUsername());
            dto.setAuthorAvatar(comment.getAuthor().getAvatarUrl());
        }
        return dto;
    }

    public static ReviewerDto toReviewerDto(User user) {
        ReviewerDto dto = new ReviewerDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setFollowers(user.getFollowers());
        dto.setRating(user.getRating());
        dto.setReviewCount(user.getReviewCount());
        return dto;
    }

    public static BrandDto toBrandDto(VehicleBrand brand) {
        BrandDto dto = new BrandDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setLogoUrl(brand.getLogoUrl());
        return dto;
    }
}
