package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.CropRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.CropResponseDto;
import com.rukshan.ranaswanu.entities.Crop;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.CropRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CropService {

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CropResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return cropRepository.findByUserId(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public CropResponseDto create(String farmerEmail, CropRequestDto requestData) {
        User farmer = requireUser(farmerEmail);

        Crop crop = new Crop();
        crop.setUser(farmer);
        applyFields(crop, requestData);
        crop.setCreatedAt(Instant.now());
        crop.setUpdatedAt(Instant.now());

        cropRepository.save(crop);
        return toResponseDto(crop);
    }

    public CropResponseDto update(String farmerEmail, Long cropId, CropRequestDto requestData) {
        Crop crop = findOwned(farmerEmail, cropId);
        applyFields(crop, requestData);
        crop.setUpdatedAt(Instant.now());
        cropRepository.save(crop);
        return toResponseDto(crop);
    }

    public CropResponseDto getById(String farmerEmail, Long cropId) {
        return toResponseDto(findOwned(farmerEmail, cropId));
    }

    public void delete(String farmerEmail, Long cropId) {
        cropRepository.delete(findOwned(farmerEmail, cropId));
    }

    private void applyFields(Crop crop, CropRequestDto requestData) {
        crop.setCropName(requestData.getCropName());
        crop.setCategory(requestData.getCategory());
        crop.setUnit(requestData.getUnit());
        crop.setHarvestQuantity(requestData.getHarvestQuantity());
        crop.setHarvestDate(requestData.getHarvestDate());
        crop.setNotes(requestData.getNotes());
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private Crop findOwned(String farmerEmail, Long cropId) {
        User farmer = requireUser(farmerEmail);
        return cropRepository.findByIdAndUserId(cropId, farmer.getId())
                .orElseThrow(() -> new AccessDeniedException("Crop not found or not owned by this farmer"));
    }

    private CropResponseDto toResponseDto(Crop crop) {
        return CropResponseDto.builder()
                .cropId(crop.getId())
                .cropName(crop.getCropName())
                .category(crop.getCategory())
                .unit(crop.getUnit())
                .harvestQuantity(crop.getHarvestQuantity())
                .harvestDate(crop.getHarvestDate())
                .notes(crop.getNotes())
                .createdAt(crop.getCreatedAt())
                .updatedAt(crop.getUpdatedAt())
                .build();
    }
}