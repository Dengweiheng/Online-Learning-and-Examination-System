package com.server.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.common.Result;
import com.server.server.entity.Config;
import com.server.server.mapper.ConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigMapper configMapper;

    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public Result<List<Config>> list() {
        return Result.success(configMapper.selectList(null));
    }

    /**
     * 根据 Key 获取配置
     */
    @GetMapping("/{key}")
    public Result<Config> getByKey(@PathVariable String key) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, key);
        return Result.success(configMapper.selectOne(wrapper));
    }

    /**
     * 保存/修改配置
     */
    @PostMapping("/save")
    public Result<?> save(@RequestBody Config config) {
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        return Result.success();
    }
}
