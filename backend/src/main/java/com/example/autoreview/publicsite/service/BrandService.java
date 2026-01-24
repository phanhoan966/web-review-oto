package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.BrandDto;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.VehicleBrandRepository;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.domain.ReviewStatus;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BrandService {

    private final VehicleBrandRepository vehicleBrandRepository;
    private final ReviewRepository reviewRepository;

    public BrandService(VehicleBrandRepository vehicleBrandRepository, ReviewRepository reviewRepository) {
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<BrandDto> featured() {
        return vehicleBrandRepository.findByFeaturedTrueOrderBySortOrderAscNameAsc().stream()
                .map(brand -> {
                    BrandDto dto = DtoMapper.toBrandDto(brand);
                    dto.setReviewCount(reviewRepository.countByBrandIdAndStatus(brand.getId(), ReviewStatus.APPROVED));
                    return dto;
                })
                .toList();
    }
}
