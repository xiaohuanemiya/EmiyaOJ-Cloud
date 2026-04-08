package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.problem.domain.pojo.TestCase;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.mapper.TestCaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例服务
 */
@Slf4j
@Service
public class TestCaseService extends ServiceImpl<TestCaseMapper, TestCase> {

    /**
     * 根据题目 ID 查询所有测试用例
     */
    public List<TestCaseVO> getByProblemId(Long problemId) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProblemId, problemId)
               .orderByAsc(TestCase::getSortOrder);
        List<TestCase> testCases = this.list(wrapper);
        return testCases.stream().map(tc -> {
            TestCaseVO vo = new TestCaseVO();
            BeanUtils.copyProperties(tc, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
