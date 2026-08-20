package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.config.JwtUtils;
import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /** 登录限流：每个 IP 每分钟最多 5 次 */
    private static final int MAX_LOGIN_PER_MINUTE = 5;
    private static final long WINDOW_MILLIS = 60_000L;
    private final Map<String, long[]> loginRateMap = new ConcurrentHashMap<>();

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String clientIp = getClientIp();
        if (isRateLimited(clientIp)) {
            return Result.error(429, "登录请求过于频繁，请稍后再试");
        }

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

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        // entry[0] = 窗口起始时间, entry[1] = 窗口内请求计数
        long[] entry = loginRateMap.compute(ip, (k, v) -> {
            if (v == null || now - v[0] > WINDOW_MILLIS) {
                return new long[]{now, 1};
            }
            v[1] = v[1] + 1;
            return v;
        });
        return entry[1] > MAX_LOGIN_PER_MINUTE;
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip == null ? "unknown" : ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
