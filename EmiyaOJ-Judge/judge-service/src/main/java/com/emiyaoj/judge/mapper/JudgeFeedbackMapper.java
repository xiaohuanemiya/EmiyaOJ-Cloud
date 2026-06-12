package com.emiyaoj.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.judge.domain.entity.JudgeFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface JudgeFeedbackMapper extends BaseMapper<JudgeFeedback> {

    @Insert("""
            INSERT IGNORE INTO judge_feedback
                (submission_id, status, source, agent_type, create_time, update_time)
            VALUES
                (#{submissionId}, 'PENDING', 'AGENT', #{agentType}, NOW(), NOW())
            """)
    int insertPendingIfAbsent(@Param("submissionId") Long submissionId,
                              @Param("agentType") String agentType);
}
