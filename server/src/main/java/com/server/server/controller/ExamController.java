package com.server.server.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.server.server.common.Result;
import com.server.server.entity.*;
import com.server.server.service.ExamService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 获取考试列表
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(examService.getStudentExams());
    }

    /**
     * 获取考试列表（兼容前端旧路径）
     */
    @GetMapping("/studentList")
    public Result<?> studentList(@RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Long teacherId) {
        return Result.success(examService.getStudentExams(studentId, teacherId));
    }

    /**
     * 开始考试
     */
    @PostMapping("/start")
    public Result<?> start(@RequestBody(required = false) Map<String, Object> body,
                           @RequestParam(required = false) Long examId,
                           @RequestParam(required = false) Long studentId) {
        if (body != null) {
            if (examId == null && body.get("examId") != null) {
                examId = Long.valueOf(body.get("examId").toString());
            }
            if (studentId == null && body.get("studentId") != null) {
                studentId = Long.valueOf(body.get("studentId").toString());
            }
        }
        return Result.success(examService.startExam(studentId, examId));
    }

    /**
     * 提交试卷
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        Long recordId = Long.valueOf(body.get("recordId").toString());
        List<Map<String, Object>> detailsMap = (List<Map<String, Object>>) body.get("details");
        
        List<ExamRecordDetail> details = detailsMap.stream().map(m -> {
            ExamRecordDetail detail = new ExamRecordDetail();
            detail.setQuestionId(Long.valueOf(m.get("questionId").toString()));
            detail.setUserAnswer(m.get("userAnswer") != null ? m.get("userAnswer").toString() : "");
            return detail;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(examService.submitExam(recordId, details));
    }

    // --- 教师端接口 ---

    @PostMapping("/save")
    public Result<?> save(@RequestBody Map<String, Object> body) {
        Map<String, Object> examMap = (Map<String, Object>) body.get("exam");
        List<Map<String, Object>> questionsMap = (List<Map<String, Object>>) body.get("questions");

        Exam exam = new Exam();
        if (examMap.get("id") != null) exam.setId(Long.valueOf(examMap.get("id").toString()));
        exam.setTitle(examMap.get("title").toString());
        if (examMap.get("courseId") != null) exam.setCourseId(Long.valueOf(examMap.get("courseId").toString()));
        exam.setDuration(Integer.valueOf(examMap.get("duration").toString()));
        exam.setTotalPoints(Integer.valueOf(examMap.get("totalPoints").toString()));
        exam.setPassScore(Integer.valueOf(examMap.get("passScore").toString()));
        exam.setMaxAttempts(Integer.valueOf(examMap.get("maxAttempts").toString()));
        exam.setStartTime(java.time.LocalDateTime.parse(examMap.get("startTime").toString().replace(" ", "T")));
        exam.setEndTime(java.time.LocalDateTime.parse(examMap.get("endTime").toString().replace(" ", "T")));

        List<ExamQuestion> questions = questionsMap.stream().map(m -> {
            ExamQuestion eq = new ExamQuestion();
            eq.setQuestionId(Long.valueOf(m.get("questionId").toString()));
            eq.setScore(Integer.valueOf(m.get("score").toString()));
            return eq;
        }).collect(java.util.stream.Collectors.toList());

        examService.saveExamWithQuestions(exam, questions);
        return Result.success();
    }
    
    @PostMapping("/randomQuestions")
    public Result<?> randomQuestions(@RequestBody List<Map<String, Object>> config) {
        return Result.success(examService.randomSelectQuestions(config));
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(examService.getExamDetail(id));
    }

    @GetMapping("/statistics/{id}")
    public Result<?> statistics(@PathVariable Long id) {
        return Result.success(examService.getExamStatistics(id));
    }

    @GetMapping("/stats/{id}")
    public Result<?> stats(@PathVariable Long id) {
        return Result.success(examService.getExamStatistics(id));
    }

    @GetMapping("/records/{id}")
    public Result<?> getRecords(@PathVariable Long id) {
        Map<String, Object> statistics = examService.getExamStatistics(id);
        return Result.success(statistics.get("records"));
    }

    @GetMapping("/recordDetails/{recordId}")
    public Result<?> getRecordDetails(@PathVariable Long recordId) {
        ExamRecord record = examService.getRecordMapper().selectById(recordId);
        List<Map<String, Object>> details = examService.getRecordDetails(recordId);
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("record", record);
        res.put("details", details);
        return Result.success(res);
    }

    @PostMapping("/submitGrading")
    public Result<?> submitGrading(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> details = (List<Map<String, Object>>) body.get("details");
        for (Map<String, Object> d : details) {
            Long id = Long.valueOf(d.get("id").toString());
            Integer score = Integer.valueOf(d.get("score").toString());
            examService.manualGrade(id, score);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        examService.removeById(id);
        return Result.success();
    }

    @GetMapping("/export/{examId}")
    public void export(@PathVariable Long examId, HttpServletResponse response) throws IOException {
        Map<String, Object> statistics = examService.getExamStatistics(examId);
        List<ExamRecord> records = (List<ExamRecord>) statistics.get("records");
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(records, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("成绩单_" + examId, "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    /**
     * 教师手动批改（更新分数）
     */
    @PostMapping("/gradeRecord")
    public Result<?> gradeRecord(@RequestBody Map<String, Object> params) {
        Long recordId = Long.valueOf(params.get("recordId").toString());
        List<Map<String, Object>> grades = (List<Map<String, Object>>) params.get("grades");
        
        examService.manualGrade(recordId, grades);
        return Result.success();
    }
}
