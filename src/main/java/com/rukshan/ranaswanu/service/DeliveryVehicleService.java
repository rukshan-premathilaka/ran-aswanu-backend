package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.delivery.DeliveryVehicleRequestDto;
import com.rukshan.ranaswanu.dto.response.delivery.DeliveryVehicleResponseDto;
import com.rukshan.ranaswanu.entities.DeliveryVehicle;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ConflictException;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.DeliveryVehicleRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class DeliveryVehicleService {

    private final DeliveryVehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public DeliveryVehicleService(DeliveryVehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeliveryVehicleResponseDto create(String email, DeliveryVehicleRequestDto request) {
        User user = requireTransport(email);
        String registrationNumber = clean(request.getRegistrationNumber()).toUpperCase(Locale.ROOT);
        ensureRegistrationAvailable(registrationNumber, null);

        Instant now = Instant.now();
        DeliveryVehicle vehicle = new DeliveryVehicle();
        vehicle.setUser(user);
        vehicle.setVehicleName(clean(request.getVehicleName()));
        vehicle.setVehicleType(clean(request.getVehicleType()));
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setActive(request.getActive() == null || request.getActive());
        vehicle.setCreatedAt(now);
        vehicle.setUpdatedAt(now);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public List<DeliveryVehicleResponseDto> listMine(String email) {
        User user = requireTransport(email);
        return vehicleRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryVehicleResponseDto update(String email, Long vehicleId, DeliveryVehicleRequestDto request) {
        User user = requireTransport(email);
        DeliveryVehicle vehicle = vehicleRepository.findByIdAndUser_Id(vehicleId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        String registrationNumber = clean(request.getRegistrationNumber()).toUpperCase(Locale.ROOT);
        ensureRegistrationAvailable(registrationNumber, vehicleId);

        vehicle.setVehicleName(clean(request.getVehicleName()));
        vehicle.setVehicleType(clean(request.getVehicleType()));
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setCapacityKg(request.getCapacityKg());
        if (request.getActive() != null) {
            vehicle.setActive(request.getActive());
        }
        vehicle.setUpdatedAt(Instant.now());
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(String email, Long vehicleId) {
        User user = requireTransport(email);
        DeliveryVehicle vehicle = vehicleRepository.findByIdAndUser_Id(vehicleId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public DeliveryVehicle requireOwnedActive(String email, Long vehicleId) {
        User user = requireTransport(email);
        DeliveryVehicle vehicle = vehicleRepository.findByIdAndUser_Id(vehicleId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!Boolean.TRUE.equals(vehicle.getActive())) {
            throw new ConflictException("This vehicle is inactive");
        }
        return vehicle;
    }

    private void ensureRegistrationAvailable(String registrationNumber, Long currentId) {
        vehicleRepository.findByRegistrationNumberIgnoreCase(registrationNumber)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Registration number is already registered");
                });
    }

    private User requireTransport(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (!user.hasRole("TRANSPORT")) {
            throw new AccessDeniedException("Only transport users can manage vehicles");
        }
        return user;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private DeliveryVehicleResponseDto toResponse(DeliveryVehicle vehicle) {
        return DeliveryVehicleResponseDto.builder()
                .vehicleId(vehicle.getId())
                .vehicleName(vehicle.getVehicleName())
                .vehicleType(vehicle.getVehicleType())
                .registrationNumber(vehicle.getRegistrationNumber())
                .capacityKg(vehicle.getCapacityKg())
                .active(Boolean.TRUE.equals(vehicle.getActive()))
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
