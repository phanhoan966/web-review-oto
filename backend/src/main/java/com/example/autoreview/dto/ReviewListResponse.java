package com.example.autoreview.dto;

import java.util.List;

public class ReviewListResponse {
    private List<ReviewDto> reviews;
    private long total;

    public ReviewListResponse() {
    }

    public ReviewListResponse(List<ReviewDto> reviews, long total) {
        this.reviews = reviews;
        this.total = total;
    }

    public List<ReviewDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDto> reviews) {
        this.reviews = reviews;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
