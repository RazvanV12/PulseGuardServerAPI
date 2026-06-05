package com.personal.pulseguardserverapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummaryResponse {
    private long measurementCount;
    private Double avgHeartRate;
    private Double avgSpo2;
    private Integer minHeartRate;
    private Integer maxHeartRate;
    private Double minSpo2;
    private Double maxSpo2;
    private long alertCount;
    private LocalDateTime lastMeasuredAt;
}
