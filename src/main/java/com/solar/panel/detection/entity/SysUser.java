package com.solar.panel.detection.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
}