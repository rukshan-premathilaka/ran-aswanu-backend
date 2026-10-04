package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    boolean existsByVehicle_IdAndDeliveryStatusIn(Long vehicleId, List<String> statuses);
}
