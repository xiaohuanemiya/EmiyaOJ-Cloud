package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.JudgeFeedbackVO;
import com.emiyaoj.judge.dto.JudgeUserStatsVO;
import com.emiyaoj.judge.dto.SolvedProblemVO;
import com.emiyaoj.judge.dto.SubmissionDetailVO;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Submission Management")
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "Get submission detail")
    @GetMapping("/{id}")
    public ResponseResult<SubmissionDetailVO> getSubmissionById(
            @Parameter(description = "Submission ID") @PathVariable Long id) {
        SubmissionDetailVO vo = submissionService.getSubmissionById(id);
        if (vo == null) {
            return ResponseResult.fail(404, "Submission does not exist");
        }
        return ResponseResult.success(vo);
    }

    @Operation(summary = "Get submission AI feedback")
    @GetMapping("/{id}/feedback")
    public ResponseResult<JudgeFeedbackVO> getSubmissionFeedback(
            @Parameter(description = "Submission ID") @PathVariable Long id) {
        JudgeFeedbackVO feedback = submissionService.getFeedbackBySubmissionId(id);
        return ResponseResult.success(feedback);
    }

    @Operation(summary = "Query submissions")
    @GetMapping("/page")
    public ResponseResult<PageVO<SubmissionVO>> getSubmissionPage(
            PageDTO pageDTO,
            @Parameter(description = "Problem ID") @RequestParam(required = false) Long problemId,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "Query current user submissions")
    @GetMapping("/my")
    public ResponseResult<PageVO<SubmissionVO>> getMySubmissions(
            PageDTO pageDTO,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Problem ID") @RequestParam(required = false) Long problemId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "List contest submissions")
    @GetMapping("/contest/{contestId}")
    public ResponseResult<List<SubmissionVO>> listContestSubmissions(@PathVariable Long contestId) {
        return ResponseResult.success(submissionService.listContestSubmissions(contestId));
    }

    @Operation(summary = "Get user judge statistics")
    @GetMapping("/user/{userId}/stats")
    public ResponseResult<JudgeUserStatsVO> getUserStats(@PathVariable Long userId) {
        return ResponseResult.success(submissionService.getUserStats(userId));
    }

    @Operation(summary = "Query user solved problems")
    @GetMapping("/user/{userId}/solved")
    public ResponseResult<PageVO<SolvedProblemVO>> getSolvedProblems(
            @PathVariable Long userId,
            PageDTO pageDTO,
            @Parameter(description = "Difficulty: 1 easy, 2 medium, 3 hard")
            @RequestParam(required = false) Integer difficulty) {
        return ResponseResult.success(submissionService.getSolvedProblems(pageDTO, userId, difficulty));
    }
}
