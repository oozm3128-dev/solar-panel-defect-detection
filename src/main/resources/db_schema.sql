CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    name VARCHAR(50),
    avatar LONGTEXT,
    role VARCHAR(20),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS detection_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    detection_mode VARCHAR(20),
    model_version VARCHAR(20),
    original_image_path LONGTEXT,
    result_image_path LONGTEXT,
    detection_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS defect_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT,
    defect_type VARCHAR(50),
    confidence DOUBLE,
    x INT,
    y INT,
    w INT,
    h INT
);

CREATE TABLE IF NOT EXISTS ai_analysis_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT,
    analysis_content TEXT,
    created_at TIMESTAMP
);