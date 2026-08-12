package com.solar.panel.detection.controller;

import com.solar.panel.detection.common.Result;
import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有用户（管理员功能）
     */
    @GetMapping("/all")
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
     * 更新用户信息
     */
    @PutMapping("/{id}")
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
    public Result<Void> deleteUser(@PathVariable Long id) {
        if (sysUserService.deleteUser(id)) {
            return Result.success();
        } else {
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        if (sysUserService.changePassword(userId, oldPassword, newPassword)) {
            return Result.success();
        } else {
            return Result.error(400, "原密码错误或修改失败");
        }
    }
}