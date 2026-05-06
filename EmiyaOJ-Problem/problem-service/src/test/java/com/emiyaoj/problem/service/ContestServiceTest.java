package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.api.SubmissionFeignClient;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.problem.domain.pojo.Contest;
import com.emiyaoj.problem.domain.pojo.ContestAdmin;
import com.emiyaoj.problem.domain.pojo.ContestProblem;
import com.emiyaoj.problem.domain.pojo.ContestRegistration;
import com.emiyaoj.problem.dto.ContestRankVO;
import com.emiyaoj.problem.dto.ContestSubmitCheckVO;
import com.emiyaoj.problem.mapper.ContestAdminMapper;
import com.emiyaoj.problem.mapper.ContestMapper;
import com.emiyaoj.problem.mapper.ContestProblemMapper;
import com.emiyaoj.problem.mapper.ContestRegistrationMapper;
import com.emiyaoj.problem.mapper.ProblemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock
    private ContestMapper contestMapper;
    @Mock
    private ContestProblemMapper contestProblemMapper;
    @Mock
    private ContestRegistrationMapper contestRegistrationMapper;
    @Mock
    private ContestAdminMapper contestAdminMapper;
    @Mock
    private ProblemMapper problemMapper;
    @Mock
    private ProblemService problemService;
    @Mock
    private SubmissionFeignClient submissionFeignClient;

    @InjectMocks
    private ContestService contestService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contestService, "baseMapper", contestMapper);
    }

    @Test
    void checkContestSubmitAllowsRegisteredActiveContestProblem() {
        Contest contest = activeContest();
        ContestProblem contestProblem = contestProblem();

        when(contestMapper.selectById(1L)).thenReturn(contest);
        when(contestRegistrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(contestProblemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(contestProblem);

        ContestSubmitCheckVO result = contestService.checkContestSubmit(1L, 10L, 100L);

        assertTrue(result.getAllowed());
        assertEquals(1000L, result.getContestProblemId());
    }

    @Test
    void checkContestSubmitRejectsUnregisteredUser() {
        when(contestMapper.selectById(1L)).thenReturn(activeContest());
        when(contestRegistrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        ContestSubmitCheckVO result = contestService.checkContestSubmit(1L, 10L, 100L);

        assertFalse(result.getAllowed());
        assertEquals("User is not registered for this contest", result.getMessage());
    }

    @Test
    void getRankHidesFrozenSubmissionsForNonAdmin() {
        Contest contest = activeFrozenContest();
        ContestRegistration registration = new ContestRegistration();
        registration.setContestId(1L);
        registration.setUserId(100L);

        SubmissionVO frozenSubmission = new SubmissionVO();
        frozenSubmission.setId(500L);
        frozenSubmission.setContestId(1L);
        frozenSubmission.setContestProblemId(1000L);
        frozenSubmission.setProblemId(10L);
        frozenSubmission.setUserId(100L);
        frozenSubmission.setStatus(2);
        frozenSubmission.setCreateTime(LocalDateTime.now().minusMinutes(10));

        when(contestMapper.selectById(1L)).thenReturn(contest);
        when(contestAdminMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(contestRegistrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(registration));
        when(contestProblemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(contestProblem()));
        when(submissionFeignClient.listContestSubmissions(1L))
                .thenReturn(ResponseResult.success(List.of(frozenSubmission)));

        ContestRankVO rank = contestService.getRank(1L, 100L);

        assertTrue(rank.getFrozen());
        assertEquals(0, rank.getRankings().getFirst().getSolvedCount());
    }

    private Contest activeContest() {
        Contest contest = new Contest();
        contest.setId(1L);
        contest.setRuleType(1);
        contest.setStatus(1);
        contest.setCreatorId(999L);
        contest.setStartTime(LocalDateTime.now().minusHours(1));
        contest.setEndTime(LocalDateTime.now().plusHours(1));
        contest.setFreezeBeforeMinutes(60);
        contest.setInviteCode("Ab3!Cd4?Ef");
        return contest;
    }

    private Contest activeFrozenContest() {
        Contest contest = activeContest();
        contest.setStartTime(LocalDateTime.now().minusHours(3));
        contest.setEndTime(LocalDateTime.now().plusMinutes(30));
        contest.setFreezeBeforeMinutes(60);
        return contest;
    }

    private ContestProblem contestProblem() {
        ContestProblem problem = new ContestProblem();
        problem.setId(1000L);
        problem.setContestId(1L);
        problem.setProblemId(10L);
        problem.setLabel("A");
        problem.setSortOrder(1);
        problem.setScore(100);
        return problem;
    }
}
