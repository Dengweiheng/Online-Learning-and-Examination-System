package com.server.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.server.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
