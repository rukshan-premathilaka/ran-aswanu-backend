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
@Table(name = "orders", schema = "dbo")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "order_date_time", nullable = false)
    private Instant orderDateTime;

    @NotNull
    @Column(name = "order_status", nullable = false)
    private Boolean orderStatus;

    @Size(max = 255)
    @Nationalized
    @Column(name = "notes")
    private String notes;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "contact_number", nullable = false, length = 50)
    private String contactNumber;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "payment_method", nullable = false, length = 100)
    private String paymentMethod;

    @NotNull
    @Column(name = "payment_status", nullable = false)
    private Boolean paymentStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();


}