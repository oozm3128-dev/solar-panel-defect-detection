package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.config.JwtUtils;
import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        SysUser user = sysUserService.findByUsername(username);
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        // 生成 JWT 令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return Result.success(response);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysUser user) {
        // 校验用户名是否已存在
        if (sysUserService.findByUsername(user.getUsername()) != null) {
            return Result.error(400, "用户名已存在");
        }

        // 注册用户
        if (sysUserService.register(user)) {
            return Result.success();
        } else {
            return Result.error(500, "注册失败");
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<SysUser> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);
        SysUser user = sysUserService.findById(userId);
        return Result.success(user);
    }
}
