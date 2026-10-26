package com.server.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type; // 1单选, 2多选, 3判断, 4填空
    private String content;
    private String options; // JSON string
    private String answer;
    private String analysis;
    private Integer score;
    private Integer difficulty;
    private Long categoryId;
    private Long courseId;
    private String knowledgePoint;
    private LocalDateTime createTime;
}
