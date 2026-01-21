package com.example.autoreview.repository;

import com.example.autoreview.entity.Comment;
import com.example.autoreview.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewOrderByCreatedAtAsc(Review review);
}
