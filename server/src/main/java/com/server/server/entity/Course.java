package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String cover;
    private String description;
    private Long categoryId;
    private Long teacherId;
    private String courseCode;
    private Integer isPublic; // 0私有, 1公开

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 冗余字段或展示字段
    @TableField(exist = false)
    private String teacherName;
    @TableField(exist = false)
    private String categoryName;
}
