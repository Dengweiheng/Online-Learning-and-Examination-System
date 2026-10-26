package com.server.server.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.server.server.common.Result;
import com.server.server.entity.QuestionBank;
import com.server.server.service.QuestionBankService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 题库控制器
 */
@RestController
@RequestMapping("/questionBank")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer type,
                          @RequestParam(required = false) Integer difficulty,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) Long courseId,
                          @RequestParam(required = false) Long teacherId,
                          @RequestParam(required = false) String keyword) {
        return Result.success(questionBankService.searchQuestions(current, size, type, difficulty, categoryId, courseId, teacherId, keyword));
    }

    /**
     * 保存/更新
     */
    @PostMapping("/save")
    public Result<?> save(@RequestBody QuestionBank question) {
        questionBankService.saveOrUpdate(question);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        questionBankService.removeById(id);
        return Result.success();
    }

    /**
     * 批量导出
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<QuestionBank> list = questionBankService.list();
        ExcelWriter writer = ExcelUtil.getWriter(true);
        
        // 自定义列名
        writer.addHeaderAlias("type", "题型(1单选2多选3判断4填空)");
        writer.addHeaderAlias("content", "题目内容");
        writer.addHeaderAlias("options", "选项(JSON)");
        writer.addHeaderAlias("answer", "答案");
        writer.addHeaderAlias("score", "分值");
        writer.addHeaderAlias("difficulty", "难度(1-3)");
        writer.addHeaderAlias("courseId", "关联课程ID");
        writer.addHeaderAlias("knowledgePoint", "知识点");

        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("题库数据", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        out.close();
    }

    /**
     * 批量导入
     */
    @PostMapping("/import")
    public Result<?> importExcel(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        
        // 设置映射（Excel表头 -> 实体类属性）
        reader.addHeaderAlias("题型(1单选2多选3判断4填空)", "type");
        reader.addHeaderAlias("题目内容", "content");
        reader.addHeaderAlias("选项(JSON)", "options");
        reader.addHeaderAlias("答案", "answer");
        reader.addHeaderAlias("分值", "score");
        reader.addHeaderAlias("难度(1-3)", "difficulty");
        reader.addHeaderAlias("关联课程ID", "courseId");
        reader.addHeaderAlias("知识点", "knowledgePoint");

        List<QuestionBank> list = reader.readAll(QuestionBank.class);
        questionBankService.saveBatch(list);
        return Result.success();
    }
}
