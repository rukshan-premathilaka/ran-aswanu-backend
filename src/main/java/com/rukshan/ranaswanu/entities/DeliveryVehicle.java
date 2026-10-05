package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "delivery_vehicles")
public class DeliveryVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "vehicle_name", nullable = false, length = 100)
    private String vehicleName;

    @Size(max = 50)
    @NotNull
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Size(max = 50)
    @NotNull
    @Column(name = "registration_number", nullable = false, length = 50, unique = true)
    private String registrationNumber;

    @NotNull
    @Positive
    @Column(name = "capacity_kg", nullable = false)
    private Long capacityKg;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
