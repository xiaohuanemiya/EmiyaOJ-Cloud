package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ProblemSetProblemDTO;
import com.emiyaoj.problem.dto.ProblemSetQueryDTO;
import com.emiyaoj.problem.dto.ProblemSetSaveDTO;
import com.emiyaoj.problem.dto.ProblemSetVO;
import com.emiyaoj.problem.service.ProblemSetService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Problem Set Management")
@RestController
@RequestMapping("/problem-set")
@RequiredArgsConstructor
@Slf4j
public class ProblemSetController {

    private final ProblemSetService problemSetService;

    @GetMapping("/list")
    @Operation(summary = "Query problem sets")
    public ResponseResult<PageVO<ProblemSetVO>> list(
            ProblemSetQueryDTO queryDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseResult.success(problemSetService.queryProblemSetPage(queryDTO, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get problem set detail")
    public ResponseResult<ProblemSetVO> detail(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ProblemSetVO vo = problemSetService.getProblemSetDetail(id, userId);
        if (vo == null) {
            return ResponseResult.fail(404, "Problem set does not exist");
        }
        return ResponseResult.success(vo);
    }

    @PostMapping
    @Operation(summary = "Create problem set")
    public ResponseResult<ProblemSetVO> save(@RequestBody ProblemSetSaveDTO dto,
                                             @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("User {} creates problem set {}", userId, dto.getTitle());
        return ResponseResult.success(problemSetService.saveProblemSet(dto, userId));
    }

    @PutMapping
    @Operation(summary = "Update problem set")
    public ResponseResult<Boolean> update(@RequestBody ProblemSetSaveDTO dto,
                                          @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(problemSetService.updateProblemSet(dto, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete problem set")
    public ResponseResult<Boolean> delete(@PathVariable Long id,
                                          @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(problemSetService.deleteProblemSet(id, userId));
    }

    @PutMapping("/{id}/problems")
    @Operation(summary = "Replace problem set problems")
    public ResponseResult<Boolean> replaceProblems(@PathVariable Long id,
                                                   @RequestBody List<ProblemSetProblemDTO> problems,
                                                   @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(problemSetService.replaceProblems(id, problems, userId));
    }

    @PostMapping("/{id}/problems")
    @Operation(summary = "Append problem set problems")
    public ResponseResult<Boolean> appendProblems(@PathVariable Long id,
                                                  @RequestBody List<ProblemSetProblemDTO> problems,
                                                  @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(problemSetService.appendProblems(id, problems, userId));
    }

    @DeleteMapping("/{id}/problems/{problemId}")
    @Operation(summary = "Remove a problem from problem set")
    public ResponseResult<Boolean> removeProblem(@PathVariable Long id,
                                                 @PathVariable Long problemId,
                                                 @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(problemSetService.removeProblem(id, problemId, userId));
    }
}
