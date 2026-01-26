package com.example.autoreview.publicsite.dto.response;

import java.time.Instant;

public class CommentDto {
    private Long id;
    private String content;
    private String authorName;
    private String authorAvatar;
    private String authorUsername;
    private Integer authorFollowers;
    private Integer authorReviewCount;
    private Double authorRating;
    private String authorBio;
    private boolean anonymous;
    private Instant createdAt;
    private Long parentId;
    private Integer likes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Integer getAuthorFollowers() {
        return authorFollowers;
    }

    public void setAuthorFollowers(Integer authorFollowers) {
        this.authorFollowers = authorFollowers;
    }

    public Integer getAuthorReviewCount() {
        return authorReviewCount;
    }

    public void setAuthorReviewCount(Integer authorReviewCount) {
        this.authorReviewCount = authorReviewCount;
    }

    public Double getAuthorRating() {
        return authorRating;
    }

    public void setAuthorRating(Double authorRating) {
        this.authorRating = authorRating;
    }

    public String getAuthorBio() {
        return authorBio;
    }

    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }
}
