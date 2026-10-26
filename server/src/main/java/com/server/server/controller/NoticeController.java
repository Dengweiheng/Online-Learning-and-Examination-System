package com.server.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.common.Result;
import com.server.server.entity.Notice;
import com.server.server.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 系统级公告列表（带角色过滤）
     */
    @GetMapping("/list")
    public Result<List<Notice>> list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            // 获取面向全体或特定角色的公告
            wrapper.eq(Notice::getTargetRole, "ALL").or().eq(Notice::getTargetRole, role);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return Result.success(noticeMapper.selectList(wrapper));
    }

    /**
     * 获取最新公告（兼容前端首页展示）
     */
    @GetMapping("/latest")
    public Result<Notice> latest(@RequestParam(required = false) String role) {
        List<Notice> notices = list(role).getData();
        return Result.success(notices == null || notices.isEmpty() ? null : notices.get(0));
    }

    /**
     * 保存/修改 (Admin)
     */
    @PostMapping("/save")
    public Result<?> save(@RequestBody Notice notice) {
        if (notice.getId() == null) {
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
        return Result.success();
    }

    /**
     * 删除 (Admin)
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }
}
