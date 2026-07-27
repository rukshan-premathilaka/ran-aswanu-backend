package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.ProductListingRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.ProductListingResponseDto;
import com.rukshan.ranaswanu.entities.ProductListing;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class ProductListingService {

    @Autowired
    private ProductListingRepository productListingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.rukshan.ranaswanu.service.FileStorageService fileStorageService;

    public ProductListingResponseDto create(String farmerEmail, ProductListingRequestDto requestData) {
        User farmer = requireFarmer(farmerEmail);

        ProductListing listing = new ProductListing();
        listing.setUser(farmer);
        applyRequestFields(listing, requestData);
        listing.setListingStatus(false); // starts unpublished/draft
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        productListingRepository.save(listing);
        return toResponseDto(listing);
    }

    public List<ProductListingResponseDto> getMyListings(String farmerEmail) {
        User farmer = requireFarmer(farmerEmail);
        return productListingRepository.findByUserId(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ProductListingResponseDto update(String farmerEmail, Long listId, ProductListingRequestDto requestData) {
        ProductListing listing = findOwned(farmerEmail, listId);
        applyRequestFields(listing, requestData);
        listing.setUpdatedAt(Instant.now());
        productListingRepository.save(listing);
        return toResponseDto(listing);
    }

    public ProductListingResponseDto setPublished(String farmerEmail, Long listId, boolean published) {
        ProductListing listing = findOwned(farmerEmail, listId);
        listing.setListingStatus(published);
        listing.setUpdatedAt(Instant.now());
        productListingRepository.save(listing);
        return toResponseDto(listing);
    }

    public void delete(String farmerEmail, Long listId) {
        productListingRepository.delete(findOwned(farmerEmail, listId));
    }

    public ProductListingResponseDto uploadImage(String farmerEmail, Long listId, MultipartFile file) {
        ProductListing listing = findOwned(farmerEmail, listId);

        if (listing.getProductImage() != null) {
            fileStorageService.deleteFile(listing.getProductImage());
        }

        listing.setProductImage(fileStorageService.storeFile(file, "product-images"));
        listing.setUpdatedAt(Instant.now());
        productListingRepository.save(listing);
        return toResponseDto(listing);
    }

    // ---------------- PUBLIC BROWSE ----------------

    public List<ProductListingResponseDto> browsePublished(String category, String keyword) {
        List<ProductListing> listings;

        if (keyword != null && !keyword.isBlank()) {
            listings = productListingRepository.findByProductNameContainingIgnoreCase(keyword);
        } else if (category != null && !category.isBlank()) {
            listings = productListingRepository.findByCategoryIgnoreCase(category);
        } else {
            listings = productListingRepository.findAll();
        }

        return listings.stream()
                .filter(l -> Boolean.TRUE.equals(l.getListingStatus()))
                .map(this::toResponseDto)
                .toList();
    }

    public ProductListingResponseDto getById(Long listId) {
        ProductListing listing = productListingRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("Product listing not found: " + listId));
        return toResponseDto(listing);
    }

    // ---------------- HELPERS ----------------

    private void applyRequestFields(ProductListing listing, ProductListingRequestDto requestData) {
        listing.setProductName(requestData.getProductName());
        listing.setCategory(requestData.getCategory());
        listing.setDescription(requestData.getDescription());
        listing.setUnitOfMeasurement(requestData.getUnitOfMeasurement());
        listing.setPricePerUnit(requestData.getPricePerUnit());
        listing.setAvailableStock(requestData.getAvailableStock());
        listing.setMinimumOrderQuantity(requestData.getMinimumOrderQuantity());
        listing.setHarvestedDate(requestData.getHarvestedDate());
        listing.setDeliveryOption(requestData.getDeliveryOption());
    }

    private User requireFarmer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (!"FARMER".equals(user.getRole())) {
            throw new AccessDeniedException("Only farmers can manage product listings");
        }
        return user;
    }

    private ProductListing findOwned(String farmerEmail, Long listId) {
        User farmer = requireFarmer(farmerEmail);
        return productListingRepository.findById(listId)
                .filter(l -> l.getUser().getId().equals(farmer.getId()))
                .orElseThrow(() -> new AccessDeniedException("Listing not found or not owned by this farmer"));
    }

    private ProductListingResponseDto toResponseDto(ProductListing listing) {
        return ProductListingResponseDto.builder()
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
                .productImage(listing.getProductImage())
                .listingStatus(listing.getListingStatus())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}