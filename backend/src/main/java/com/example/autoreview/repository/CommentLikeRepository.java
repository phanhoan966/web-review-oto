package com.example.autoreview.repository;

import com.example.autoreview.domain.Comment;
import com.example.autoreview.domain.CommentLike;
import com.example.autoreview.domain.User;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);

    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    @Query("select cl.comment.id from CommentLike cl where cl.user = :user and cl.comment.id in :commentIds")
    Set<Long> findLikedCommentIds(@Param("user") User user, @Param("commentIds") Collection<Long> commentIds);
}
