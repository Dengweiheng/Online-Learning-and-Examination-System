package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Integer score;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer status; // 0进行中, 1待批改, 2已判分
    private Integer attempt;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String username;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;
}
