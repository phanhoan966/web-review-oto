package com.example.autoreview.repository;

import com.example.autoreview.domain.Review;
import com.example.autoreview.domain.ReviewLike;
import com.example.autoreview.domain.User;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReviewAndUser(Review review, User user);

    Optional<ReviewLike> findByReviewAndUser(Review review, User user);

    @Query("select rl.review.id from ReviewLike rl where rl.user = :user and rl.review.id in :reviewIds")
    Set<Long> findLikedReviewIds(@Param("user") User user, @Param("reviewIds") Collection<Long> reviewIds);
}
