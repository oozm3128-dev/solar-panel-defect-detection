package com.solar.panel.detection.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitConfig implements ApplicationRunner {

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
                System.out.println("Table created: " + sql.substring(20, 40));
            } catch (Exception e) {
                System.out.println("Table creation skipped: " + e.getMessage());
            }
        }

        // 初始化默认用户
        try {
            String checkUserSql = "SELECT COUNT(*) FROM sys_user";
            Integer count = jdbcTemplate.queryForObject(checkUserSql, Integer.class);
            if (count == 0) {
                String insertUserSql = "INSERT INTO sys_user (username, password, name, avatar, role, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
                jdbcTemplate.update(insertUserSql, "admin", passwordEncoder.encode("123456"), "管理员", "https://img.icons8.com/color/48/000000/user.png", "ADMIN");
                System.out.println("Default user created");
            }
        } catch (Exception e) {
            System.out.println("User initialization skipped: " + e.getMessage());
        }
    }
}
