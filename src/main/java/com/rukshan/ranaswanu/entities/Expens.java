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
@Table(name = "expenses", schema = "dbo")
public class Expens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id", nullable = false)
    private Long id;

    @Column(name = "expense_date")
    private Instant expenseDate;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}