package com.personal.pulseguardserverapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMeasurementResponse {
    private int savedCount;
    private int alertCount;
    private String message;
}
