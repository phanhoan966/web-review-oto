package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReviewerService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ReviewerService(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewerDto> topReviewers(int limit) {
        List<ReviewerDto> dtos = userRepository.findTopReviewers(PageRequest.of(0, limit)).stream()
                .map(DtoMapper::toReviewerDto)
                .toList();
        applyReviewCounts(dtos);
        return dtos;
    }

    public ReviewerDto getById(Long id) {
        ReviewerDto dto = userRepository.findById(id)
                .map(DtoMapper::toReviewerDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        applyReviewCounts(List.of(dto));
        return dto;
    }

    public ReviewerDto getByUsername(String username) {
        ReviewerDto dto = userRepository.findByUsername(username)
                .map(DtoMapper::toReviewerDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        applyReviewCounts(List.of(dto));
        return dto;
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
}
