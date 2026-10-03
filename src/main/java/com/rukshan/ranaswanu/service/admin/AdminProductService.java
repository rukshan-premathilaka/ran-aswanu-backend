package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminProductResponseDto;
import com.rukshan.ranaswanu.entities.ProductListing;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AdminProductService {

    @Autowired
    private ProductListingRepository productListingRepository;

    // ALL listings (published and disabled), newest first. Every filter is optional.
    @Transactional(readOnly = true)
    public List<AdminProductResponseDto> list(String category, Boolean listingStatus, String search) {
        final String wantedCategory = (category == null || category.isBlank()) ? null : category.trim();
        final String keyword = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        return productListingRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(p -> wantedCategory == null || wantedCategory.equalsIgnoreCase(p.getCategory()))
                .filter(p -> listingStatus == null || listingStatus.equals(p.getListingStatus()))
                .filter(p -> keyword == null
                        || (p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword)))
                .map(this::toDto)
                .toList();
    }

    // Own lookup (not the public getById) so a disabled product is still visible to the Admin
    @Transactional(readOnly = true)
    public AdminProductResponseDto getById(Long listId) {
        return toDto(requireListing(listId));
    }

    // Enable / disable only. Products are never deleted (orders point to them).
    @Transactional
    public AdminProductResponseDto setActive(Long listId, boolean active) {
        ProductListing listing = requireListing(listId);
        listing.setListingStatus(active);
        listing.setUpdatedAt(Instant.now());
        productListingRepository.save(listing);
        return toDto(listing);
    }

    // ---------------- HELPERS ----------------

    private ProductListing requireListing(Long listId) {
        return productListingRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private AdminProductResponseDto toDto(ProductListing listing) {
        return AdminProductResponseDto.builder()
                .listId(listing.getId())
                .farmerId(listing.getUser().getId())
                .farmerName(listing.getUser().getName())
                .productName(listing.getProductName())
                .category(listing.getCategory())
                .description(listing.getDescription())
                .unitOfMeasurement(listing.getUnitOfMeasurement())
                .pricePerUnit(listing.getPricePerUnit())
                .availableStock(listing.getAvailableStock())
                .minimumOrderQuantity(listing.getMinimumOrderQuantity())
                .harvestedDate(listing.getHarvestedDate())
                .deliveryOption(listing.getDeliveryOption())
                .productImage(listing.getProductImage() != null ? "/files/" + listing.getProductImage() : null)
                .listingStatus(listing.getListingStatus())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}
