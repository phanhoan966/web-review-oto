package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.repository.UserFollowRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.publicsite.service.NotificationService;
import com.example.autoreview.domain.User;
import com.example.autoreview.domain.UserFollow;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewerService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final UserFollowRepository userFollowRepository;
    private final NotificationService notificationService;

    public ReviewerService(UserRepository userRepository, ReviewRepository reviewRepository, UserFollowRepository userFollowRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.userFollowRepository = userFollowRepository;
        this.notificationService = notificationService;
    }

    public List<ReviewerDto> topReviewers(int limit, String email) {
        List<ReviewerDto> dtos = userRepository.findTopReviewers(PageRequest.of(0, limit)).stream()
                .map(DtoMapper::toReviewerDto)
                .toList();
        applyReviewCounts(dtos);
        applyFollowing(dtos, findUser(email));
        return dtos;
    }

    public ReviewerDto getById(Long id, String email) {
        ReviewerDto dto = userRepository.findById(id)
                .map(DtoMapper::toReviewerDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        applyReviewCounts(List.of(dto));
        applyFollowing(List.of(dto), findUser(email));
        return dto;
    }

    public ReviewerDto getByUsername(String username, String email) {
        ReviewerDto dto = userRepository.findByUsername(username)
                .map(DtoMapper::toReviewerDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        applyReviewCounts(List.of(dto));
        applyFollowing(List.of(dto), findUser(email));
        return dto;
    }

    @Transactional
    public void follow(Long targetId, String actorEmail) {
        User actor = findRequiredUser(actorEmail);
        if (actor.getId().equals(targetId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không thể tự theo dõi chính mình");
        }
        User target = userRepository.findById(targetId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (userFollowRepository.existsByFollowerAndFollowing(actor, target)) {
            return;
        }
        UserFollow follow = new UserFollow();
        follow.setFollower(actor);
        follow.setFollowing(target);
        follow.setCreatedAt(Instant.now());
        userFollowRepository.save(follow);
        target.setFollowers((target.getFollowers() == null ? 0 : target.getFollowers()) + 1);
        userRepository.save(target);
        notificationService.notifyFollow(target, actor);
    }

    @Transactional
    public void unfollow(Long targetId, String actorEmail) {
        User actor = findRequiredUser(actorEmail);
        User target = userRepository.findById(targetId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        UserFollow follow = userFollowRepository.findByFollowerAndFollowing(actor, target).orElse(null);
        if (follow == null) {
            return;
        }
        userFollowRepository.delete(follow);
        int current = target.getFollowers() == null ? 0 : target.getFollowers();
        target.setFollowers(Math.max(0, current - 1));
        userRepository.save(target);
    }

    private void applyReviewCounts(List<ReviewerDto> dtos) {
        Set<Long> userIds = dtos.stream().map(ReviewerDto::getId).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, Long> counts = reviewRepository.countApprovedByAuthorIds(userIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        dtos.forEach(dto -> dto.setReviewCount(counts.getOrDefault(dto.getId(), 0L).intValue()));
    }

    private void applyFollowing(List<ReviewerDto> dtos, User viewer) {
        if (viewer == null) {
            return;
        }
        Set<Long> ids = dtos.stream().map(ReviewerDto::getId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> followingIds = userFollowRepository.findFollowingIds(viewer, ids);
        dtos.forEach(dto -> dto.setFollowing(followingIds.contains(dto.getId())));
    }

    private User findUser(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private User findRequiredUser(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập");
        }
        return userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"));
    }
}
