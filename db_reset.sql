DROP DATABASE IF EXISTS solar_panel_detection;
CREATE DATABASE solar_panel_detection DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE solar_panel_detection;

-- 用户表 (sys_user)
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    avatar VARCHAR(255),
    role ENUM('admin', 'user') DEFAULT 'user',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 检测记录表 (detection_record)
CREATE TABLE IF NOT EXISTS detection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    detection_mode ENUM('image', 'video', 'camera') NOT NULL,
    model_version VARCHAR(10) NOT NULL,
    original_image_path VARCHAR(255) NOT NULL,
    result_image_path VARCHAR(255) NOT NULL,
    detection_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 缺陷详情表 (defect_detail)
CREATE TABLE IF NOT EXISTS defect_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    defect_type ENUM('black_core', 'crack', 'finger', 'horizontal_dislocation', 'short_circuit', 'thick_line') NOT NULL,
    confidence FLOAT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    w INT NOT NULL,
    h INT NOT NULL,
    FOREIGN KEY (record_id) REFERENCES detection_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 诊断报告表 (ai_analysis_report)
CREATE TABLE IF NOT EXISTS ai_analysis_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    analysis_content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES detection_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 默认用户由应用启动时通过 DatabaseInitConfig 初始化，不在脚本中插入明文密码。
