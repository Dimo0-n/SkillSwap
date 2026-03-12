package com.example.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTimeZoneUpdateRequest {

    @NotBlank
    private String timeZoneId;
}
