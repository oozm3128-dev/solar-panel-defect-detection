package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.config.JwtUtils;
import com.solar.panel.detection.entity.DetectionRecord;
import com.solar.panel.detection.entity.DefectDetail;
import com.solar.panel.detection.entity.AiAnalysisReport;
import com.solar.panel.detection.service.DetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detection")
public class DetectionController {

    @Autowired
    private DetectionService detectionService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * ????
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> detectImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelVersion") String modelVersion
    ) throws IOException {
        // ????ID
        Long userId = 1L;

        Map<String, Object> result = detectionService.detectImage(file, modelVersion, userId);
        return Result.success(result);
    }

    /**
     * ????
     */
    @PostMapping("/analyze/{recordId}")
    public Result<AiAnalysisReport> analyzeDefects(
            @PathVariable Long recordId
    ) {
        AiAnalysisReport report = detectionService.analyzeDefects(recordId);
        return Result.success(report);
    }

    /**
     * ????????
     */
    @GetMapping("/records")
    public Result<List<DetectionRecord>> getDetectionRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<DetectionRecord> records = detectionService.getDetectionRecords(userId, mode, page, size);
        return Result.success(records);
    }

    /**
     * ??????
     */
    @GetMapping("/defects/{recordId}")
    public Result<List<DefectDetail>> getDefectDetails(
            @PathVariable Long recordId
    ) {
        List<DefectDetail> defects = detectionService.getDefectDetails(recordId);
        return Result.success(defects);
    }

    /**
     * ??AI????
     */
    @GetMapping("/analysis/{recordId}")
    public Result<AiAnalysisReport> getAiAnalysisReport(
            @PathVariable Long recordId
    ) {
        AiAnalysisReport report = detectionService.getAiAnalysisReport(recordId);
        return Result.success(report);
    }
    
    /**
     * ??检测记录详情
     */
    @GetMapping("/record/{recordId}")
    public Result<DetectionRecord> getDetectionRecord(
            @PathVariable Long recordId
    ) {
        DetectionRecord record = detectionService.getDetectionRecord(recordId);
        return Result.success(record);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = detectionService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 标记为未能成功识别
     */
    @PostMapping("/unrecognized")
    public Result<String> markUnrecognized(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String result = detectionService.markUnrecognized(file);
        return Result.success(result);
    }
}
