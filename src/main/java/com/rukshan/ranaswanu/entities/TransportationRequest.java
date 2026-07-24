package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transportation_requests", schema = "dbo")
public class TransportationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transportation_request_id", nullable = false)
    private Long id;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "delivery_location", nullable = false)
    private String deliveryLocation;

    @NotNull
    @Column(name = "requested_date_time", nullable = false)
    private Instant requestedDateTime;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @NotNull
    @Column(name = "estimated_weight", nullable = false)
    private Long estimatedWeight;

    @NotNull
    @Column(name = "request_status", nullable = false)
    private Boolean requestStatus;

    @Size(max = 500)
    @Nationalized
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "\"size\"", nullable = false, length = 50)
    private String size;

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


}