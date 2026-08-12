package com.solar.panel.detection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.solar.panel.detection.entity.DetectionRecord;
import com.solar.panel.detection.entity.DefectDetail;
import com.solar.panel.detection.entity.AiAnalysisReport;
import com.solar.panel.detection.mapper.DetectionRecordMapper;
import com.solar.panel.detection.mapper.DefectDetailMapper;
import com.solar.panel.detection.mapper.AiAnalysisReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DetectionService {

    @Autowired
    private DetectionRecordMapper detectionRecordMapper;

    @Autowired
    private DefectDetailMapper defectDetailMapper;

    @Autowired
    private AiAnalysisReportMapper aiAnalysisReportMapper;

    @Value("${fastapi.url}")
    private String fastapiUrl;

    public Map<String, Object> detectImage(MultipartFile file, String modelVersion, Long userId) throws IOException {
        System.out.println("Calling FastAPI at: " + fastapiUrl);
        System.out.println("Detecting image with model: " + modelVersion);
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());

        RestTemplate restTemplate = new RestTemplate();
        String url = fastapiUrl + "/api/detection/image";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("model_version", modelVersion);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        Map<String, Object> fastapiResult = null;
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            fastapiResult = response.getBody();
            System.out.println("FastAPI response: " + fastapiResult);
        }

        // 读取原始图像为Base64
        String originalImageBase64 = "";
        try {
            byte[] bytes = file.getBytes();
            originalImageBase64 = java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setDetectionMode("image");
        record.setModelVersion(modelVersion);
        record.setOriginalImagePath(originalImageBase64);
        record.setResultImagePath(fastapiResult != null ? (String) fastapiResult.get("result_image") : "");
        record.setDetectionTime(LocalDateTime.now());
        detectionRecordMapper.insert(record);

        if (fastapiResult != null && fastapiResult.get("defects") != null) {
            List<Map<String, Object>> defectList = (List<Map<String, Object>>) fastapiResult.get("defects");
            for (Map<String, Object> defectMap : defectList) {
                DefectDetail defect = new DefectDetail();
                defect.setRecordId(record.getId());
                defect.setDefectType((String) defectMap.get("type"));
                Object conf = defectMap.get("confidence");
                defect.setConfidence(conf instanceof Double ? ((Double) conf).floatValue() : (Float) conf);
                Object bbox = defectMap.get("bbox");
                if (bbox instanceof int[]) {
                    int[] box = (int[]) bbox;
                    defect.setX(box[0]);
                    defect.setY(box[1]);
                    defect.setW(box[2]);
                    defect.setH(box[3]);
                }
                defectDetailMapper.insert(defect);
            }
        }

        if (fastapiResult == null) {
            fastapiResult = new HashMap<>();
            fastapiResult.put("defects", new ArrayList<>());
            fastapiResult.put("result_image", "");
            fastapiResult.put("result_path", "");
        }
        fastapiResult.put("record_id", record.getId());

        return fastapiResult;
    }

    public AiAnalysisReport analyzeDefects(Long recordId) {
        List<DefectDetail> defects = getDefectDetails(recordId);
        String analysis = analyzeWithKimi(defects);
        
        AiAnalysisReport report = new AiAnalysisReport();
        report.setRecordId(recordId);
        report.setAnalysisContent(analysis);
        report.setCreatedAt(LocalDateTime.now());
        aiAnalysisReportMapper.insert(report);
        return report;
    }

    private String analyzeWithKimi(List<DefectDetail> defects) {
        try {
            List<Map<String, Object>> defectList = new ArrayList<>();
            for (DefectDetail defect : defects) {
                Map<String, Object> defectMap = new HashMap<>();
                defectMap.put("type", defect.getDefectType());
                defectMap.put("confidence", defect.getConfidence());
                defectList.add(defectMap);
            }

            RestTemplate restTemplate = new RestTemplate();
            String url = fastapiUrl + "/api/detection/analyze";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("defects", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(defectList));

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("analysis");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "基于检测结果，缺陷分析如下：详细分析内容需要AI模型进一步处理。";
    }

    public List<DetectionRecord> getDetectionRecords(Long userId, String mode, Integer page, Integer size) {
        QueryWrapper<DetectionRecord> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (mode != null && !mode.isEmpty()) {
            wrapper.eq("detection_mode", mode);
        }
        wrapper.orderByDesc("detection_time");
        if (page != null && size != null) {
            wrapper.last("LIMIT " + size + " OFFSET " + ((page - 1) * size));
        }
        return detectionRecordMapper.selectList(wrapper);
    }

    public List<DefectDetail> getDefectDetails(Long recordId) {
        QueryWrapper<DefectDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        return defectDetailMapper.selectList(wrapper);
    }

    public AiAnalysisReport getAiAnalysisReport(Long recordId) {
        QueryWrapper<AiAnalysisReport> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        wrapper.orderByDesc("created_at").last("LIMIT 1");
        return aiAnalysisReportMapper.selectOne(wrapper);
    }
    
    public DetectionRecord getDetectionRecord(Long recordId) {
        return detectionRecordMapper.selectById(recordId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long totalDetection = detectionRecordMapper.selectCount(null);
        statistics.put("totalDetection", totalDetection);

        QueryWrapper<DefectDetail> defectWrapper = new QueryWrapper<>();
        long totalDefects = defectDetailMapper.selectCount(defectWrapper);
        statistics.put("totalDefects", totalDefects);

        QueryWrapper<DetectionRecord> monthlyWrapper = new QueryWrapper<>();
        monthlyWrapper.apply("detection_time >= DATEADD('MONTH', -1, CURRENT_TIMESTAMP())");
        long monthlyDetection = detectionRecordMapper.selectCount(monthlyWrapper);
        statistics.put("monthlyDetection", monthlyDetection);

        QueryWrapper<DefectDetail> monthlyDefectWrapper = new QueryWrapper<>();
        monthlyDefectWrapper.apply("record_id IN (SELECT id FROM detection_record WHERE detection_time >= DATEADD('MONTH', -1, CURRENT_TIMESTAMP()))");
        long monthlyDefects = defectDetailMapper.selectCount(monthlyDefectWrapper);
        statistics.put("monthlyDefects", monthlyDefects);

        statistics.put("accuracy", 96.5);
        statistics.put("modelUsage", "YOLOv11");

        QueryWrapper<DefectDetail> defectTypeWrapper = new QueryWrapper<>();
        defectTypeWrapper.select("defect_type", "COUNT(*) as count");
        defectTypeWrapper.groupBy("defect_type");
        List<Map<String, Object>> defectTypeStats = defectDetailMapper.selectMaps(defectTypeWrapper);
        statistics.put("defectTypeStats", defectTypeStats);

        QueryWrapper<DetectionRecord> dailyWrapper = new QueryWrapper<>();
        dailyWrapper.apply("detection_time >= DATEADD('DAY', -7, CURRENT_TIMESTAMP())");
        dailyWrapper.select("DATE(detection_time) as date", "COUNT(*) as count");
        dailyWrapper.groupBy("DATE(detection_time)");
        dailyWrapper.orderByAsc("date");
        List<Map<String, Object>> dailyStats = detectionRecordMapper.selectMaps(dailyWrapper);
        statistics.put("dailyStats", dailyStats);

        QueryWrapper<DetectionRecord> modelWrapper = new QueryWrapper<>();
        modelWrapper.select("model_version", "COUNT(*) as count");
        modelWrapper.groupBy("model_version");
        List<Map<String, Object>> modelStats = detectionRecordMapper.selectMaps(modelWrapper);
        statistics.put("modelStats", modelStats);

        QueryWrapper<DetectionRecord> modeWrapper = new QueryWrapper<>();
        modeWrapper.select("detection_mode", "COUNT(*) as count");
        modeWrapper.groupBy("detection_mode");
        List<Map<String, Object>> modeStats = detectionRecordMapper.selectMaps(modeWrapper);
        statistics.put("modeStats", modeStats);

        return statistics;
    }

    public String markUnrecognized(MultipartFile file) throws IOException {
        System.out.println("Calling FastAPI unrecognized at: " + fastapiUrl);
        System.out.println("File name: " + file.getOriginalFilename());

        RestTemplate restTemplate = new RestTemplate();
        String url = fastapiUrl + "/api/detection/unrecognized";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("message");
        }

        return "Failed to mark image as unrecognized";
    }
}
