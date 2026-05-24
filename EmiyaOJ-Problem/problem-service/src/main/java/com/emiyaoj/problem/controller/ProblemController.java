package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ProblemQueryDTO;
import com.emiyaoj.problem.dto.ProblemSaveDTO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 * <p>
 * 微服务版本：通过网关转发的 X-User-Id 请求头获取当前操作用户 ID
 */
@Tag(name = "题目管理")
@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {

    private final ProblemService problemService;

    /**
     * 分页查询题目列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询题目列表")
    public ResponseResult<PageVO<ProblemVO>> list(ProblemQueryDTO queryDTO) {
        PageVO<ProblemVO> pageVO = problemService.queryProblemPage(queryDTO);
        return ResponseResult.success(pageVO);
    }

    /**
     * 查询题目详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询题目详情")
    public ResponseResult<ProblemVO> detail(@PathVariable Long id) {
        ProblemVO vo = problemService.getProblemDetail(id);
        if (vo == null) {
            return ResponseResult.fail(404, "题目不存在");
        }
        return ResponseResult.success(vo);
    }

    @GetMapping("/internal/batch")
    @Operation(summary = "内部批量查询公开题目")
    public ResponseResult<List<ProblemVO>> internalBatch(@RequestParam List<Long> ids) {
        return ResponseResult.success(problemService.listPublicProblemsByIds(ids));
    }

    /**
     * 新增题目
     * <p>
     * 通过 @RequestHeader("X-User-Id") 获取网关注入的当前用户 ID
     */
    @PostMapping
    @Operation(summary = "新增题目")
    public ResponseResult<Boolean> save(@RequestBody ProblemSaveDTO dto,
                                        @RequestHeader("X-User-Id") Long userId) {
        log.info("用户 {} 新增题目: {}", userId, dto.getTitle());
        boolean result = problemService.saveProblem(dto, userId);
        return ResponseResult.success(result);
    }

    /**
     * 更新题目
     */
    @PutMapping
    @Operation(summary = "更新题目")
    public ResponseResult<Boolean> update(@RequestBody ProblemSaveDTO dto,
                                          @RequestHeader("X-User-Id") Long userId) {
        log.info("用户 {} 更新题目: {}", userId, dto.getId());
        boolean result = problemService.updateProblem(dto, userId);
        return ResponseResult.success(result);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public ResponseResult<Boolean> delete(@PathVariable Long id,
                                          @RequestHeader("X-User-Id") Long userId) {
        log.info("用户 {} 删除题目: {}", userId, id);
        boolean result = problemService.deleteProblem(id);
        return ResponseResult.success(result);
    }
}
