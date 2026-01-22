package com.example.autoreview.repository;

import com.example.autoreview.entity.Review;
import com.example.autoreview.entity.ReviewStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select r from Review r where r.status = :status and (:brand is null or r.brand.name = :brand) and (:fuelType is null or r.fuelType = :fuelType) and (:priceSegment is null or r.priceSegment = :priceSegment)")
    Page<Review> findByFilters(@org.springframework.data.repository.query.Param("status") com.example.autoreview.entity.ReviewStatus status, @org.springframework.data.repository.query.Param("brand") String brand, @org.springframework.data.repository.query.Param("fuelType") String fuelType, @org.springframework.data.repository.query.Param("priceSegment") String priceSegment, Pageable pageable);

    @Query("select r from Review r where r.status = com.example.autoreview.entity.ReviewStatus.APPROVED order by r.views desc")
    Page<Review> findMostViewed(Pageable pageable);

    Page<Review> findByStatus(com.example.autoreview.entity.ReviewStatus status, Pageable pageable);

    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    Page<Review> findByAuthorEmailOrderByCreatedAtDesc(String email, Pageable pageable);
}
