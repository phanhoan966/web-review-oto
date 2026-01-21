package com.example.autoreview.repository;

import com.example.autoreview.entity.VehicleBrand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {
    List<VehicleBrand> findByFeaturedTrueOrderBySortOrderAscNameAsc();
}
