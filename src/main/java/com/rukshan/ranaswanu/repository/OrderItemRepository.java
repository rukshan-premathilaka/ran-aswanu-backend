package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
