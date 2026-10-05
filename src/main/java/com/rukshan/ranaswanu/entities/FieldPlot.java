package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "field_plots")
public class FieldPlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_plot_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "current_crop", nullable = false)
    private String currentCrop;

    @Size(max = 255)
    @NotNull
    @Column(name = "crop_variety", nullable = false)
    private String cropVariety;

    @Column(name = "area_unit", precision = 18, scale = 2)
    private BigDecimal areaUnit;

    @Size(max = 100)
    @NotNull
    @Column(name = "growth_stage", nullable = false, length = 100)
    private String growthStage;

    @Size(max = 100)
    @NotNull
    @Column(name = "health_condition", nullable = false, length = 100)
    private String healthCondition;

    @Column(name = "field_logs", columnDefinition = "text")
    private String fieldLogs;

    @NotNull
    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private Crop crop;


}