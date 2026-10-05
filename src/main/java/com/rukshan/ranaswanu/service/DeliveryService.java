package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.delivery.AssignVehicleRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.DeliveryStatusRequestDto;
import com.rukshan.ranaswanu.dto.request.delivery.JoinDeliveryRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.AcceptDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryMatchesResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryRequestResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryStatusResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.JoinDeliveryResponseDto;
import com.rukshan.ranaswanu.dto.response.delivery.OpenDeliveryRequestDto;
import com.rukshan.ranaswanu.entities.Delivery;
import com.rukshan.ranaswanu.entities.DeliveryVehicle;
import com.rukshan.ranaswanu.entities.Order;
import com.rukshan.ranaswanu.entities.OrderItem;
import com.rukshan.ranaswanu.entities.TransportationRequest;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ConflictException;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.DeliveryRepository;
import com.rukshan.ranaswanu.repository.OrderRepository;
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
    private static final String CUSTOMER_REQUEST = "CUSTOMER_REQUEST";
    private static final String FARMER_REQUEST = "FARMER_REQUEST"; // legacy request type
    private static final String VEHICLE_OFFER = "VEHICLE_OFFER"; // legacy request type
    private static final Set<String> SHAREABLE_REQUEST_TYPES = Set.of(CUSTOMER_REQUEST, FARMER_REQUEST);
    private static final List<String> DELIVERY_STATUSES =
            List.of("PENDING", "PICKED_UP", "IN_TRANSIT", "DELIVERED", "CANCELLED");
    private static final List<String> ACTIVE_DELIVERY_STATUSES =
            List.of("PENDING", "PICKED_UP", "IN_TRANSIT");

    private static final Map<String, Set<String>> ALLOWED_STATUS_CHANGES = Map.of(
            "PENDING", Set.of("PICKED_UP", "CANCELLED"),
            "PICKED_UP", Set.of("IN_TRANSIT", "CANCELLED"),
            "IN_TRANSIT", Set.of("DELIVERED", "CANCELLED")
    );

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private DeliveryVehicleService deliveryVehicleService;
    @Autowired private TransportationRequestRepository transportationRequestRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public DeliveryRequestResponseDto createRequest(String email, DeliveryRequestDto request) {
        User user = requireUser(email);
        Instant now = Instant.now();

        String requestType = request.getRequestType();
        if (requestType == null || requestType.isBlank()) {
            requestType = request.getOrderId() != null
                    ? CUSTOMER_REQUEST
                    : user.hasRole("TRANSPORT") ? VEHICLE_OFFER : CUSTOMER_REQUEST;
        }

        if (CUSTOMER_REQUEST.equals(requestType) && !isCustomerRole(user)) {
            throw new AccessDeniedException("Only buyers or farmers can create delivery requests");
        }
        if (FARMER_REQUEST.equals(requestType) && !user.hasRole("FARMER")) {
            throw new AccessDeniedException("Only farmers can create farmer requests");
        }
        if (VEHICLE_OFFER.equals(requestType) && !user.hasRole("TRANSPORT")) {
            throw new AccessDeniedException("Only transport users can offer vehicles");
        }

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

        if (request.getOrderId() != null) {
            if (!CUSTOMER_REQUEST.equals(requestType)) {
                throw new IllegalArgumentException("An order can only be attached to a customer delivery request");
            }
            Order order = requireOrderForDelivery(user, request.getOrderId());
            if (order.getDelivery() != null) {
                throw new ConflictException("This order already has a delivery");
            }
            entity.setOrder(order);
        }

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
        if (!isCustomerRole(user)) {
            throw new AccessDeniedException("Only buyers or farmers can find shared delivery matches");
        }

        TransportationRequest mine = transportationRequestRepository.findByIdAndUser_Id(requestId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        List<TransportationRequest> candidates = SHAREABLE_REQUEST_TYPES.contains(mine.getRequestType())
                ? transportationRequestRepository
                        .findByUser_IdNotAndRequestStatusAndDeliveryIsNull(user.getId(), OPEN)
                        .stream()
                        .filter(candidate -> SHAREABLE_REQUEST_TYPES.contains(candidate.getRequestType()))
                        .filter(candidate -> calculateMatchScore(mine, candidate) >= 0.5)
                        .toList()
                : List.of();

        List<DeliveryMatchResponseDto> matches = candidates
                .stream()
                .map(candidate -> toMatch(mine, candidate))
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
        if (!isCustomerRole(me)) {
            throw new AccessDeniedException("Only buyers or farmers can join shared deliveries");
        }

        Long otherRequestId = request.getWithRequestId();
        if (requestId.equals(otherRequestId)) {
            throw new IllegalArgumentException("You cannot join your own request");
        }

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
        if (!SHAREABLE_REQUEST_TYPES.contains(mine.getRequestType())
                || !SHAREABLE_REQUEST_TYPES.contains(other.getRequestType())) {
            throw new IllegalArgumentException("Only buyer/farmer delivery requests can be shared");
        }
        if (calculateMatchScore(mine, other) < 0.5) {
            throw new IllegalArgumentException("These delivery requests do not meet the matching requirements");
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

        attachOrderToDelivery(mine, delivery);
        attachOrderToDelivery(other, delivery);

        notifyMatched(mine, other, delivery);
        return JoinDeliveryResponseDto.builder()
                .requestId(mine.getId())
                .deliveryId(delivery.getId())
                .status(MATCHED)
                .estimatedSavingPercent(estimateSavingPercent(2))
                .build();
    }

    @Transactional(readOnly = true)
    public List<OpenDeliveryRequestDto> listOpen(String email, String type) {
        User me = requireUser(email);
        String normalized = type == null ? CUSTOMER_REQUEST : type.trim().toUpperCase(Locale.ROOT);

        if (CUSTOMER_REQUEST.equals(normalized)) {
            if (!me.hasRole("TRANSPORT")) {
                throw new AccessDeniedException("Only transport users can view delivery requests to accept");
            }
            return transportationRequestRepository
                    .findTransportBoardRequests(Set.of(CUSTOMER_REQUEST, FARMER_REQUEST), me.getId())
                    .stream()
                    .map(this::toOpenResponse)
                    .toList();
        }

        if (FARMER_REQUEST.equals(normalized)) {
            if (!me.hasRole("TRANSPORT")) {
                throw new AccessDeniedException("Only transport users can view farmer requests");
            }
            return transportationRequestRepository
                    .findTransportBoardRequests(Set.of(FARMER_REQUEST), me.getId())
                    .stream()
                    .map(this::toOpenResponse)
                    .toList();
        }

        if (VEHICLE_OFFER.equals(normalized)) {
            if (!isCustomerRole(me)) {
                throw new AccessDeniedException("Only buyers or farmers can view legacy vehicle offers");
            }
            return transportationRequestRepository
                    .findByRequestTypeAndRequestStatusAndDeliveryIsNullAndUser_IdNotOrderByRequestedDateTimeAsc(
                            VEHICLE_OFFER, OPEN, me.getId())
                    .stream()
                    .map(this::toOpenResponse)
                    .toList();
        }

        throw new IllegalArgumentException("Type must be CUSTOMER_REQUEST, FARMER_REQUEST or VEHICLE_OFFER");
    }

    @Transactional
    public AcceptDeliveryResponseDto accept(String email, Long requestId, AssignVehicleRequestDto assignment) {
        User me = requireUser(email);
        TransportationRequest target = transportationRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (target.getUser().getId().equals(me.getId())) {
            throw new IllegalArgumentException("You cannot accept your own request");
        }

        if (SHAREABLE_REQUEST_TYPES.contains(target.getRequestType())) {
            if (!me.hasRole("TRANSPORT")) {
                throw new AccessDeniedException("Only transport users can accept delivery requests");
            }
            if (assignment == null || assignment.getVehicleId() == null) {
                throw new IllegalArgumentException("Select a vehicle before accepting a delivery");
            }

            DeliveryVehicle vehicle = deliveryVehicleService.requireOwnedActive(email, assignment.getVehicleId());
            if (deliveryRepository.existsByVehicle_IdAndDeliveryStatusIn(vehicle.getId(), ACTIVE_DELIVERY_STATUSES)) {
                throw new ConflictException("This vehicle is already assigned to an active delivery");
            }

            Instant now = Instant.now();
            Delivery delivery;
            if (target.getDelivery() != null) {
                if (!MATCHED.equals(target.getRequestStatus())) {
                    throw new ConflictException("This delivery request is no longer available");
                }
                delivery = deliveryRepository.findById(target.getDelivery().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
                if (delivery.getVehicle() != null) {
                    throw new ConflictException("A vehicle is already assigned to this delivery");
                }
            } else {
                if (!OPEN.equals(target.getRequestStatus())) {
                    throw new ConflictException("This request is no longer open");
                }
                delivery = new Delivery();
                delivery.setDeliveryStatus("PENDING");
                delivery.setEstimatedDeliveryDate(target.getRequestedDateTime());
                delivery.setCreatedAt(now);
            }

            long totalWeight = target.getEstimatedWeight() == null ? 0L : target.getEstimatedWeight();
            if (target.getDelivery() != null) {
                totalWeight = transportationRequestRepository.findByDelivery_Id(delivery.getId()).stream()
                        .filter(r -> SHAREABLE_REQUEST_TYPES.contains(r.getRequestType()))
                        .map(TransportationRequest::getEstimatedWeight)
                        .filter(java.util.Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .sum();
            }
            if (vehicle.getCapacityKg() < totalWeight) {
                throw new ConflictException("This vehicle cannot carry the combined weight of this delivery");
            }

            delivery.setVehicle(vehicle);
            delivery.setAssignedDate(now);
            delivery.setUpdatedAt(now);
            delivery = deliveryRepository.save(delivery);

            if (target.getDelivery() == null) {
                target.setDelivery(delivery);
                target.setRequestStatus(MATCHED);
                target.setUpdatedAt(now);
                transportationRequestRepository.save(target);
                attachOrderToDelivery(target, delivery);
            }

            ensureTransportParticipant(target, me, delivery, now);
            notifyDeliveryAssigned(delivery, me);

            return AcceptDeliveryResponseDto.builder()
                    .requestId(target.getId())
                    .deliveryId(delivery.getId())
                    .status(MATCHED)
                    .build();
        }

        // Legacy vehicle-offer flow remains available so existing clients keep working.
        if (VEHICLE_OFFER.equals(target.getRequestType())) {
            if (!me.hasRole("FARMER")) {
                throw new AccessDeniedException("Only farmers can choose a legacy vehicle offer");
            }
            if (!OPEN.equals(target.getRequestStatus()) || target.getDelivery() != null) {
                throw new ConflictException("This request is no longer open");
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

            ensureTransportParticipant(target, me, delivery, now, FARMER_REQUEST);
            notifyMatched(target, findTransportParticipant(delivery, me.getId()), delivery);
            return AcceptDeliveryResponseDto.builder()
                    .requestId(target.getId())
                    .deliveryId(delivery.getId())
                    .status(MATCHED)
                    .build();
        }

        throw new IllegalArgumentException("Unsupported delivery request type");
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
        if (!transportationRequestRepository.existsByDelivery_IdAndUser_Id(deliveryId, user.getId())) {
            throw new ResourceNotFoundException("Delivery not found");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        String newStatus = normalizeStatus(request.getStatus());
        String current = delivery.getDeliveryStatus();

        if (!ALLOWED_STATUS_CHANGES.getOrDefault(current, Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException("Cannot change status from " + current + " to " + newStatus);
        }

        Instant now = Instant.now();
        delivery.setDeliveryStatus(newStatus);
        delivery.setUpdatedAt(now);
        if ("PICKED_UP".equals(newStatus) && delivery.getAssignedDate() == null) {
            delivery.setAssignedDate(now);
        }
        if ("DELIVERED".equals(newStatus)) {
            delivery.setActualDeliveryDate(now);
        }
        deliveryRepository.save(delivery);

        for (TransportationRequest item : transportationRequestRepository.findByDelivery_Id(deliveryId)) {
            item.setRequestStatus(newStatus);
            item.setUpdatedAt(now);
            transportationRequestRepository.save(item);
            if (!item.getUser().getId().equals(user.getId())) {
                notificationService.create(item.getUser().getId(),
                        "Delivery " + newStatus.toLowerCase(Locale.ROOT),
                        "Your delivery #" + deliveryId + " is now " + newStatus.toLowerCase(Locale.ROOT) + ".");
            }
        }

        return toStatusResponse(delivery, user.getId());
    }

    private void ensureTransportParticipant(TransportationRequest target,
                                            User transport,
                                            Delivery delivery,
                                            Instant now) {
        ensureTransportParticipant(target, transport, delivery, now, VEHICLE_OFFER);
    }

    private void ensureTransportParticipant(TransportationRequest target,
                                            User transport,
                                            Delivery delivery,
                                            Instant now,
                                            String mirrorType) {
        if (transportationRequestRepository.findFirstByDelivery_IdAndUser_Id(delivery.getId(), transport.getId()).isPresent()) {
            return;
        }

        TransportationRequest mirror = new TransportationRequest();
        mirror.setUser(transport);
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
        mirror.setOrder(target.getOrder());
        mirror.setCreatedAt(now);
        mirror.setUpdatedAt(now);
        transportationRequestRepository.save(mirror);
    }

    private TransportationRequest findTransportParticipant(Delivery delivery, Long excludedUserId) {
        return transportationRequestRepository.findByDelivery_Id(delivery.getId()).stream()
                .filter(r -> !r.getUser().getId().equals(excludedUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Delivery participant not found"));
    }

    private DeliveryMatchResponseDto toMatch(TransportationRequest mine, TransportationRequest candidate) {
        return DeliveryMatchResponseDto.builder()
                .requestId(candidate.getId())
                .orderId(candidate.getOrder() == null ? null : candidate.getOrder().getId())
                .deliveryId(candidate.getDelivery() == null ? null : candidate.getDelivery().getId())
                .userName(candidate.getUser().getName())
                .pickupLocation(candidate.getPickupLocation())
                .destination(candidate.getDeliveryLocation())
                .preferredDateTime(candidate.getRequestedDateTime())
                .matchScore(calculateMatchScore(mine, candidate))
                .estimatedSavingPercent(estimateSavingPercent(2))
                .build();
    }

    private OpenDeliveryRequestDto toOpenResponse(TransportationRequest r) {
        return OpenDeliveryRequestDto.builder()
                .requestId(r.getId())
                .orderId(r.getOrder() == null ? null : r.getOrder().getId())
                .deliveryId(r.getDelivery() == null ? null : r.getDelivery().getId())
                .requestType(r.getRequestType())
                .requestStatus(r.getRequestStatus())
                .description(r.getDescription())
                .vehicleType(r.getVehicleType())
                .estimatedWeight(r.getEstimatedWeight())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .pickupLocation(r.getPickupLocation())
                .destination(r.getDeliveryLocation())
                .preferredDateTime(r.getRequestedDateTime())
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

    private void notifyDeliveryAssigned(Delivery delivery, User transport) {
        for (TransportationRequest participant : transportationRequestRepository.findByDelivery_Id(delivery.getId())) {
            if (!participant.getUser().getId().equals(transport.getId())) {
                notificationService.create(participant.getUser().getId(), "Delivery assigned",
                        transport.getName() + " assigned vehicle " + delivery.getVehicle().getRegistrationNumber()
                                + " to delivery #" + delivery.getId() + ".");
            }
        }
    }

    private DeliveryRequestResponseDto toRequestResponse(TransportationRequest request) {
        return DeliveryRequestResponseDto.builder()
                .requestId(request.getId())
                .orderId(request.getOrder() == null ? null : request.getOrder().getId())
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

    private DeliveryStatusResponseDto toStatusResponse(Delivery delivery, Long viewerId) {
        List<TransportationRequest> requests = transportationRequestRepository.findByDelivery_Id(delivery.getId());
        TransportationRequest mine = requests.stream()
                .filter(r -> r.getUser().getId().equals(viewerId))
                .findFirst()
                .orElse(null);
        String partnerName = requests.stream()
                .filter(r -> !r.getUser().getId().equals(viewerId))
                .map(r -> r.getUser().getName())
                .distinct()
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
                .vehicleId(delivery.getVehicle() == null ? null : delivery.getVehicle().getId())
                .vehicleName(delivery.getVehicle() == null ? null : delivery.getVehicle().getVehicleName())
                .registrationNumber(delivery.getVehicle() == null ? null : delivery.getVehicle().getRegistrationNumber())
                .vehicleCapacityKg(delivery.getVehicle() == null ? null : delivery.getVehicle().getCapacityKg())
                .build();
    }

    private Order requireOrderForDelivery(User user, Long orderId) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean purchaser = order.getUser().getId().equals(user.getId());
        boolean seller = order.getOrderItems().stream()
                .map(OrderItem::getList)
                .anyMatch(listing -> listing.getUser().getId().equals(user.getId()));

        if (!purchaser && !seller) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order;
    }

    private void attachOrderToDelivery(TransportationRequest request, Delivery delivery) {
        Order order = request.getOrder();
        if (order == null) return;
        if (order.getDelivery() != null && !order.getDelivery().getId().equals(delivery.getId())) {
            throw new ConflictException("Order #" + order.getId() + " is already assigned to another delivery");
        }
        order.setDelivery(delivery);
        orderRepository.save(order);
    }

    private boolean isCustomerRole(User user) {
        // Delivery partners retain buyer capability, so TRANSPORT users are valid customer requesters too.
        return user.hasRole("BUYER") || user.hasRole("FARMER") || user.hasRole("TRANSPORT");
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
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private boolean sameDay(Instant first, Instant second) {
        if (first == null || second == null) return false;
        LocalDate firstDate = first.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate secondDate = second.atZone(ZoneOffset.UTC).toLocalDate();
        return firstDate.equals(secondDate);
    }

    private Instant laterDate(Instant first, Instant second) {
        if (first == null) return second;
        if (second == null) return first;
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
        if (!user.hasRole(role)) {
            throw new AccessDeniedException(deniedMessage);
        }
        return user;
    }
}
