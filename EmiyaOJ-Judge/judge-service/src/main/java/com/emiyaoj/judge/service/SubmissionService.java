package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.ProblemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交 & 判题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final JudgeExecutor judgeExecutor;
    private final ProblemFeignClient problemFeignClient;

    /**
     * 提交代码 - 创建提交记录后异步执行判题
     */
    public SubmissionVO submitCode(SubmitCodeDTO dto, Long userId) {
        // 1. 远程调用获取题目信息
        ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(dto.getProblemId());
        if (problemResult == null || problemResult.getCode() != 200 || problemResult.getData() == null) {
            throw new BaseException(400, "题目不存在");
        }

        // 2. 远程调用获取语言信息
        ResponseResult<LanguageVO> languageResult = problemFeignClient.getLanguageById(dto.getLanguageId());
        if (languageResult == null || languageResult.getCode() != 200 || languageResult.getData() == null) {
            throw new BaseException(400, "编程语言不存在");
        }

        // 3. 创建提交记录
        Submission submission = new Submission();
        submission.setProblemId(dto.getProblemId());
        submission.setUserId(userId);
        submission.setLanguageId(dto.getLanguageId());
        submission.setCode(dto.getCode());
        submission.setStatus(0); // 待判题
        submission.setScore(0);
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        submission.setDeleted(0);
        submissionMapper.insert(submission);

        log.info("Submission created: id={}", submission.getId());

        // 4. 异步执行判题 (通过独立 Bean 调用, 确保 @Async 代理生效)
        judgeExecutor.executeJudgeAsync(submission.getId(), dto.getProblemId(), dto.getLanguageId(), dto.getCode());

        // 5. 立即返回提交记录 (前端可轮询查询判题结果)
        SubmissionVO vo = new SubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 查询提交记录详情
     */
    public SubmissionVO getSubmissionById(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }
        SubmissionVO vo = new SubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 分页查询提交记录
     */
    public PageVO<SubmissionVO> getSubmissionPage(PageDTO pageDTO, Long problemId, Long userId) {
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        if (problemId != null) {
            wrapper.eq(Submission::getProblemId, problemId);
        }
        if (userId != null) {
            wrapper.eq(Submission::getUserId, userId);
        }
        wrapper.orderByDesc(Submission::getCreateTime);

        Page<Submission> page = submissionMapper.selectPage(
                new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()),
                wrapper
        );

        List<SubmissionVO> voList = page.getRecords().stream().map(s -> {
            SubmissionVO vo = new SubmissionVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).toList();

        return new PageVO<>(page.getTotal(), voList, (long) pageDTO.getPageNum(), (long) pageDTO.getPageSize());
    }
}

