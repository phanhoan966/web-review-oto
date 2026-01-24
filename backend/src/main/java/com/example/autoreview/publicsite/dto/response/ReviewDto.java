package com.example.autoreview.publicsite.dto.response;

import java.time.Instant;

public class ReviewDto {
    private Long id;
    private Long authorId;
    private String title;
    private String excerpt;
    private String content;
    private String heroImageUrl;
    private String authorName;
    private String authorAvatar;
    private String authorUsername;
    private String brand;
    private String vehicleModel;
    private Integer vehicleYear;
    private Integer likes;
    private Integer commentsCount;
    private Integer views;
    private String fuelType;
    private String priceSegment;
    private Instant createdAt;
    private Instant publishedAt;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public Integer getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getPriceSegment() {
        return priceSegment;
    }

    public void setPriceSegment(String priceSegment) {
        this.priceSegment = priceSegment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
