package com.rukshan.ranaswanu.dto.response.delivery;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeliveryMatchesResponseDto {
    private Long requestId;
    private int totalMatches;
    private List<DeliveryMatchResponseDto> matches;
}
