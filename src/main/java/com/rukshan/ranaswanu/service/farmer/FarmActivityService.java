package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.FarmActivityRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.FarmActivityResponseDto;
import com.rukshan.ranaswanu.entities.FarmActivity;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.FarmActivityRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FarmActivityService {

    @Autowired
    private FarmActivityRepository farmActivityRepository;

    @Autowired
    private UserRepository userRepository;

    public List<FarmActivityResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return farmActivityRepository.findByUserIdOrderByCreatedAtDesc(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public FarmActivityResponseDto create(String farmerEmail, FarmActivityRequestDto requestData) {
        User farmer = requireUser(farmerEmail);

        FarmActivity activity = new FarmActivity();
        activity.setUser(farmer);
        activity.setActivity(requestData.getActivity());
        activity.setActivityStatus(false);
        activity.setCreatedAt(Instant.now());
        activity.setUpdatedAt(Instant.now());

        farmActivityRepository.save(activity);
        return toResponseDto(activity);
    }

    public FarmActivityResponseDto updateStatus(String farmerEmail, Long activityId, boolean done) {
        FarmActivity activity = findOwned(farmerEmail, activityId);
        activity.setActivityStatus(done);
        activity.setUpdatedAt(Instant.now());
        farmActivityRepository.save(activity);
        return toResponseDto(activity);
    }

    public void delete(String farmerEmail, Long activityId) {
        farmActivityRepository.delete(findOwned(farmerEmail, activityId));
    }

    private FarmActivity findOwned(String farmerEmail, Long activityId) {
        User farmer = requireUser(farmerEmail);
        return farmActivityRepository.findByIdAndUserId(activityId, farmer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private FarmActivityResponseDto toResponseDto(FarmActivity activity) {
        return FarmActivityResponseDto.builder()
                .activityId(activity.getId())
                .activity(activity.getActivity())
                .activityStatus(activity.getActivityStatus())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
