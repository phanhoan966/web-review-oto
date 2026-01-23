package com.example.autoreview.repository;

import java.util.List;

import com.example.autoreview.domain.Comment;
import com.example.autoreview.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewOrderByCreatedAtAsc(Review review);

    Page<Comment> findByReviewOrderByCreatedAtDesc(Review review, Pageable pageable);
}
