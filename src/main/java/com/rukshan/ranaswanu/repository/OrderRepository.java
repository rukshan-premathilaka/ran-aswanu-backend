package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}