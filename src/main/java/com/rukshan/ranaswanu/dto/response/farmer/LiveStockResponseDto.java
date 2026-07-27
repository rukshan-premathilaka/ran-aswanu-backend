package com.rukshan.ranaswanu.dto.response.farmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveStockResponseDto {
    private Long liveStockId;
    private String category;
    private String breed;
    private Long amount;
}