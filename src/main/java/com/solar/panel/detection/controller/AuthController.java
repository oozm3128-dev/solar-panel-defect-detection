package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.config.JwtUtils;
import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    /**
     * ????????
     */
    private void createTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // ?????
            String createUserTable = "CREATE TABLE IF NOT EXISTS sys_user " +
                "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50), " +
                "password VARCHAR(100), " +
                "name VARCHAR(50), " +
                "avatar LONGTEXT, " +
                "role VARCHAR(20), " +
                "created_at TIMESTAMP)";
            stmt.executeUpdate(createUserTable);

            // ???????
            String createDetectionTable = "CREATE TABLE IF NOT EXISTS detection_record " +
                "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT, " +
                "detection_mode VARCHAR(20), " +
                "model_version VARCHAR(20), " +
                "original_image_path LONGTEXT, " +
                "result_image_path LONGTEXT, " +
                "detection_time TIMESTAMP)";
            stmt.executeUpdate(createDetectionTable);

            // ???????
            String createDefectTable = "CREATE TABLE IF NOT EXISTS defect_detail " +
                "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "record_id BIGINT, " +
                "defect_type VARCHAR(50), " +
                "confidence DOUBLE, " +
                "x INT, " +
                "y INT, " +
                "w INT, " +
                "h INT)";
            stmt.executeUpdate(createDefectTable);

            // ??AI?????
            String createAiReportTable = "CREATE TABLE IF NOT EXISTS ai_analysis_report " +
                "(id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "record_id BIGINT, " +
                "analysis_content TEXT, " +
                "created_at TIMESTAMP)";
            stmt.executeUpdate(createAiReportTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        // ???????
        createTables();

        String username = loginData.get("username");
        String password = loginData.get("password");

        SysUser user = sysUserService.findByUsername(username);
        if (user == null) {
            // ??????????????????
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setName("????");
            user.setRole("user");
            sysUserService.register(user);
        } else if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(401, "????????");
        }

        // ?? JWT ??
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return Result.success(response);
    }

    /**
     * ??
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysUser user) {
        // ??????????
        if (sysUserService.findByUsername(user.getUsername()) != null) {
            return Result.error(400, "??????");
        }

        // ????
        if (sysUserService.register(user)) {
            return Result.success();
        } else {
            return Result.error(500, "????");
        }
    }

    /**
     * ????????
     */
    @GetMapping("/current")
    public Result<SysUser> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);
        SysUser user = sysUserService.findById(userId);
        return Result.success(user);
    }
}
