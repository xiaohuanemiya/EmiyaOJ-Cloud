package com.emiyaoj.problem.controller;

import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ContestAdminAssignDTO;
import com.emiyaoj.problem.dto.ContestProblemDTO;
import com.emiyaoj.problem.dto.ContestQueryDTO;
import com.emiyaoj.problem.dto.ContestRankVO;
import com.emiyaoj.problem.dto.ContestRegisterDTO;
import com.emiyaoj.problem.dto.ContestRegistrationVO;
import com.emiyaoj.problem.dto.ContestSaveDTO;
import com.emiyaoj.problem.dto.ContestSubmitCheckVO;
import com.emiyaoj.problem.dto.ContestVO;
import com.emiyaoj.problem.service.ContestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Contest Management")
@RestController
@RequestMapping("/contest")
@RequiredArgsConstructor
@Slf4j
public class ContestController {

    private final ContestService contestService;

    @GetMapping("/list")
    @Operation(summary = "Query contests")
    public ResponseResult<PageVO<ContestVO>> list(
            ContestQueryDTO queryDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseResult.success(contestService.queryContestPage(queryDTO, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contest detail")
    public ResponseResult<ContestVO> detail(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ContestVO vo = contestService.getContestDetail(id, userId);
        if (vo == null) {
            return ResponseResult.fail(404, "Contest does not exist");
        }
        return ResponseResult.success(vo);
    }

    @PostMapping
    @Operation(summary = "Create contest")
    public ResponseResult<ContestVO> save(@RequestBody ContestSaveDTO dto,
                                          @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("User {} creates contest {}", userId, dto.getTitle());
        return ResponseResult.success(contestService.saveContest(dto, userId));
    }

    @PutMapping
    @Operation(summary = "Update contest")
    public ResponseResult<Boolean> update(@RequestBody ContestSaveDTO dto,
                                          @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.updateContest(dto, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete contest")
    public ResponseResult<Boolean> delete(@PathVariable Long id,
                                          @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.deleteContest(id, userId));
    }

    @PostMapping("/{id}/register")
    @Operation(summary = "Register contest")
    public ResponseResult<Boolean> register(@PathVariable Long id,
                                            @RequestBody ContestRegisterDTO dto,
                                            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.registerContest(id, dto, userId));
    }

    @DeleteMapping("/{id}/register")
    @Operation(summary = "Cancel current user registration")
    public ResponseResult<Boolean> cancelRegistration(@PathVariable Long id,
                                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.cancelRegistration(id, userId));
    }

    @GetMapping("/{id}/registrations")
    @Operation(summary = "List contest registrations")
    public ResponseResult<List<ContestRegistrationVO>> registrations(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.listRegistrations(id, userId));
    }

    @DeleteMapping("/{id}/registrations/{userId}")
    @Operation(summary = "Remove contest registration")
    public ResponseResult<Boolean> removeRegistration(@PathVariable Long id,
                                                      @PathVariable Long userId,
                                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long operatorId) {
        return ResponseResult.success(contestService.removeRegistration(id, userId, operatorId));
    }

    @PutMapping("/{id}/problems")
    @Operation(summary = "Replace contest problems")
    public ResponseResult<Boolean> replaceProblems(@PathVariable Long id,
                                                   @RequestBody List<ContestProblemDTO> problems,
                                                   @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.replaceContestProblems(id, problems, userId));
    }

    @GetMapping("/admin-candidates")
    @Operation(summary = "List contest admin candidates")
    public ResponseResult<List<UserVO>> adminCandidates() {
        return ResponseResult.success(contestService.listAdminCandidates());
    }

    @PutMapping("/{id}/admins")
    @Operation(summary = "Replace contest admins")
    public ResponseResult<Boolean> replaceAdmins(@PathVariable Long id,
                                                 @RequestBody ContestAdminAssignDTO dto,
                                                 @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.replaceAdmins(id, dto, userId));
    }

    @GetMapping("/{id}/rank")
    @Operation(summary = "Get contest rank")
    public ResponseResult<ContestRankVO> rank(@PathVariable Long id,
                                              @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(contestService.getRank(id, userId));
    }

    @GetMapping("/internal/{id}/submit-check")
    @Operation(summary = "Check contest submission")
    public ResponseResult<ContestSubmitCheckVO> submitCheck(@PathVariable Long id,
                                                            @RequestParam Long problemId,
                                                            @RequestParam Long userId) {
        return ResponseResult.success(contestService.checkContestSubmit(id, problemId, userId));
    }
}
