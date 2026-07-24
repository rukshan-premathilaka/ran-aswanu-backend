package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product_listings", schema = "dbo")
public class ProductListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "listing_status", nullable = false)
    private Boolean listingStatus;

    @NotNull
    @Column(name = "available_stock", nullable = false, precision = 18, scale = 2)
    private BigDecimal availableStock;

    @NotNull
    @Column(name = "price_per_unit", nullable = false, precision = 18, scale = 2)
    private BigDecimal pricePerUnit;

    @NotNull
    @Column(name = "minimum_order_quantity", nullable = false, precision = 18, scale = 2)
    private BigDecimal minimumOrderQuantity;

    @Column(name = "harvested_date")
    private Instant harvestedDate;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "delivery_option", nullable = false)
    private String deliveryOption;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 10)
    @NotNull
    @Nationalized
    @Column(name = "unit_of_measurement", nullable = false, length = 10)
    private String unitOfMeasurement;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "category", nullable = false)
    private String category;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Size(max = 255)
    @Nationalized
    @Column(name = "product_image")
    private String productImage;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "list")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();


}