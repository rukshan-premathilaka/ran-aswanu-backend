package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.LiveStockRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.LiveStockResponseDto;
import com.rukshan.ranaswanu.entities.LiveStock;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.LiveStockRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LiveStockService {

    @Autowired
    private LiveStockRepository liveStockRepository;

    @Autowired
    private UserRepository userRepository;

    public List<LiveStockResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return liveStockRepository.findByUserId(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public LiveStockResponseDto create(String farmerEmail, LiveStockRequestDto requestData) {
        User farmer = requireUser(farmerEmail);

        LiveStock stock = new LiveStock();
        stock.setUser(farmer);
        stock.setCategory(requestData.getCategory());
        stock.setBreed(requestData.getBreed());
        stock.setAmount(requestData.getAmount());
        stock.setCreatedAt(Instant.now());
        stock.setUpdatedAt(Instant.now());

        liveStockRepository.save(stock);
        return toResponseDto(stock);
    }

    public void delete(String farmerEmail, Long liveStockId) {
        User farmer = requireUser(farmerEmail);
        LiveStock stock = liveStockRepository.findByIdAndUserId(liveStockId, farmer.getId())
                .orElseThrow(() -> new AccessDeniedException("Livestock record not found or not owned by this farmer"));
        liveStockRepository.delete(stock);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private LiveStockResponseDto toResponseDto(LiveStock stock) {
        return LiveStockResponseDto.builder()
                .liveStockId(stock.getId())
                .category(stock.getCategory())
                .breed(stock.getBreed())
                .amount(stock.getAmount())
                .build();
    }
}