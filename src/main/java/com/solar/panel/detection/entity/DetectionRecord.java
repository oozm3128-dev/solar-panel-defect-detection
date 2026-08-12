package com.solar.panel.detection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("detection_record")
public class DetectionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("detection_mode")
    private String detectionMode;
    @TableField("model_version")
    private String modelVersion;
    @TableField("original_image_path")
    private String originalImagePath;
    @TableField("result_image_path")
    private String resultImagePath;
    @TableField("detection_time")
    private LocalDateTime detectionTime;
}