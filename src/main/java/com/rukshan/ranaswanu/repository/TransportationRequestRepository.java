package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.TransportationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TransportationRequestRepository extends JpaRepository<TransportationRequest, Long> {

    List<TransportationRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<TransportationRequest> findByIdAndUser_Id(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransportationRequest t where t.id = :id")
    Optional<TransportationRequest> findByIdForUpdate(@Param("id") Long id);

    List<TransportationRequest> findByUser_IdNotAndRequestStatusAndDeliveryIsNull(Long userId, String requestStatus);

    List<TransportationRequest> findByDelivery_Id(Long deliveryId);

    long countByDelivery_Id(Long deliveryId);

    boolean existsByDelivery_IdAndUser_Id(Long deliveryId, Long userId);
}
