package com.example.autoreview.repository;

import java.util.Optional;

import com.example.autoreview.domain.Review;
import com.example.autoreview.domain.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select r from Review r where r.status = :status and (:brand is null or r.brand.name = :brand) and (:fuelType is null or r.fuelType = :fuelType) and (:priceSegment is null or r.priceSegment = :priceSegment)")
    Page<Review> findByFilters(@org.springframework.data.repository.query.Param("status") ReviewStatus status, @org.springframework.data.repository.query.Param("brand") String brand, @org.springframework.data.repository.query.Param("fuelType") String fuelType, @org.springframework.data.repository.query.Param("priceSegment") String priceSegment, Pageable pageable);

    @Query("select r from Review r where r.status = com.example.autoreview.domain.ReviewStatus.APPROVED order by r.views desc")
    Page<Review> findMostViewed(Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    Page<Review> findByAuthorEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<Review> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, ReviewStatus status, Pageable pageable);

    Page<Review> findByAuthorUsernameAndStatusOrderByCreatedAtDesc(String username, ReviewStatus status, Pageable pageable);

    long countByBrandIdAndStatus(Long brandId, ReviewStatus status);

    @Query("select r.author.id, count(r) from Review r where r.status = com.example.autoreview.domain.ReviewStatus.APPROVED and r.author.id in :authorIds group by r.author.id")
    java.util.List<Object[]> countApprovedByAuthorIds(@org.springframework.data.repository.query.Param("authorIds") java.util.Set<Long> authorIds);

    long countByAuthorIdAndStatus(Long authorId, ReviewStatus status);
}
