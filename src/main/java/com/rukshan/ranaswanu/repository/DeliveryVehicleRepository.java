package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.DeliveryVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryVehicleRepository extends JpaRepository<DeliveryVehicle, Long> {
    List<DeliveryVehicle> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<DeliveryVehicle> findByIdAndUser_Id(Long id, Long userId);

    Optional<DeliveryVehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);
}
