package com.solar.panel.detection.entity;

import lombok.Data;

@Data
public class DefectDetail {
    private Long id;
    private Long recordId;
    private String defectType;
    private Float confidence;
    private Integer x;
    private Integer y;
    private Integer w;
    private Integer h;
}