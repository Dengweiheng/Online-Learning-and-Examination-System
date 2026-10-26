package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_resource")
public class CourseResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long chapterId;
    private String title;
    private String type; // VIDEO, PDF, PPT, TEXT
    private String url;
    private String content;
    private LocalDateTime createTime;
}
