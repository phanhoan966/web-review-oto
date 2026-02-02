package com.example.autoreview.publicsite.service;

import com.example.autoreview.domain.Comment;
import com.example.autoreview.domain.Notification;
import com.example.autoreview.domain.Review;
import com.example.autoreview.domain.User;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.publicsite.dto.response.NotificationDto;
import com.example.autoreview.repository.CommentRepository;
import com.example.autoreview.repository.NotificationRepository;
import com.example.autoreview.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void notifyNewComment(Review review, Comment comment, User actor) {
        if (actor == null) {
            return;
        }
        Set<Long> recipientIds = new HashSet<>();
        if (review.getAuthor() != null && review.getAuthor().getId() != null && !review.getAuthor().getId().equals(actor.getId())) {
            recipientIds.add(review.getAuthor().getId());
        }
        if (comment.getParent() != null && comment.getParent().getAuthor() != null && comment.getParent().getAuthor().getId() != null && !comment.getParent().getAuthor().getId().equals(actor.getId())) {
            recipientIds.add(comment.getParent().getAuthor().getId());
        }
        Set<Long> participantIds = commentRepository.findByReviewOrderByCreatedAtAsc(review).stream()
                .map(Comment::getAuthor)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(actor.getId()))
                .collect(Collectors.toSet());
        recipientIds.addAll(participantIds);
        if (recipientIds.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        for (Long recipientId : recipientIds) {
            User recipient = userRepository.findById(recipientId).orElse(null);
            if (recipient == null) {
                continue;
            }
            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setActor(actor);
            notification.setReview(review);
            notification.setComment(comment);
            notification.setCreatedAt(now);
            notification.setReadFlag(false);
            String type = resolveType(recipientId, review, comment);
            notification.setType(type);
            notification.setMessage(resolveMessage(type, actor.getUsername()));
            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(String email, int limit) {
        User user = requireUser(email);
        int size = Math.max(1, Math.min(limit, 50));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, PageRequest.of(0, size)).stream()
                .map(DtoMapper::toNotificationDto)
                .toList();
    }

    @Transactional
    public void markRead(Long id, String email) {
        User user = requireUser(email);
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        if (notification.isReadFlag()) {
            return;
        }
        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public long countUnread(String email) {
        User user = requireUser(email);
        return notificationRepository.countByRecipientAndReadFlagFalse(user);
    }

    private String resolveType(Long recipientId, Review review, Comment comment) {
        if (comment.getParent() != null && comment.getParent().getAuthor() != null && recipientId.equals(comment.getParent().getAuthor().getId())) {
            return "REPLY_TO_COMMENT";
        }
        if (review.getAuthor() != null && recipientId.equals(review.getAuthor().getId())) {
            return "COMMENT_ON_OWN_REVIEW";
        }
        return "NEW_COMMENT_ON_WATCHED_REVIEW";
    }

    private String resolveMessage(String type, String actorName) {
        String name = StringUtils.hasText(actorName) ? actorName : "Người dùng";
        if ("REPLY_TO_COMMENT".equals(type)) {
            return name + " đã trả lời bình luận của bạn";
        }
        if ("COMMENT_ON_OWN_REVIEW".equals(type)) {
            return name + " đã bình luận bài viết của bạn";
        }
        return name + " đã bình luận bài viết bạn đang theo dõi";
    }

    private User requireUser(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập");
        }
        return userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
