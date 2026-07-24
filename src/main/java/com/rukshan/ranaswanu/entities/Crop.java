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
@Table(name = "crops", schema = "dbo")
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "crop_name", nullable = false)
    private String cropName;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @NotNull
    @Column(name = "harvest_quantity", nullable = false, precision = 18, scale = 2)
    private BigDecimal harvestQuantity;

    @NotNull
    @Column(name = "harvest_date", nullable = false)
    private Instant harvestDate;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "crop")
    private Set<FieldPlot> fieldPlots = new LinkedHashSet<>();


}