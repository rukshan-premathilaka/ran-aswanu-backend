package com.rukshan.ranaswanu.dto.request.farmer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityStatusRequestDto {

    @NotNull(message = "done is required (true or false)")
    private Boolean done;
}
