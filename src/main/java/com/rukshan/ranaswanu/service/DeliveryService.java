package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.delivery.DeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.JoinDeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchesResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryRequestResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryStatusResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.JoinDeliveryResponseDto;
import com.rukshan.ranaswanu.entities.Delivery;
import com.rukshan.ranaswanu.entities.TransportationRequest;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ConflictException;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.DeliveryRepository;
import com.rukshan.ranaswanu.repository.TransportationRequestRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DeliveryService {

    private static final String OPEN = "OPEN";
    private static final String MATCHED = "MATCHED";
    private static final List<String> DELIVERY_STATUSES =
            List.of("PENDING", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "CANCELLED");

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private TransportationRequestRepository transportationRequestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public DeliveryRequestResponseDto createRequest(String email, DeliveryRequestDto request) {
        User user = requireUser(email);
        Instant now = Instant.now();

        // Save the request without a delivery so it can be matched later.
        TransportationRequest entity = new TransportationRequest();
        entity.setUser(user);
        entity.setPickupLocation(request.getPickupLocation().trim());
        entity.setDeliveryLocation(request.getDestination().trim());
        entity.setRequestedDateTime(request.getPreferredDateTime());
        entity.setVehicleType(request.getVehicleType().trim());
        entity.setEstimatedWeight(request.getEstimatedWeight());
        entity.setSize(request.getSize().trim());
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setSpecialInstructions(trimToNull(request.getSpecialInstructions()));
        entity.setRequestStatus(OPEN);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        transportationRequestRepository.save(entity);
        return toRequestResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<DeliveryRequestResponseDto> listMine(String email) {
        User user = requireUser(email);
        return transportationRequestRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryMatchesResponseDto findMatches(String email, Long requestId) {
        User user = requireUser(email);
        TransportationRequest mine = transportationRequestRepository.findByIdAndUser_Id(requestId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        List<DeliveryMatchResponseDto> matches = transportationRequestRepository
                .findByUser_IdNotAndRequestStatusAndDeliveryIsNull(user.getId(), OPEN)
                .stream()
                .map(candidate -> toMatch(mine, candidate))
                .filter(match -> match.getMatchScore() >= 0.5)
                .sorted(Comparator.comparingDouble(DeliveryMatchResponseDto::getMatchScore).reversed()
                        .thenComparing(DeliveryMatchResponseDto::getPreferredDateTime,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return DeliveryMatchesResponseDto.builder()
                .requestId(mine.getId())
                .totalMatches(matches.size())
                .matches(matches)
                .build();
    }

    @Transactional
    public JoinDeliveryResponseDto join(String email, Long requestId, JoinDeliveryRequestDto request) {
        User me = requireUser(email);
        TransportationRequest mine = transportationRequestRepository.findByIdAndUser_Id(requestId, me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (!OPEN.equals(mine.getRequestStatus())) {
            if (mine.getDelivery() != null) {
                throw new ConflictException("You already joined this delivery");
            }
            throw new ConflictException("This request is no longer open");
        }

        TransportationRequest other = transportationRequestRepository.findById(request.getWithRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (other.getUser().getId().equals(me.getId())) {
            throw new IllegalArgumentException("You cannot join your own request");
        }
        if (!OPEN.equals(other.getRequestStatus()) || other.getDelivery() != null) {
            throw new ConflictException("This request is no longer open");
        }

        Instant now = Instant.now();
        Delivery delivery = new Delivery();
        delivery.setDeliveryStatus("PENDING");
        delivery.setEstimatedDeliveryDate(laterDate(mine.getRequestedDateTime(), other.getRequestedDateTime()));
        delivery.setCreatedAt(now);
        delivery.setUpdatedAt(now);
        delivery = deliveryRepository.save(delivery);

        mine.setDelivery(delivery);
        mine.setRequestStatus(MATCHED);
        mine.setUpdatedAt(now);
        other.setDelivery(delivery);
        other.setRequestStatus(MATCHED);
        other.setUpdatedAt(now);
        transportationRequestRepository.save(mine);
        transportationRequestRepository.save(other);

        notifyMatched(mine, other, delivery);
        return JoinDeliveryResponseDto.builder()
                .requestId(mine.getId())
                .deliveryId(delivery.getId())
                .status(MATCHED)
                .estimatedSavingPercent(estimateSavingPercent(2))
                .build();
    }

    @Transactional(readOnly = true)
    public DeliveryStatusResponseDto getStatus(String email, Long deliveryId) {
        User user = requireUser(email);
        if (!transportationRequestRepository.existsByDelivery_IdAndUser_Id(deliveryId, user.getId())) {
            throw new ResourceNotFoundException("Delivery not found");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return toStatusResponse(delivery);
    }

    @Transactional
    public DeliveryStatusResponseDto updateStatus(String email, Long deliveryId, DeliveryStatusRequestDto request) {
        User user = requireRole(email, "TRANSPORT", "Only transport users can update deliveries");
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        String newStatus = normalizeStatus(request.getStatus());
        Instant now = Instant.now();
        delivery.setDeliveryStatus(newStatus);
        delivery.setUpdatedAt(now);
        if ("PICKED_UP".equals(newStatus)) {
            delivery.setAssignedDate(now);
        }
        if ("DELIVERED".equals(newStatus)) {
            delivery.setActualDeliveryDate(now);
        }
        deliveryRepository.save(delivery);

        List<TransportationRequest> requests = transportationRequestRepository.findByDelivery_Id(deliveryId);
        for (TransportationRequest item : requests) {
            item.setRequestStatus(newStatus);
            item.setUpdatedAt(now);
            transportationRequestRepository.save(item);
            notificationService.create(item.getUser().getId(),
                    "Delivery " + newStatus.toLowerCase(Locale.ROOT),
                    "Your delivery #" + deliveryId + " is now " + newStatus.toLowerCase(Locale.ROOT) + ".");
        }

        return toStatusResponse(delivery);
    }

    // Calculate the match score required by the shared-delivery contract.
    private DeliveryMatchResponseDto toMatch(TransportationRequest mine, TransportationRequest candidate) {
        double score = 0.0;
        if (sameText(mine.getDeliveryLocation(), candidate.getDeliveryLocation())) score += 0.5;
        if (sameText(mine.getPickupLocation(), candidate.getPickupLocation())) score += 0.3;
        if (sameDay(mine.getRequestedDateTime(), candidate.getRequestedDateTime())) score += 0.2;

        return DeliveryMatchResponseDto.builder()
                .requestId(candidate.getId())
                .userName(candidate.getUser().getName())
                .pickupLocation(candidate.getPickupLocation())
                .destination(candidate.getDeliveryLocation())
                .preferredDateTime(candidate.getRequestedDateTime())
                .matchScore(score)
                .estimatedSavingPercent(estimateSavingPercent(2))
                .build();
    }

    private void notifyMatched(TransportationRequest first, TransportationRequest second, Delivery delivery) {
        notificationService.create(first.getUser().getId(), "Delivery matched",
                "Your request #" + first.getId() + " was matched with request #" + second.getId()
                        + ". Shared delivery #" + delivery.getId() + " was created.");
        notificationService.create(second.getUser().getId(), "Delivery matched",
                "Your request #" + second.getId() + " was matched with request #" + first.getId()
                        + ". Shared delivery #" + delivery.getId() + " was created.");
    }

    // Keep the response contract small and independent from the entity.
    private DeliveryRequestResponseDto toRequestResponse(TransportationRequest request) {
        return DeliveryRequestResponseDto.builder()
                .requestId(request.getId())
                .status(request.getRequestStatus())
                .pickupLocation(request.getPickupLocation())
                .destination(request.getDeliveryLocation())
                .preferredDateTime(request.getRequestedDateTime())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private DeliveryStatusResponseDto toStatusResponse(Delivery delivery) {
        return DeliveryStatusResponseDto.builder()
                .deliveryId(delivery.getId())
                .status(delivery.getDeliveryStatus())
                .estimatedArrival(delivery.getEstimatedDeliveryDate())
                .lastUpdated(delivery.getUpdatedAt())
                .build();
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!DELIVERY_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid status. Allowed values: " + DELIVERY_STATUSES);
        }
        return normalized;
    }

    private int estimateSavingPercent(long requestCount) {
        return (int) Math.round(100.0 - (100.0 / requestCount));
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private boolean sameDay(Instant first, Instant second) {
        if (first == null || second == null) return false;
        LocalDate firstDate = first.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate secondDate = second.atZone(ZoneOffset.UTC).toLocalDate();
        return firstDate.equals(secondDate);
    }

    private Instant laterDate(Instant first, Instant second) {
        return first.isAfter(second) ? first : second;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private User requireRole(String email, String role, String deniedMessage) {
        User user = requireUser(email);
        if (!role.equals(user.getRole())) {
            throw new AccessDeniedException(deniedMessage);
        }
        return user;
    }
}
