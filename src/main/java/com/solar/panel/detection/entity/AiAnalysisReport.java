package com.solar.panel.detection.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiAnalysisReport {
    private Long id;
    private Long recordId;
    private String analysisContent;
    private LocalDateTime createdAt;
}