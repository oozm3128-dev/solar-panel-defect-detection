package com.solar.panel.detection.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.logging.Logger;

@Component
public class DatabaseInitConfig implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(DatabaseInitConfig.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS sys_user (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    username VARCHAR(50)," +
            "    password VARCHAR(100)," +
            "    name VARCHAR(50)," +
            "    avatar LONGTEXT," +
            "    role VARCHAR(20)," +
            "    created_at TIMESTAMP" +
            ")",
            "CREATE TABLE IF NOT EXISTS detection_record (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    user_id BIGINT," +
            "    detection_mode VARCHAR(20)," +
            "    model_version VARCHAR(20)," +
            "    original_image_path LONGTEXT," +
            "    result_image_path LONGTEXT," +
            "    detection_time TIMESTAMP" +
            ")",
            "CREATE TABLE IF NOT EXISTS defect_detail (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    record_id BIGINT," +
            "    defect_type VARCHAR(50)," +
            "    confidence DOUBLE," +
            "    x INT," +
            "    y INT," +
            "    w INT," +
            "    h INT" +
            ")",
            "CREATE TABLE IF NOT EXISTS ai_analysis_report (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    record_id BIGINT," +
            "    analysis_content TEXT," +
            "    created_at TIMESTAMP" +
            ")"
        };

        for (String sql : tables) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                logger.warning("Table creation skipped: " + e.getMessage());
            }
        }

        // 初始化默认管理员（仅在无用户时）
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
            if (count == null || count == 0) {
                String randomPassword = generateRandomPassword();
                jdbcTemplate.update(
                    "INSERT INTO sys_user (username, password, name, avatar, role, created_at) VALUES (?, ?, ?, ?, ?, NOW())",
                    "admin",
                    passwordEncoder.encode(randomPassword),
                    "管理员",
                    "https://img.icons8.com/color/48/000000/user.png",
                    "admin"
                );
                logger.warning("====================================================");
                logger.warning("默认管理员账号: admin");
                logger.warning("默认管理员密码(仅本次启动,请妥善保存并尽快修改): " + randomPassword);
                logger.warning("====================================================");
            }
        } catch (Exception e) {
            logger.warning("User initialization skipped: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
