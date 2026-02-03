package com.example.autoreview.repository;

import com.example.autoreview.domain.Notification;
import com.example.autoreview.domain.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    List<Notification> findByRecipientAndReadFlagFalse(User recipient);

    long countByRecipientAndReadFlagFalse(User recipient);
}
