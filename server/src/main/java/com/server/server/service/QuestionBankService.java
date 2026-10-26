package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.server.entity.QuestionBank;
import com.server.server.mapper.QuestionBankMapper;
import org.springframework.stereotype.Service;

import com.server.server.entity.Course;
import com.server.server.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库服务实现类
 */
@Service
public class QuestionBankService extends ServiceImpl<QuestionBankMapper, QuestionBank> {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 分页搜索题目
     */
    public Page<QuestionBank> searchQuestions(int current, int size, Integer type, Integer difficulty, Long categoryId, Long courseId, Long teacherId, String keyword) {
        Page<QuestionBank> page = new Page<>(current, size);
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        
        if (type != null) wrapper.eq(QuestionBank::getType, type);
        if (difficulty != null) wrapper.eq(QuestionBank::getDifficulty, difficulty);
        if (categoryId != null) wrapper.eq(QuestionBank::getCategoryId, categoryId);
        
        if (courseId != null) {
            wrapper.eq(QuestionBank::getCourseId, courseId);
        } else if (teacherId != null) {
            // 如果没有选特定课程，但有教师ID，则只查该教师名下的课程题目
            List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacherId));
            if (courses.isEmpty()) {
                return page; // 返回空页
            }
            List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
            wrapper.in(QuestionBank::getCourseId, courseIds);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(QuestionBank::getContent, keyword);
        }
        
        wrapper.orderByDesc(QuestionBank::getCreateTime);
        return this.page(page, wrapper);
    }
}
