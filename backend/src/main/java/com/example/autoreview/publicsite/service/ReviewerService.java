package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.ReviewerDto;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ReviewerService {

    private final UserRepository userRepository;

    public ReviewerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<ReviewerDto> topReviewers(int limit) {
        return userRepository.findTopReviewers(PageRequest.of(0, limit)).stream()
                .map(DtoMapper::toReviewerDto)
                .toList();
    }
}
