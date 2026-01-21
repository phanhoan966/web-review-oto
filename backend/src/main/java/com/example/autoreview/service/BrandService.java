package com.example.autoreview.service;

import com.example.autoreview.dto.BrandDto;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.VehicleBrandRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BrandService {

    private final VehicleBrandRepository vehicleBrandRepository;

    public BrandService(VehicleBrandRepository vehicleBrandRepository) {
        this.vehicleBrandRepository = vehicleBrandRepository;
    }

    public List<BrandDto> featured() {
        return vehicleBrandRepository.findByFeaturedTrueOrderBySortOrderAscNameAsc().stream()
                .map(DtoMapper::toBrandDto)
                .toList();
    }
}
