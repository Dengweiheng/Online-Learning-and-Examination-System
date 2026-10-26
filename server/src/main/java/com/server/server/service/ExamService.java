package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.server.entity.*;
import com.server.server.exception.BusinessException;
import com.server.server.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamService extends ServiceImpl<ExamMapper, Exam> {

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ExamRecordMapper recordMapper;

    @Autowired
    private ExamRecordDetailMapper recordDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseSelectionMapper selectionMapper;

    public ExamRecordMapper getRecordMapper() {
        return recordMapper;
    }

    /**
     * 获取考试列表并标记状态
     */
    public List<Map<String, Object>> getStudentExams() {
        return getStudentExams(null);
    }

    public List<Map<String, Object>> getStudentExams(Long studentId) {
        return getStudentExams(studentId, null);
    }

    public List<Map<String, Object>> getStudentExams(Long studentId, Long teacherId) {
        List<Exam> exams;
        if (studentId != null) {
            // 只获取学生已选课程的考试
            List<CourseSelection> selections = selectionMapper.selectList(
                new LambdaQueryWrapper<CourseSelection>().eq(CourseSelection::getStudentId, studentId)
            );
            if (selections.isEmpty()) {
                return new ArrayList<>();
            }
            List<Long> joinedCourseIds = selections.stream()
                .map(s -> Long.valueOf(s.getCourseId()))
                .collect(Collectors.toList());
            exams = this.list(new LambdaQueryWrapper<Exam>().in(Exam::getCourseId, joinedCourseIds));
        } else if (teacherId != null) {
            // 获取该教师名下课程的考试
            List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacherId));
            if (courses.isEmpty()) {
                return new ArrayList<>();
            }
            List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
            exams = this.list(new LambdaQueryWrapper<Exam>().in(Exam::getCourseId, courseIds));
        } else {
            exams = this.list();
        }
        
        LocalDateTime now = LocalDateTime.now();

        return exams.stream().map(exam -> {
            // 设置课程标题
            Course course = courseMapper.selectById(exam.getCourseId());
            if (course != null) {
                exam.setCourseTitle(course.getTitle());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("exam", exam);
            
            // 获取该学生的考试记录
            if (studentId != null) {
                LambdaQueryWrapper<ExamRecord> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(ExamRecord::getExamId, exam.getId())
                             .eq(ExamRecord::getStudentId, studentId)
                             .orderByDesc(ExamRecord::getSubmitTime);
                List<ExamRecord> records = recordMapper.selectList(recordWrapper);
                map.put("records", records);
                map.put("hasTaken", !records.isEmpty());
            } else {
                map.put("records", new ArrayList<>());
                map.put("hasTaken", false);
            }

            String status;
            if (now.isBefore(exam.getStartTime())) {
                status = "未开始";
            } else if (now.isAfter(exam.getEndTime())) {
                status = "已结束";
            } else {
                status = "进行中";
            }
            map.put("status", status);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取试卷详情 (包含基础信息和关联题目)
     */
    public Map<String, Object> getExamDetail(Long id) {
        Exam exam = this.getById(id);
        if (exam == null)
            return null;

        // 设置课程标题
        Course course = courseMapper.selectById(exam.getCourseId());
        if (course != null) {
            exam.setCourseTitle(course.getTitle());
        }

        // 获取已选题目关联
        LambdaQueryWrapper<ExamQuestion> eqWrapper = new LambdaQueryWrapper<>();
        eqWrapper.eq(ExamQuestion::getExamId, id).orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);

        // 获取题目详情并设置考试分值
        List<QuestionBank> questions = examQuestions.stream().map(eq -> {
            QuestionBank q = questionBankMapper.selectById(eq.getQuestionId());
            if (q != null) {
                q.setScore(eq.getScore());
            }
            return q;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("exam", exam);
        res.put("questions", questions);
        return res;
    }

    /**
     * 开始考试
     */
    @Transactional
    public Map<String, Object> startExam(Long studentId, Long examId) {
        Exam exam = this.getById(examId);
        if (exam == null)
            throw new BusinessException("考试不存在");

        // 检查次数
        LambdaQueryWrapper<ExamRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ExamRecord::getExamId, examId).eq(ExamRecord::getStudentId, studentId);
        Long count = recordMapper.selectCount(recordWrapper);
        if (count >= exam.getMaxAttempts()) {
            throw new BusinessException("考试次数已用尽");
        }

        // 创建记录
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        record.setAttempt(count.intValue() + 1);
        recordMapper.insert(record);

        // 获取题目（不返回答案）
        LambdaQueryWrapper<ExamQuestion> eqWrapper = new LambdaQueryWrapper<>();
        eqWrapper.eq(ExamQuestion::getExamId, examId).orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);

        List<QuestionBank> questions = examQuestions.stream().map(eq -> {
            QuestionBank q = questionBankMapper.selectById(eq.getQuestionId());
            q.setAnswer(null); // 隐藏答案
            q.setAnalysis(null);
            // 顺便把题目在本次考试的分值带上
            q.setScore(eq.getScore());
            return q;
        }).collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("recordId", record.getId());
        res.put("exam", exam);
        res.put("questions", questions);

        // 计算剩余时间（秒）
        long usedSeconds = java.time.Duration.between(record.getStartTime(), LocalDateTime.now()).getSeconds();
        long remainingTime = (exam.getDuration() * 60L) - usedSeconds;
        res.put("remainingTime", Math.max(0, remainingTime));

        return res;
    }

    /**
     * 提交试卷
     */
    @Transactional
    public Integer submitExam(Long recordId, List<ExamRecordDetail> details) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null || record.getStatus() != 0)
            throw new BusinessException("纪录无效或已提交");

        Exam exam = this.getById(record.getExamId());
        int totalScore = 0;

        for (ExamRecordDetail detail : details) {
            QuestionBank q = questionBankMapper.selectById(detail.getQuestionId());
            ExamQuestion eq = examQuestionMapper.selectOne(new LambdaQueryWrapper<ExamQuestion>()
                    .eq(ExamQuestion::getExamId, exam.getId())
                    .eq(ExamQuestion::getQuestionId, q.getId()));

            boolean isCorrect = false;
            String correctAnswer = q.getAnswer() != null ? q.getAnswer().trim() : "";
            String userAnswer = detail.getUserAnswer() != null ? detail.getUserAnswer().trim() : "";

            if (q.getType() == 2) { // 多选题：忽略顺序
                String[] correctArr = correctAnswer.split("[,，]");
                String[] userArr = userAnswer.split("[,，]");
                
                if (correctArr.length == userArr.length && correctArr.length > 0) {
                    java.util.List<String> correctList = java.util.Arrays.stream(correctArr).map(String::trim).sorted().collect(Collectors.toList());
                    java.util.List<String> userList = java.util.Arrays.stream(userArr).map(String::trim).sorted().collect(Collectors.toList());
                    if (correctList.equals(userList)) {
                        isCorrect = true;
                    }
                }
            } else if (q.getType() <= 4) {
                if (correctAnswer.equalsIgnoreCase(userAnswer)) {
                    isCorrect = true;
                }
            }

            detail.setRecordId(recordId);
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setScore(isCorrect ? eq.getScore() : 0);
            recordDetailMapper.insert(detail);

            if (isCorrect)
                totalScore += eq.getScore();
        }

        record.setScore(totalScore);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(2); // 自动判分完成
        recordMapper.updateById(record);
        return totalScore;
    }

    /**
     * 自动组卷 (随机抽题)
     * configList: [{type: 1, quantity: 10, difficulty: 2, categoryId: 1}, ...]
     */
    public List<QuestionBank> randomSelectQuestions(List<Map<String, Object>> configList) {
        List<QuestionBank> allSelected = new ArrayList<>();
        for (Map<String, Object> config : configList) {
            Integer type = (Integer) config.get("type");
            Integer quantity = (Integer) config.get("quantity");
            Integer difficulty = (Integer) config.get("difficulty");
            Object catIdObj = config.get("categoryId");
            Long categoryId = catIdObj != null ? Long.valueOf(catIdObj.toString()) : null;

            LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
            if (type != null)
                wrapper.eq(QuestionBank::getType, type);
            if (difficulty != null)
                wrapper.eq(QuestionBank::getDifficulty, difficulty);
            if (categoryId != null)
                wrapper.eq(QuestionBank::getCategoryId, categoryId);

            wrapper.last("ORDER BY RAND() LIMIT " + quantity);
            allSelected.addAll(questionBankMapper.selectList(wrapper));
        }
        return allSelected;
    }

    /**
     * 保存试卷及其题目关联
     */
    @Transactional
    public void saveExamWithQuestions(Exam exam, List<ExamQuestion> questions) {
        this.saveOrUpdate(exam);

        // 先删除旧的关联
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId()));

        // 保存新关联
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion eq = questions.get(i);
            eq.setExamId(exam.getId());
            eq.setSort(i + 1);
            examQuestionMapper.insert(eq);
        }
    }

    /**
     * 获取考试监控统计（增强版）
     */
    public Map<String, Object> getExamStatistics(Long examId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getExamId, examId);
        List<ExamRecord> records = recordMapper.selectList(wrapper);

        if (records.isEmpty()) {
            stats.put("totalParticipants", 0);
            stats.put("averageScore", 0);
            stats.put("maxScore", 0);
            return stats;
        }

        int totalUsers = (int) records.stream().map(ExamRecord::getStudentId).distinct().count();
        int submittedCount = (int) records.stream().filter(r -> r.getStatus() >= 1).count();

        List<ExamRecord> gradedRecords = records.stream().filter(r -> r.getStatus() == 2).collect(Collectors.toList());
        double avgScore = gradedRecords.stream().mapToInt(ExamRecord::getScore).average().orElse(0);
        int maxScore = gradedRecords.stream().mapToInt(ExamRecord::getScore).max().orElse(0);

        // 分数段分布 (0-60, 60-70, 70-80, 80-90, 90-100)
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("0-60", gradedRecords.stream().filter(r -> r.getScore() < 60).count());
        distribution.put("60-70", gradedRecords.stream().filter(r -> r.getScore() >= 60 && r.getScore() < 70).count());
        distribution.put("70-80", gradedRecords.stream().filter(r -> r.getScore() >= 70 && r.getScore() < 80).count());
        distribution.put("80-90", gradedRecords.stream().filter(r -> r.getScore() >= 80 && r.getScore() < 90).count());
        distribution.put("90-100", gradedRecords.stream().filter(r -> r.getScore() >= 90).count());

        // 为每个记录补充用户信息
        for (ExamRecord r : records) {
            User student = userMapper.selectById(r.getStudentId());
            if (student != null) {
                r.setUsername(student.getUsername());
                r.setNickname(student.getNickname());
            }
        }

        stats.put("totalParticipants", totalUsers);
        stats.put("submittedCount", submittedCount);
        stats.put("averageScore", avgScore);
        stats.put("maxScore", maxScore);
        stats.put("distribution", distribution);
        stats.put("records", records);

        return stats;
    }

    /**
     * 获取答卷详情
     */
    public List<Map<String, Object>> getRecordDetails(Long recordId) {
        LambdaQueryWrapper<ExamRecordDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecordDetail::getRecordId, recordId);
        List<ExamRecordDetail> details = recordDetailMapper.selectList(wrapper);

        return details.stream().map(detail -> {
            Map<String, Object> map = new HashMap<>();
            map.put("detail", detail);
            map.put("question", questionBankMapper.selectById(detail.getQuestionId()));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 手动批改
     */
    @Transactional
    public void manualGrade(Long detailId, Integer score) {
        ExamRecordDetail detail = recordDetailMapper.selectById(detailId);
        if (detail == null)
            throw new BusinessException("详情不存在");

        int diff = score - detail.getScore();
        detail.setScore(score);
        detail.setIsCorrect(score > 0 ? 1 : 0); // 简单逻辑：只要有分就算对或部分对
        recordDetailMapper.updateById(detail);

        // 更新总分
        ExamRecord record = recordMapper.selectById(detail.getRecordId());
        record.setScore(record.getScore() + diff);
        record.setStatus(2); // 标记为已判分
        recordMapper.updateById(record);
    }
    /**
     * 手动批改
     */
    @Transactional
    public void manualGrade(Long recordId, List<Map<String, Object>> grades) {
        int totalScore = 0;
        for (Map<String, Object> g : grades) {
            Long detailId = Long.valueOf(g.get("detailId").toString());
            int score = Integer.parseInt(g.get("score").toString());
            boolean isCorrect = Boolean.parseBoolean(g.get("isCorrect").toString());
            
            ExamRecordDetail detail = recordDetailMapper.selectById(detailId);
            if (detail != null) {
                detail.setScore(score);
                detail.setIsCorrect(isCorrect ? 1 : 0);
                recordDetailMapper.updateById(detail);
            }
            totalScore += score;
        }
        
        ExamRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            record.setScore(totalScore);
            record.setStatus(2); // 设为已判分
            recordMapper.updateById(record);
        }
    }
}
