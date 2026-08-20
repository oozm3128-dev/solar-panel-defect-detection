package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有用户（管理员功能）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysUser>> getAllUsers() {
        List<SysUser> users = sysUserService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 根据 ID 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.findById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息（管理员功能）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        if (sysUserService.update(user)) {
            return Result.success();
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 删除用户（管理员功能）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        if (sysUserService.deleteUser(id)) {
            return Result.success();
        } else {
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 修改密码（从 SecurityContext 取当前用户，不接受客户端传入 userId）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> passwordData) {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        if (sysUserService.changePassword(currentUser.getId(), oldPassword, newPassword)) {
            return Result.success();
        } else {
            return Result.error(400, "原密码错误或修改失败");
        }
    }

    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return sysUserService.findByUsername(username);
    }
}
