package com.rukshan.ranaswanu.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

// Own page wrapper, so the JSON shape never depends on Spring's Page class
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int page;          // starts at 0
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageResponseDto<T> of(Page<T> p) {
        return new PageResponseDto<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
