package com.server.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.server.common.Result;
import com.server.server.entity.Course;
import com.server.server.entity.CourseChapter;
import com.server.server.entity.CourseResource;
import com.server.server.mapper.CourseChapterMapper;
import com.server.server.mapper.CourseResourceMapper;
import com.server.server.entity.CourseSelection;
import com.server.server.entity.LearningProgress;
import com.server.server.entity.User;
import com.server.server.mapper.CourseSelectionMapper;
import com.server.server.service.CourseService;
import com.server.server.service.LearningProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseChapterMapper chapterMapper;

    @Autowired
    private CourseResourceMapper resourceMapper;

    @Autowired
    private LearningProgressService progressService;

    @Autowired
    private CourseSelectionMapper selectionMapper;

    /**
     * 课程广场
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) Long teacherId,
                          @RequestParam(required = false) String keyword) {
        return Result.success(courseService.searchCourses(current, size, categoryId, teacherId, keyword));
    }

    /**
     * 课程广场（兼容前端旧路径）
     */
    @GetMapping("/search")
    public Result<?> search(@RequestParam(defaultValue = "1") int current,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(required = false) Long teacherId,
                            @RequestParam(required = false) String keyword) {
        return Result.success(courseService.searchCourses(current, size, categoryId, teacherId, keyword));
    }

    /**
     * 加入课程
     */
    @PostMapping("/join")
    public Result<?> join(@RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        Long courseId = Long.valueOf(params.get("courseId").toString());
        String code = params.get("code") != null ? params.get("code").toString() : null;
        courseService.joinCourse(studentId, courseId, code);
        return Result.success();
    }

    /**
     * 课程详情(包含大纲)
     */
    @GetMapping({"/detail/{id}", "/detail"})
    public Result<Map<String, Object>> detail(@PathVariable(required = false) Long id,
                                              @RequestParam(required = false) Long courseId) {
        Long targetId = id != null ? id : courseId;
        Course course = courseService.getById(targetId);

        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(CourseChapter::getCourseId, targetId).orderByAsc(CourseChapter::getSort);
        List<CourseChapter> chapters = chapterMapper.selectList(chapterWrapper);

        List<Map<String, Object>> chapterList = chapters.stream().map(chapter -> {
            Map<String, Object> chapterMap = new HashMap<>();
            chapterMap.put("id", chapter.getId());
            chapterMap.put("courseId", chapter.getCourseId());
            chapterMap.put("title", chapter.getTitle());
            chapterMap.put("sort", chapter.getSort());

            LambdaQueryWrapper<CourseResource> resourceWrapper = new LambdaQueryWrapper<>();
            resourceWrapper.eq(CourseResource::getChapterId, chapter.getId());
            List<CourseResource> resources = resourceMapper.selectList(resourceWrapper)
                    .stream()
                    .map(this::normalizeResource)
                    .collect(Collectors.toList());
            chapterMap.put("resources", resources);
            return chapterMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("course", course);
        result.put("chapters", chapterList);
        result.put("outline", chapterList);
        return Result.success(result);
    }

    /**
     * 资源详情（章节内切换）
     */
    @GetMapping("/resource/{id}")
    public Result<Map<String, Object>> resource(@PathVariable Long id) {
        CourseResource resource = normalizeResource(resourceMapper.selectById(id));
        if (resource == null) {
            return Result.success(null);
        }

        LambdaQueryWrapper<CourseResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseResource::getChapterId, resource.getChapterId());
        List<CourseResource> siblings = resourceMapper.selectList(wrapper)
                .stream()
                .map(this::normalizeResource)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("resource", resource);
        result.put("siblings", siblings);
        return Result.success(result);
    }

    /**
     * 更新进度
     */
    @PostMapping("/progress")
    public Result<?> updateProgress(@RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        Long resourceId = Long.valueOf(params.get("resourceId").toString());
        Integer progress = Integer.valueOf(params.get("progress").toString());
        progressService.updateProgress(studentId, resourceId, progress);
        return Result.success();
    }

    /**
     * 获取单条学习进度
     */
    @GetMapping("/progress/{resourceId}")
    public Result<LearningProgress> getProgress(@PathVariable Long resourceId, @RequestParam Long studentId) {
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getResourceId, resourceId).eq(LearningProgress::getStudentId, studentId);
        return Result.success(progressService.getOne(wrapper));
    }

    /**
     * 我的课程
     */
    @GetMapping("/myEnrolled")
    public Result<Map<String, Object>> myEnrolled(@RequestParam Long studentId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId);
        List<CourseSelection> selections = selectionMapper.selectList(wrapper);

        List<Long> courseIds = selections.stream().map(CourseSelection::getCourseId).collect(Collectors.toList());
        List<Course> courses = courseIds.isEmpty() ? List.of() : courseService.listByIds(courseIds);

        Map<String, Object> progressMap = new HashMap<>();
        for (Course course : courses) {
            LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.eq(CourseChapter::getCourseId, course.getId());
            List<CourseChapter> courseChapters = chapterMapper.selectList(chapterWrapper);
            List<Long> resourceIds = courseChapters.stream().flatMap(chapter -> {
                LambdaQueryWrapper<CourseResource> resourceWrapper = new LambdaQueryWrapper<>();
                resourceWrapper.eq(CourseResource::getChapterId, chapter.getId());
                return resourceMapper.selectList(resourceWrapper).stream().map(CourseResource::getId);
            }).collect(Collectors.toList());

            if (resourceIds.isEmpty()) {
                progressMap.put(String.valueOf(course.getId()), 0);
                continue;
            }

            LambdaQueryWrapper<LearningProgress> progressWrapper = new LambdaQueryWrapper<>();
            progressWrapper.eq(LearningProgress::getStudentId, studentId).in(LearningProgress::getResourceId, resourceIds);
            List<LearningProgress> progressList = progressService.list(progressWrapper);
            int averageProgress = progressList.isEmpty()
                    ? 0
                    : (int) progressList.stream().mapToInt(item -> item.getProgress() == null ? 0 : item.getProgress()).average().orElse(0);
            progressMap.put(String.valueOf(course.getId()), averageProgress);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses);
        result.put("progressMap", progressMap);
        return Result.success(result);
    }

    /**
     * 退出课程
     */
    @DeleteMapping("/selection/{courseId}")
    public Result<?> quitCourse(@PathVariable Long courseId, @RequestParam Long studentId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseId, courseId).eq(CourseSelection::getStudentId, studentId);
        selectionMapper.delete(wrapper);
        return Result.success();
    }

    // --- 教师端接口 ---

    /**
     * 创建/修改课程
     */
    @PostMapping("/save")
    public Result<?> save(@RequestBody Course course) {
        courseService.saveOrUpdate(course);
        return Result.success();
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    /**
     * 保存章节
     */
    @PostMapping("/chapter/save")
    public Result<?> saveChapter(@RequestBody CourseChapter chapter) {
        if (chapter.getId() == null) {
            chapterMapper.insert(chapter);
        } else {
            chapterMapper.updateById(chapter);
        }
        return Result.success();
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/chapter/{id}")
    public Result<?> deleteChapter(@PathVariable Long id) {
        chapterMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 保存资源
     */
    @PostMapping("/resource/save")
    public Result<?> saveResource(@RequestBody CourseResource resource) {
        if (resource.getId() == null) {
            resourceMapper.insert(resource);
        } else {
            resourceMapper.updateById(resource);
        }
        return Result.success();
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/resource/{id}")
    public Result<?> deleteResource(@PathVariable Long id) {
        resourceMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 获取选课学生列表
     */
    @GetMapping("/students/{id}")
    public Result<List<User>> getStudents(@PathVariable Long id) {
        return Result.success(courseService.getCourseStudents(id));
    }

    /**
     * 移除选课学生
     */
    @DeleteMapping("/removeStudent/{courseId}/{studentId}")
    public Result<?> removeStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        courseService.removeStudent(courseId, studentId);
        return Result.success();
    }

    private CourseResource normalizeResource(CourseResource resource) {
        if (resource == null) {
            return null;
        }
        CourseResource normalized = new CourseResource();
        BeanUtils.copyProperties(resource, normalized);
        if ((normalized.getContent() == null || normalized.getContent().isEmpty()) && normalized.getUrl() != null) {
            normalized.setContent(normalized.getUrl());
        }
        return normalized;
    }
}
