package com.server.server.controller;

import com.server.server.common.Result;
import com.server.server.entity.User;
import com.server.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        Map<String, Object> data = userService.login(body.get("username"), body.get("password"));
        return Result.success(data);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody User user) {
        // 这里简单处理：直接更新传入的非空字段。
        // 在实际项目中，建议通过 JWT 获取当前用户 ID 确保安全
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info/{id}")
    public Result<User> getInfo(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    // --- 管理员接口 ---

    /**
     * 分页查询用户
     */
    @GetMapping("/admin/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String role,
                          @RequestParam(required = false) String keyword) {
        return Result.success(userService.searchUsers(current, size, role, keyword));
    }

    /**
     * 管理员修改用户（包含角色分配和启用/禁用）
     */
    @PutMapping("/admin/update")
    public Result<?> adminUpdate(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/admin/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 删除用户（兼容前端直接调用）
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/admin/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPassword = params.get("newPassword").toString();
        userService.resetPassword(userId, newPassword);
        return Result.success();
    }
}
