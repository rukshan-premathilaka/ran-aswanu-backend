package com.rukshan.ranaswanu.service.admin;

import com.rukshan.ranaswanu.dto.response.admin.AdminProductDto;
import com.rukshan.ranaswanu.dto.response.admin.PageResponseDto;
import com.rukshan.ranaswanu.entities.ProductListing;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.spec.AdminProductSpecs;
import com.rukshan.ranaswanu.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class AdminProductService {

    private static final List<String> STATUSES = List.of("PUBLISHED", "DRAFT", "DISABLED");

    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponseDto<AdminProductDto> list(String q, String category, String status, Long farmerId,
                                                 LocalDate from, LocalDate to, int page, int size) {
        String normalizedStatus = null;
        if (AdminQueryUtil.hasText(status)) {
            normalizedStatus = status.trim().toUpperCase();
            if (!STATUSES.contains(normalizedStatus)) {
                throw new IllegalArgumentException("Invalid status filter. Allowed values: " + STATUSES);
            }
        }
        AdminQueryUtil.checkDateRange(from, to);

        Page<ProductListing> result = productListingRepository.findAll(
                AdminProductSpecs.filter(q, category, normalizedStatus, farmerId,
                        AdminQueryUtil.startOfDay(from), AdminQueryUtil.startOfNextDay(to)),
                AdminQueryUtil.pageable(page, size));

        return PageResponseDto.of(result.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public AdminProductDto get(Long id) {
        return toDto(requireProduct(id));
    }


    @Transactional
    public AdminProductDto setDisabled(Long id, boolean disabled) {
        ProductListing p = requireProduct(id);

        if (p.isAdminDisabled() != disabled) {
            p.setAdminDisabled(disabled);
            p.setAdminDisabledAt(disabled ? Instant.now() : null);
            p.setUpdatedAt(Instant.now());
            productListingRepository.save(p);

            String title = disabled ? "Product disabled by admin" : "Product enabled by admin";
            String message = disabled
                    ? "Your product \"" + p.getProductName() + "\" was disabled by an administrator. "
                      + "Please contact support."
                    : "Your product \"" + p.getProductName() + "\" was enabled again by an administrator.";
            notificationService.create(p.getUser().getId(), title, message); // never throws, cuts long text
        }
        return toDto(p);
    }

    private ProductListing requireProduct(Long id) {
        return productListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private AdminProductDto toDto(ProductListing p) {
        User farmer = p.getUser();
        String status = p.isAdminDisabled() ? "DISABLED"
                : Boolean.TRUE.equals(p.getListingStatus()) ? "PUBLISHED" : "DRAFT";

        return AdminProductDto.builder()
                .listId(p.getId())
                .productName(p.getProductName())
                .category(p.getCategory())
                .description(p.getDescription())
                .unitOfMeasurement(p.getUnitOfMeasurement())
                .pricePerUnit(p.getPricePerUnit())
                .availableStock(p.getAvailableStock())
                .minimumOrderQuantity(p.getMinimumOrderQuantity())
                .deliveryOption(p.getDeliveryOption())
                .productImage(p.getProductImage() != null ? "/files/" + p.getProductImage() : null)
                .harvestedDate(p.getHarvestedDate())
                .listingStatus(p.getListingStatus())
                .adminDisabled(p.isAdminDisabled())
                .adminDisabledAt(p.getAdminDisabledAt())
                .status(status)
                .farmerId(farmer.getId())
                .farmerName(farmer.getName())
                .farmerEmail(farmer.getEmail())
                .farmerActive(farmer.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
