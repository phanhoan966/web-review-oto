package com.example.autoreview.repository;

import com.example.autoreview.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);

    void deleteByExpiresAtBefore(Instant cutoff);
}
