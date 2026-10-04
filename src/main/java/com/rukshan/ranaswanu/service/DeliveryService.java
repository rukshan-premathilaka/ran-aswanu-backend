package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.delivery.DeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.JoinDeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryStatusRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.AcceptDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchesResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryRequestResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryStatusResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.JoinDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.OpenDeliveryRequestDto;
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
import java.util.Map;
import java.util.Set;

@Service
public class DeliveryService {

    private static final String OPEN = "OPEN";
    private static final String MATCHED = "MATCHED";
    private static final String FARMER_REQUEST = "FARMER_REQUEST";
    private static final String VEHICLE_OFFER = "VEHICLE_OFFER";
    private static final List<String> DELIVERY_STATUSES =
            List.of("PENDING", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "CANCELLED");

    // Allowed delivery status moves. DELIVERED and CANCELLED are final.
    private static final Map<String, Set<String>> ALLOWED_STATUS_CHANGES = Map.of(
            "PENDING", Set.of("PICKED_UP", "CANCELLED"),
            "PICKED_UP", Set.of("IN_TRANSIT", "CANCELLED"),
            "IN_TRANSIT", Set.of("DELIVERED", "CANCELLED")
    );

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private TransportationRequestRepository transportationRequestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public DeliveryRequestResponseDto createRequest(String email, DeliveryRequestDto request) {
        User user = requireUser(email);
        Instant now = Instant.now();

        // Only farmers ask for a vehicle; only transport users offer one.
        // If the type is missing, it is chosen from the role (keeps old clients working).
        String requestType = request.getRequestType();
        if (requestType == null || requestType.isBlank()) {
            requestType = "TRANSPORT".equals(user.getRole()) ? VEHICLE_OFFER : FARMER_REQUEST;
        }
        if (FARMER_REQUEST.equals(requestType) && !"FARMER".equals(user.getRole())) {
            throw new AccessDeniedException("Only farmers can create requests");
        }
        if (VEHICLE_OFFER.equals(requestType) && !"TRANSPORT".equals(user.getRole())) {
            throw new AccessDeniedException("Only transport users can offer vehicles");
        }

        // Save the request without a delivery so it can be matched later.
        TransportationRequest entity = new TransportationRequest();
        entity.setUser(user);
        entity.setPickupLocation(request.getPickupLocation().trim());
        entity.setDeliveryLocation(request.getDestination().trim());
        entity.setRequestedDateTime(request.getPreferredDateTime());
        entity.setVehicleType(request.getVehicleType().trim());
        entity.setEstimatedWeight(request.getEstimatedWeight());
        entity.setSize(request.getSize() == null || request.getSize().isBlank() ? "N/A" : request.getSize().trim());
        entity.setRequestType(requestType);
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

        // Route sharing is only for farmer requests; vehicle offers are never route matches.
        List<TransportationRequest> candidates = FARMER_REQUEST.equals(mine.getRequestType())
                ? transportationRequestRepository
                        .findByUser_IdNotAndRequestStatusAndDeliveryIsNull(user.getId(), OPEN)
                        .stream()
                        .filter(candidate -> FARMER_REQUEST.equals(candidate.getRequestType()))
                        .toList()
                : List.of();

        List<DeliveryMatchResponseDto> matches = candidates
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
        Long otherRequestId = request.getWithRequestId();
        TransportationRequest first = transportationRequestRepository.findByIdForUpdate(Math.min(requestId, otherRequestId))
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));
        TransportationRequest second = transportationRequestRepository.findByIdForUpdate(Math.max(requestId, otherRequestId))
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));
        TransportationRequest mine = requestId.equals(first.getId()) ? first : second;
        TransportationRequest other = requestId.equals(first.getId()) ? second : first;

        if (!mine.getUser().getId().equals(me.getId())) {
            throw new ResourceNotFoundException("Delivery request not found");
        }
        if (!OPEN.equals(mine.getRequestStatus())) {
            if (mine.getDelivery() != null) {
                throw new ConflictException("You already joined this delivery");
            }
            throw new ConflictException("This request is no longer open");
        }

        if (other.getUser().getId().equals(me.getId())) {
            throw new IllegalArgumentException("You cannot join your own request");
        }
        if (!OPEN.equals(other.getRequestStatus()) || other.getDelivery() != null) {
            throw new ConflictException("This request is no longer open");
        }

        if (!FARMER_REQUEST.equals(mine.getRequestType()) || !FARMER_REQUEST.equals(other.getRequestType())) {
            throw new IllegalArgumentException("Only farmer requests can be shared");
        }

        if (calculateMatchScore(mine, other) < 0.5) {
            throw new IllegalArgumentException("These delivery requests do not meet the matching requirements.");
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

    // Delivery board: other users' open items of one type
    @Transactional(readOnly = true)
    public List<OpenDeliveryRequestDto> listOpen(String email, String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!FARMER_REQUEST.equals(normalized) && !VEHICLE_OFFER.equals(normalized)) {
            throw new IllegalArgumentException("Type must be FARMER_REQUEST or VEHICLE_OFFER");
        }
        User me = requireUser(email);

        return transportationRequestRepository
                .findByRequestTypeAndRequestStatusAndDeliveryIsNullAndUser_IdNotOrderByRequestedDateTimeAsc(
                        normalized, OPEN, me.getId())
                .stream()
                .map(r -> OpenDeliveryRequestDto.builder()
                        .requestId(r.getId())
                        .requestType(r.getRequestType())
                        .description(r.getDescription())
                        .vehicleType(r.getVehicleType())
                        .estimatedWeight(r.getEstimatedWeight())
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getName())
                        .pickupLocation(r.getPickupLocation())
                        .destination(r.getDeliveryLocation())
                        .preferredDateTime(r.getRequestedDateTime())
                        .build())
                .toList();
    }

    // Delivery board: a transport user accepts a farmer request, or a farmer chooses a vehicle offer
    @Transactional
    public AcceptDeliveryResponseDto accept(String email, Long requestId) {
        User me = requireUser(email);

        // Lock the row so two users cannot accept the same item at the same time
        TransportationRequest target = transportationRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (target.getUser().getId().equals(me.getId())) {
            throw new IllegalArgumentException("You cannot accept your own request");
        }
        if (!OPEN.equals(target.getRequestStatus()) || target.getDelivery() != null) {
            throw new ConflictException("This request is no longer open");
        }

        String mirrorType;
        if (FARMER_REQUEST.equals(target.getRequestType())) {
            if (!"TRANSPORT".equals(me.getRole())) {
                throw new AccessDeniedException("Only transport users can accept farmer requests");
            }
            mirrorType = VEHICLE_OFFER;
        } else {
            if (!"FARMER".equals(me.getRole())) {
                throw new AccessDeniedException("Only farmers can choose a vehicle");
            }
            mirrorType = FARMER_REQUEST;
        }

        Instant now = Instant.now();
        Delivery delivery = new Delivery();
        delivery.setDeliveryStatus("PENDING");
        delivery.setEstimatedDeliveryDate(target.getRequestedDateTime());
        delivery.setCreatedAt(now);
        delivery.setUpdatedAt(now);
        delivery = deliveryRepository.save(delivery);

        target.setDelivery(delivery);
        target.setRequestStatus(MATCHED);
        target.setUpdatedAt(now);
        transportationRequestRepository.save(target);

        // A copy of the item for the accepting user, so both users are "in" the delivery.
        // This keeps the status permission check and notifications working with no schema change.
        TransportationRequest mirror = new TransportationRequest();
        mirror.setUser(me);
        mirror.setPickupLocation(target.getPickupLocation());
        mirror.setDeliveryLocation(target.getDeliveryLocation());
        mirror.setRequestedDateTime(target.getRequestedDateTime());
        mirror.setVehicleType(target.getVehicleType());
        mirror.setEstimatedWeight(target.getEstimatedWeight());
        mirror.setSize(target.getSize());
        mirror.setDescription(target.getDescription());
        mirror.setSpecialInstructions(target.getSpecialInstructions());
        mirror.setRequestType(mirrorType);
        mirror.setRequestStatus(MATCHED);
        mirror.setDelivery(delivery);
        mirror.setCreatedAt(now);
        mirror.setUpdatedAt(now);
        transportationRequestRepository.save(mirror);

        notifyMatched(target, mirror, delivery);

        return AcceptDeliveryResponseDto.builder()
                .requestId(target.getId())
                .deliveryId(delivery.getId())
                .status(MATCHED)
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
        return toStatusResponse(delivery, user.getId());
    }

    @Transactional
    public DeliveryStatusResponseDto updateStatus(String email, Long deliveryId, DeliveryStatusRequestDto request) {
        User user = requireRole(email, "TRANSPORT", "Only transport users can update deliveries");

        // Only a user who is part of this delivery may change it (others get 404)
        if (!transportationRequestRepository.existsByDelivery_IdAndUser_Id(deliveryId, user.getId())) {
            throw new ResourceNotFoundException("Delivery not found");
        }
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        String newStatus = normalizeStatus(request.getStatus());
        String current = delivery.getDeliveryStatus();
        if ("DELIVERED".equals(current) || "CANCELLED".equals(current)) {
            throw new IllegalArgumentException("This delivery is already finished");
        }
        if (!ALLOWED_STATUS_CHANGES.getOrDefault(current, Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException("Cannot change status from " + current + " to " + newStatus);
        }
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

        return toStatusResponse(delivery, user.getId());
    }

    // Calculate the match score required by the shared-delivery contract.
    private DeliveryMatchResponseDto toMatch(TransportationRequest mine, TransportationRequest candidate) {
        double score = calculateMatchScore(mine, candidate);

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
                .requestType(request.getRequestType())
                .vehicleType(request.getVehicleType())
                .estimatedWeight(request.getEstimatedWeight())
                .description(request.getDescription())
                .deliveryId(request.getDelivery() != null ? request.getDelivery().getId() : null)
                .build();
    }

    // Route, vehicle and weight come from the viewer's own request in this delivery;
    // partnerName is the other user in the same delivery.
    private DeliveryStatusResponseDto toStatusResponse(Delivery delivery, Long viewerId) {
        List<TransportationRequest> requests = transportationRequestRepository.findByDelivery_Id(delivery.getId());
        TransportationRequest mine = requests.stream()
                .filter(r -> r.getUser().getId().equals(viewerId))
                .findFirst()
                .orElse(null);
        String partnerName = requests.stream()
                .filter(r -> !r.getUser().getId().equals(viewerId))
                .map(r -> r.getUser().getName())
                .findFirst()
                .orElse(null);

        return DeliveryStatusResponseDto.builder()
                .deliveryId(delivery.getId())
                .status(delivery.getDeliveryStatus())
                .estimatedArrival(delivery.getEstimatedDeliveryDate())
                .lastUpdated(delivery.getUpdatedAt())
                .pickupLocation(mine == null ? null : mine.getPickupLocation())
                .destination(mine == null ? null : mine.getDeliveryLocation())
                .preferredDateTime(mine == null ? null : mine.getRequestedDateTime())
                .vehicleType(mine == null ? null : mine.getVehicleType())
                .estimatedWeight(mine == null ? null : mine.getEstimatedWeight())
                .partnerName(partnerName)
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

    private double calculateMatchScore(TransportationRequest first, TransportationRequest second) {
        double score = 0.0;
        if (sameText(first.getDeliveryLocation(), second.getDeliveryLocation())) score += 0.5;
        if (sameText(first.getPickupLocation(), second.getPickupLocation())) score += 0.3;
        if (sameDay(first.getRequestedDateTime(), second.getRequestedDateTime())) score += 0.2;
        return score;
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
