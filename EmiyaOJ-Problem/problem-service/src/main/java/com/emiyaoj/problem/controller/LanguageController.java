package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 编程语言控制器
 */
@Tag(name = "编程语言管理")
@RestController
@RequestMapping("/language")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    /**
     * 根据 ID 查询语言详情（供 Feign 调用）
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询语言详情")
    public ResponseResult<LanguageVO> getById(@PathVariable Long id) {
        LanguageVO vo = languageService.getLanguageDetail(id);
        if (vo == null) {
            return ResponseResult.fail(404, "语言不存在或已禁用");
        }
        return ResponseResult.success(vo);
    }
}
