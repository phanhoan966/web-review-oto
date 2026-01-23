package com.example.autoreview.repository;

import com.example.autoreview.domain.RefreshToken;
import com.example.autoreview.domain.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(Instant cutoff);
}
