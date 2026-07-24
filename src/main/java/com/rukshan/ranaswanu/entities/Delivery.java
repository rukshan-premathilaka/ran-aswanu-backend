package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "deliveries", schema = "dbo")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "delivery_status", nullable = false)
    private Boolean deliveryStatus;

    @Column(name = "assigned_date")
    private Instant assignedDate;

    @Column(name = "estimated_delivery_date")
    private Instant estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private Instant actualDeliveryDate;

    @Size(max = 500)
    @Nationalized
    @Column(name = "delivery_note", length = 500)
    private String deliveryNote;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "delivery")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "delivery")
    private Set<TransportationRequest> transportationRequests = new LinkedHashSet<>();


}