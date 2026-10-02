package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.TransportationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportationRequestRepository extends JpaRepository<TransportationRequest, Long> {

    List<TransportationRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<TransportationRequest> findByIdAndUser_Id(Long id, Long userId);

    List<TransportationRequest> findByUser_IdNotAndRequestStatusAndDeliveryIsNull(Long userId, String requestStatus);

    List<TransportationRequest> findByDelivery_Id(Long deliveryId);

    long countByDelivery_Id(Long deliveryId);

    boolean existsByDelivery_IdAndUser_Id(Long deliveryId, Long userId);
}
