package com.emiyaoj.judge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.dto.SolvedProblemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("""
            SELECT COUNT(1)
            FROM submission s
            WHERE s.user_id = #{userId}
              AND s.deleted = 0
            """)
    Integer countUserSubmissions(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM submission s
            INNER JOIN submission_judge_result r ON r.submission_id = s.id
            WHERE s.user_id = #{userId}
              AND s.deleted = 0
              AND r.status = #{status}
            """)
    Integer countUserSubmissionsByStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("""
            SELECT s.problem_id AS problem_id,
                   MIN(COALESCE(r.finish_time, s.create_time)) AS accepted_at
            FROM submission s
            INNER JOIN submission_judge_result r ON r.submission_id = s.id
            WHERE s.user_id = #{userId}
              AND s.deleted = 0
              AND r.status = #{status}
            GROUP BY s.problem_id
            ORDER BY accepted_at DESC
            """)
    List<SolvedProblemVO> listUserAcceptedProblems(@Param("userId") Long userId, @Param("status") Integer status);
}
