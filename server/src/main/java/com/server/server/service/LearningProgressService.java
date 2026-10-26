package com.server.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.server.entity.LearningProgress;
import com.server.server.mapper.LearningProgressMapper;
import org.springframework.stereotype.Service;

@Service
public class LearningProgressService extends ServiceImpl<LearningProgressMapper, LearningProgress> {

    /**
     * 更新学习进度
     */
    public void updateProgress(Long studentId, Long resourceId, Integer progress) {
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getStudentId, studentId).eq(LearningProgress::getResourceId, resourceId);
        LearningProgress record = this.getOne(wrapper);

        if (record == null) {
            record = new LearningProgress();
            record.setStudentId(studentId);
            record.setResourceId(resourceId);
            record.setProgress(progress);
            record.setIsFinished(progress >= 100 ? 1 : 0);
            this.save(record);
        } else {
            // 只在进度变大时更新
            if (progress > record.getProgress()) {
                record.setProgress(progress);
                if (progress >= 100) record.setIsFinished(1);
                this.updateById(record);
            }
        }
    }
}
