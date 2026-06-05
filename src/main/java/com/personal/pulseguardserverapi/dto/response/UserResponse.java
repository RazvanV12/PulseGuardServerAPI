package com.personal.pulseguardserverapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String email;
    private String name;
    private String sex;
    private Integer age;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
