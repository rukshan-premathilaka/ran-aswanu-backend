package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.FieldPlotRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.FieldPlotResponseDto;
import com.rukshan.ranaswanu.entities.Crop;
import com.rukshan.ranaswanu.entities.FieldPlot;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.CropRepository;
import com.rukshan.ranaswanu.repository.FieldPlotRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class FieldPlotService {

    @Autowired
    private FieldPlotRepository fieldPlotRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private UserRepository userRepository;

    public List<FieldPlotResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return fieldPlotRepository.findByUserId(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public FieldPlotResponseDto create(String farmerEmail, FieldPlotRequestDto requestData) {
        User farmer = requireUser(farmerEmail);
        Crop crop = cropRepository.findByIdAndUserId(requestData.getCropId(), farmer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Crop not found or not owned by this farmer"));

        FieldPlot plot = new FieldPlot();
        plot.setUser(farmer);
        plot.setCrop(crop);
        applyFields(plot, requestData);
        plot.setCreatedAt(Instant.now());
        plot.setUpdatedAt(Instant.now());

        fieldPlotRepository.save(plot);
        return toResponseDto(plot);
    }

    public FieldPlotResponseDto update(String farmerEmail, Long fieldPlotId, FieldPlotRequestDto requestData) {
        FieldPlot plot = findOwned(farmerEmail, fieldPlotId);
        applyFields(plot, requestData);
        plot.setUpdatedAt(Instant.now());
        fieldPlotRepository.save(plot);
        return toResponseDto(plot);
    }

    public void delete(String farmerEmail, Long fieldPlotId) {
        fieldPlotRepository.delete(findOwned(farmerEmail, fieldPlotId));
    }

    private void applyFields(FieldPlot plot, FieldPlotRequestDto requestData) {
        plot.setCurrentCrop(requestData.getCurrentCrop());
        plot.setCropVariety(requestData.getCropVariety());
        plot.setAreaUnit(requestData.getAreaUnit());
        plot.setGrowthStage(requestData.getGrowthStage());
        plot.setHealthCondition(requestData.getHealthCondition());
        plot.setFieldLogs(requestData.getFieldLogs());
        plot.setInspectionDate(requestData.getInspectionDate() != null ? requestData.getInspectionDate() : LocalDate.now());
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private FieldPlot findOwned(String farmerEmail, Long fieldPlotId) {
        User farmer = requireUser(farmerEmail);
        return fieldPlotRepository.findByIdAndUserId(fieldPlotId, farmer.getId())
                .orElseThrow(() -> new AccessDeniedException("Field plot not found or not owned by this farmer"));
    }

    private FieldPlotResponseDto toResponseDto(FieldPlot plot) {
        return FieldPlotResponseDto.builder()
                .fieldPlotId(plot.getId())
                .cropId(plot.getCrop().getId())
                .cropName(plot.getCrop().getCropName())
                .currentCrop(plot.getCurrentCrop())
                .cropVariety(plot.getCropVariety())
                .areaUnit(plot.getAreaUnit())
                .growthStage(plot.getGrowthStage())
                .healthCondition(plot.getHealthCondition())
                .fieldLogs(plot.getFieldLogs())
                .inspectionDate(plot.getInspectionDate())
                .createdAt(plot.getCreatedAt())
                .updatedAt(plot.getUpdatedAt())
                .build();
    }
}