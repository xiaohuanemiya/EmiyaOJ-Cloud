package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.LanguageSaveDTO;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 编程语言配置控制器。
 */
@Tag(name = "编程语言管理")
@RestController
@RequestMapping("/language")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping("/list")
    @Operation(summary = "查询启用的编程语言列表")
    public ResponseResult<List<LanguageVO>> listEnabled() {
        return ResponseResult.success(languageService.listEnabled());
    }

    @GetMapping("/admin/list")
    @Operation(summary = "管理端查询全部编程语言")
    public ResponseResult<List<LanguageVO>> listAll() {
        return ResponseResult.success(languageService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询启用的编程语言详情")
    public ResponseResult<LanguageVO> getById(@PathVariable Long id) {
        LanguageVO vo = languageService.getLanguageDetail(id);
        if (vo == null) {
            return ResponseResult.fail(404, "语言不存在或已禁用");
        }
        return ResponseResult.success(vo);
    }

    @GetMapping("/admin/{id}")
    @Operation(summary = "管理端查询编程语言详情")
    public ResponseResult<LanguageVO> getAdminById(@PathVariable Long id) {
        LanguageVO vo = languageService.getAdminById(id);
        if (vo == null) {
            return ResponseResult.fail(404, "语言不存在");
        }
        return ResponseResult.success(vo);
    }

    @PostMapping
    @Operation(summary = "新增编程语言")
    public ResponseResult<LanguageVO> save(@RequestBody LanguageSaveDTO dto) {
        LanguageVO vo = languageService.saveLanguage(dto);
        return ResponseResult.success(vo);
    }

    @PutMapping
    @Operation(summary = "更新编程语言")
    public ResponseResult<Boolean> update(@RequestBody LanguageSaveDTO dto) {
        boolean result = languageService.updateLanguage(dto);
        return result ? ResponseResult.success(true) : ResponseResult.fail("更新失败");
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用编程语言")
    public ResponseResult<Boolean> enable(@PathVariable Long id) {
        boolean result = languageService.enableLanguage(id);
        return result ? ResponseResult.success(true) : ResponseResult.fail("启用失败");
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用编程语言")
    public ResponseResult<Boolean> disable(@PathVariable Long id) {
        boolean result = languageService.disableLanguage(id);
        return result ? ResponseResult.success(true) : ResponseResult.fail("禁用失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除编程语言")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        boolean result = languageService.deleteLanguage(id);
        return result ? ResponseResult.success(true) : ResponseResult.fail("删除失败");
    }
}
