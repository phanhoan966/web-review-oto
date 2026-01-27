package com.example.autoreview.repository;

import com.example.autoreview.domain.User;
import com.example.autoreview.domain.UserFollow;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    @Query("select f.following.id from UserFollow f where f.follower = :follower and f.following.id in :targetIds")
    Set<Long> findFollowingIds(@Param("follower") User follower, @Param("targetIds") Set<Long> targetIds);
}
