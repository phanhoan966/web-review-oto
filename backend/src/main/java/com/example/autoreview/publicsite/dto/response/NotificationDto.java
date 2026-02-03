package com.example.autoreview.publicsite.dto.response;

import java.time.Instant;

public class NotificationDto {

    private Long id;
    private String type;
    private String message;
    private boolean read;
    private Instant createdAt;
    private Long reviewId;
    private String reviewSlug;
    private String reviewTitle;
    private Long commentId;
    private String actorName;
    private String actorUsername;
    private String actorAvatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewSlug() {
        return reviewSlug;
    }

    public void setReviewSlug(String reviewSlug) {
        this.reviewSlug = reviewSlug;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorAvatar() {
        return actorAvatar;
    }

    public void setActorAvatar(String actorAvatar) {
        this.actorAvatar = actorAvatar;
    }
}
