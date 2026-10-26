package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.server.entity.User;
import com.server.server.exception.BusinessException;
import com.server.server.mapper.UserMapper;
import com.server.server.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    public void register(User user) {
        // 1. 检查用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        if (this.getOne(queryWrapper) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3. 默认角色为学生，管理员新增用户时允许传入角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("STUDENT");
        }
        user.setStatus(1); // 开启

        // 4. 保存
        this.save(user);
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        // 1. 根据用户名查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = this.getOne(queryWrapper);

        // 2. 验证
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已停用");
        }

        // 3. 生成 Token
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());

        // 4. 返回信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 分页搜索用户 (Admin)
     */
    public Page<User> searchUsers(int current, int size, String role, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 重置密码
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = this.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }
}
