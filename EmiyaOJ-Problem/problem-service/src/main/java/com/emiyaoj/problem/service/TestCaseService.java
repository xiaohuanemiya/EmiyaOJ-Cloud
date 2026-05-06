package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.problem.domain.pojo.TestCase;
import com.emiyaoj.problem.dto.TestCaseSaveDTO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.mapper.TestCaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * 根据 ID 查询单个测试用例
     */
    public TestCaseVO getTestCaseById(Long id) {
        TestCase tc = this.getById(id);
        if (tc == null) {
            return null;
        }
        TestCaseVO vo = new TestCaseVO();
        BeanUtils.copyProperties(tc, vo);
        return vo;
    }

    /**
     * 新增单个测试用例
     */
    public TestCaseVO saveTestCase(TestCaseSaveDTO dto) {
        TestCase tc = new TestCase();
        BeanUtils.copyProperties(dto, tc);
        tc.setCreateTime(LocalDateTime.now());
        tc.setUpdateTime(LocalDateTime.now());
        this.save(tc);
        TestCaseVO vo = new TestCaseVO();
        BeanUtils.copyProperties(tc, vo);
        return vo;
    }

    /**
     * 批量新增测试用例（problemId 以路径参数为准）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TestCaseVO> batchSaveTestCases(Long problemId, List<TestCaseSaveDTO> dtos) {
        List<TestCaseVO> result = new ArrayList<>();
        for (TestCaseSaveDTO dto : dtos) {
            dto.setProblemId(problemId);
            result.add(saveTestCase(dto));
        }
        return result;
    }

    /**
     * 更新测试用例
     */
    public boolean updateTestCase(TestCaseSaveDTO dto) {
        TestCase tc = this.getById(dto.getId());
        if (tc == null) {
            throw new RuntimeException("测试用例不存在");
        }
        BeanUtils.copyProperties(dto, tc);
        tc.setUpdateTime(LocalDateTime.now());
        return this.updateById(tc);
    }

    /**
     * 删除单个测试用例（逻辑删除）
     */
    public boolean deleteTestCaseById(Long id) {
        return this.removeById(id);
    }

    /**
     * 批量删除测试用例（逻辑删除）
     */
    public boolean batchDeleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return this.getBaseMapper().deleteBatchIds(ids) > 0;
    }

    /**
     * 删除题目下所有测试用例（逻辑删除）
     */
    public boolean deleteByProblemId(Long problemId) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProblemId, problemId);
        return this.remove(wrapper);
    }
}
