package com.example.autoreview.repository;

import com.example.autoreview.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query("select u from User u order by u.followers desc, u.rating desc")
    List<User> findTopReviewers(org.springframework.data.domain.Pageable pageable);
}
