package com.solar.panel.detection.service;

import com.solar.panel.detection.entity.SysUser;
import com.solar.panel.detection.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 根据用户名查询用户
     */
    public SysUser findByUsername(String username) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return sysUserMapper.selectOne(wrapper);
    }

    /**
     * 根据ID查询用户
     */
    public SysUser findById(Long id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 注册用户
     */
    public boolean register(SysUser user) {
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认角色
        user.setRole("user");
        return sysUserMapper.insert(user) > 0;
    }

    /**
     * 更新用户信息
     */
    public boolean update(SysUser user) {
        return sysUserMapper.updateById(user) > 0;
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return sysUserMapper.updateById(user) > 0;
    }

    /**
     * 获取所有用户（管理员功能）
     */
    public List<SysUser> getAllUsers() {
        return sysUserMapper.selectList(null);
    }

    /**
     * 删除用户（管理员功能）
     */
    public boolean deleteUser(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }
}