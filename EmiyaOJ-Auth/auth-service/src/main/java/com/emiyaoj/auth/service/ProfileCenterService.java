package com.emiyaoj.auth.service;

import com.emiyaoj.auth.vo.ProfileCenterVO;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.blog.api.BlogFeignClient;
import com.emiyaoj.blog.vo.BlogUserStatsVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.api.SubmissionFeignClient;
import com.emiyaoj.judge.dto.JudgeUserStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileCenterService {

    private final IUserService userService;
    private final SubmissionFeignClient submissionFeignClient;
    private final BlogFeignClient blogFeignClient;

    public ProfileCenterVO getProfileCenter(Long userId) {
        UserVO user = userService.selectUserById(userId);
        if (user == null) {
            return null;
        }

        JudgeUserStatsVO judgeStats = loadJudgeStats(userId);
        BlogUserStatsVO blogStats = loadBlogStats(userId);

        ProfileCenterVO vo = new ProfileCenterVO();
        vo.setUser(new ProfileCenterVO.PublicUserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getCreateTime()));
        vo.setSolvedCount(judgeStats.getSolvedCount());
        vo.setTotalSubmitCount(judgeStats.getTotalSubmitCount());
        vo.setAcceptedSubmitCount(judgeStats.getAcceptedSubmitCount());
        vo.setPassRate(judgeStats.getPassRate());
        vo.setDifficultyStats(judgeStats.getDifficultyStats().stream()
                .map(stat -> new ProfileCenterVO.DifficultySolvedStatsVO(
                        stat.getDifficulty(),
                        stat.getDifficultyDesc(),
                        stat.getSolvedCount()))
                .toList());
        vo.setBlogCount(blogStats.getBlogCount());
        vo.setStarCount(blogStats.getStarCount());
        vo.setLikedBlogCount(blogStats.getLikedBlogCount());
        return vo;
    }

    private JudgeUserStatsVO loadJudgeStats(Long userId) {
        try {
            ResponseResult<JudgeUserStatsVO> result = submissionFeignClient.getUserStats(userId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("load judge stats failed, userId={}", userId, e);
        }
        JudgeUserStatsVO fallback = new JudgeUserStatsVO();
        fallback.setUserId(userId);
        fallback.setSolvedCount(0);
        fallback.setTotalSubmitCount(0);
        fallback.setAcceptedSubmitCount(0);
        fallback.setPassRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        fallback.setDifficultyStats(List.of(
                new com.emiyaoj.judge.dto.DifficultySolvedStatsVO(1, "简单", 0),
                new com.emiyaoj.judge.dto.DifficultySolvedStatsVO(2, "中等", 0),
                new com.emiyaoj.judge.dto.DifficultySolvedStatsVO(3, "困难", 0)
        ));
        return fallback;
    }

    private BlogUserStatsVO loadBlogStats(Long userId) {
        try {
            ResponseResult<BlogUserStatsVO> result = blogFeignClient.getUserStats(userId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("load blog stats failed, userId={}", userId, e);
        }
        return new BlogUserStatsVO(userId, 0, 0, 0);
    }
}
