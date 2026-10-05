package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.ProductListing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;

import java.util.List;
import java.util.Optional;

public interface ProductListingRepository extends JpaRepository<ProductListing, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<ProductListing> {
    List<ProductListing> findByUserId(Long userId);
    Optional<ProductListing> findByIdAndUserId(Long id, Long userId);
    List<ProductListing> findByCategoryIgnoreCase(String category);
    List<ProductListing> findByProductNameContainingIgnoreCase(String keyword);


    List<ProductListing> findByListingStatusTrue();


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductListing p where p.id in :ids order by p.id")
    List<ProductListing> findAllForUpdate(@Param("ids") Collection<Long> ids);


    @Modifying
    @Query("update ProductListing p set p.availableStock = p.availableStock + :qty, p.updatedAt = :now where p.id = :id")
    int addStock(@Param("id") Long id, @Param("qty") java.math.BigDecimal qty, @Param("now") Instant now);
    long countByUserIdAndListingStatusTrue(Long userId); // dashboard: active listings
    List<ProductListing> findByListingStatusTrueAndCategoryIgnoreCase(String category);
    List<ProductListing> findByListingStatusTrueAndProductNameContainingIgnoreCase(String keyword);
    List<ProductListing> findByListingStatusTrueAndCategoryIgnoreCaseAndProductNameContainingIgnoreCase(
            String category, String keyword);


    long countByUserId(Long userId);
    long countByAdminDisabledTrue();
    long countByListingStatusTrueAndAdminDisabledFalse();   // published
    long countByListingStatusFalseAndAdminDisabledFalse();  // draft


    @Query("select count(p) from ProductListing p where p.createdAt >= :from and p.createdAt < :to")
    long countCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("select min(p.createdAt) from ProductListing p")
    Instant findEarliestCreatedAt();
}