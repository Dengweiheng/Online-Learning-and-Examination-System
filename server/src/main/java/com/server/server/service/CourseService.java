package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.server.entity.Course;
import com.server.server.entity.CourseSelection;
import com.server.server.entity.User;
import com.server.server.exception.BusinessException;
import com.server.server.mapper.CourseMapper;
import com.server.server.mapper.CourseSelectionMapper;
import com.server.server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    @Autowired
    private CourseSelectionMapper selectionMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 搜索课程 (支持教师筛选)
     */
    public Page<Course> searchCourses(int current, int size, Long categoryId, Long teacherId, String keyword) {
        Page<Course> page = new Page<>(current, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        return this.page(page, wrapper);
    }

    /**
     * 加入课程
     */
    public void joinCourse(Long studentId, Long courseId, String code) {
        Course course = this.getById(courseId);
        if (course == null) throw new BusinessException("课程不存在");

        // 如果是私有课，验证课程码
        if (course.getIsPublic() == 0) {
            if (code == null || !code.equals(course.getCourseCode())) {
                throw new BusinessException("课程码错误");
            }
        }

        // 检查是否已选
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId).eq(CourseSelection::getCourseId, courseId);
        if (selectionMapper.selectOne(wrapper) != null) {
            throw new BusinessException("您已加入该课程");
        }

        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        selectionMapper.insert(selection);
    }

    /**
     * 获取选课学生列表
     */
    public List<User> getCourseStudents(Long courseId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseId, courseId);
        List<CourseSelection> selections = selectionMapper.selectList(wrapper);
        
        if (selections.isEmpty()) return new ArrayList<>();
        
        List<Long> studentIds = selections.stream().map(CourseSelection::getStudentId).collect(Collectors.toList());
        return userMapper.selectBatchIds(studentIds);
    }

    /**
     * 移除选课学生
     */
    public void removeStudent(Long courseId, Long studentId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseId, courseId).eq(CourseSelection::getStudentId, studentId);
        selectionMapper.delete(wrapper);
    }
}
