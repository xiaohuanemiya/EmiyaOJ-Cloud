package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.common.exception.BadRequestException;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.problem.domain.pojo.Language;
import com.emiyaoj.problem.dto.LanguageSaveDTO;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.mapper.LanguageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 编程语言配置服务。
 */
@Slf4j
@Service
public class LanguageService extends ServiceImpl<LanguageMapper, Language> {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("[A-Za-z0-9_.-]+");
    private static final Pattern SAFE_EXTENSION = Pattern.compile("[A-Za-z0-9]+");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Set<String> ALLOWED_PLACEHOLDERS = Set.of(
            "LanguageVersion", "CompileFileName", "SourceFileName", "ExecutableFileName"
    );
    private static final BigDecimal DEFAULT_MULTIPLIER = BigDecimal.ONE;

    /**
     * 获取语言详情，供 Feign 调用，仅返回启用语言。
     */
    public LanguageVO getLanguageDetail(Long id) {
        Language language = this.getById(id);
        if (language == null || language.getStatus() == 0) {
            return null;
        }
        return toVO(language);
    }

    /**
     * 查询所有启用的编程语言。
     */
    public List<LanguageVO> listEnabled() {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Language::getStatus, 1)
                .orderByAsc(Language::getId);
        return this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 管理端查询所有编程语言，包含禁用语言。
     */
    public List<LanguageVO> listAll() {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Language::getId);
        return this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 管理端根据 ID 查询语言详情，不过滤状态。
     */
    public LanguageVO getAdminById(Long id) {
        Language language = this.getById(id);
        return language == null ? null : toVO(language);
    }

    /**
     * 新增编程语言配置。
     */
    public LanguageVO saveLanguage(LanguageSaveDTO dto) {
        normalizeDefaults(dto);
        validateSaveDTO(dto, false);
        checkDuplicateNameVersion(dto, null);

        Language language = new Language();
        BeanUtils.copyProperties(dto, language);
        language.setCreateTime(LocalDateTime.now());
        language.setUpdateTime(LocalDateTime.now());
        this.save(language);
        return toVO(language);
    }

    /**
     * 更新编程语言配置。
     */
    public boolean updateLanguage(LanguageSaveDTO dto) {
        normalizeDefaults(dto);
        validateSaveDTO(dto, true);
        Language language = this.getById(dto.getId());
        if (language == null) {
            throw new BaseException(404, "语言不存在");
        }
        checkDuplicateNameVersion(dto, dto.getId());

        BeanUtils.copyProperties(dto, language);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 启用编程语言。
     */
    public boolean enableLanguage(Long id) {
        Language language = this.getById(id);
        if (language == null) {
            throw new BaseException(404, "语言不存在");
        }
        language.setStatus(1);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 禁用编程语言。
     */
    public boolean disableLanguage(Long id) {
        Language language = this.getById(id);
        if (language == null) {
            throw new BaseException(404, "语言不存在");
        }
        language.setStatus(0);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 物理删除编程语言。
     */
    public boolean deleteLanguage(Long id) {
        if (this.getById(id) == null) {
            throw new BaseException(404, "语言不存在");
        }
        return this.removeById(id);
    }

    private void normalizeDefaults(LanguageSaveDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getStatus() == null) {
            dto.setStatus(1);
        }
        if (dto.getIsCompiled() == null) {
            dto.setIsCompiled(1);
        }
        if (dto.getCompileFileName() == null) {
            dto.setCompileFileName("main");
        }
        if (dto.getExecutableFileName() == null) {
            dto.setExecutableFileName("main");
        }
        if (dto.getEnvVars() == null) {
            dto.setEnvVars("PATH=/usr/bin:/bin");
        }
        if (dto.getTimeLimitMultiplier() == null) {
            dto.setTimeLimitMultiplier(DEFAULT_MULTIPLIER);
        }
        if (dto.getMemoryLimitMultiplier() == null) {
            dto.setMemoryLimitMultiplier(DEFAULT_MULTIPLIER);
        }
        if (dto.getCompileTimeLimit() == null) {
            dto.setCompileTimeLimit(10000);
        }
        if (dto.getCompileMemoryLimit() == null) {
            dto.setCompileMemoryLimit(512);
        }
        if (dto.getCompileProcLimit() == null) {
            dto.setCompileProcLimit(50);
        }
        if (dto.getRunProcLimit() == null) {
            dto.setRunProcLimit(1);
        }
    }

    private void validateSaveDTO(LanguageSaveDTO dto, boolean requireId) {
        if (dto == null) {
            throw new BadRequestException("请求体不能为空");
        }
        if (requireId && dto.getId() == null) {
            throw new BadRequestException("语言ID不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BadRequestException("语言名称不能为空");
        }
        if (!StringUtils.hasText(dto.getVersion())) {
            throw new BadRequestException("展示版本不能为空");
        }
        if (!StringUtils.hasText(dto.getLanguageVersion())) {
            throw new BadRequestException("语言版本不能为空");
        }
        if (!StringUtils.hasText(dto.getSourceFileExt())) {
            throw new BadRequestException("源文件扩展名不能为空");
        }
        if (!StringUtils.hasText(dto.getCompileFileName())) {
            throw new BadRequestException("源文件基础名不能为空");
        }
        if (!StringUtils.hasText(dto.getExecutableFileName())) {
            throw new BadRequestException("可执行目标名不能为空");
        }
        if (!StringUtils.hasText(dto.getRunCommand())) {
            throw new BadRequestException("运行命令不能为空");
        }
        if (dto.getIsCompiled() == 1 && !StringUtils.hasText(dto.getCompileCommand())) {
            throw new BadRequestException("编译型语言必须配置编译命令");
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BadRequestException("状态只能为0或1");
        }
        if (dto.getIsCompiled() != 0 && dto.getIsCompiled() != 1) {
            throw new BadRequestException("是否需要编译只能为0或1");
        }
        validateSafeName("源文件基础名", dto.getCompileFileName());
        validateSafeName("可执行目标名", dto.getExecutableFileName());
        validateLanguageVersion(dto.getLanguageVersion());
        validateSafeExtension(dto.getSourceFileExt());
        validateCompiledFileNames(dto.getCompiledFileNames());
        validatePositive("运行 CPU 时间限制倍数", dto.getTimeLimitMultiplier());
        validatePositive("运行内存限制倍数", dto.getMemoryLimitMultiplier());
        validatePositiveInt("编译 CPU 时间限制", dto.getCompileTimeLimit());
        validatePositiveInt("编译内存限制", dto.getCompileMemoryLimit());
        validatePositiveInt("编译进程数限制", dto.getCompileProcLimit());
        validatePositiveInt("运行进程数限制", dto.getRunProcLimit());
        validateCommandTemplate("编译命令", dto.getCompileCommand(), dto.getIsCompiled() == 1);
        validateCommandTemplate("运行命令", dto.getRunCommand(), true);

        if (dto.getIsCompiled() == 1 && !referencesSourceFile(dto.getCompileCommand())) {
            throw new BadRequestException("编译命令必须引用 {CompileFileName} 或 {SourceFileName}");
        }
        if (!referencesRunnableTarget(dto.getRunCommand())) {
            throw new BadRequestException("运行命令必须引用 {ExecutableFileName}、{CompileFileName} 或 {SourceFileName}");
        }
    }

    private void validateSafeName(String label, String value) {
        if (!SAFE_FILE_NAME.matcher(value).matches() || value.contains("..")) {
            throw new BadRequestException(label + "只能包含字母、数字、下划线、点和短横线，且不能包含路径");
        }
    }

    private void validateSafeExtension(String extension) {
        if (!SAFE_EXTENSION.matcher(extension).matches()) {
            throw new BadRequestException("源文件扩展名只能包含字母和数字");
        }
    }

    private void validateLanguageVersion(String languageVersion) {
        if (!Pattern.matches("[A-Za-z0-9+_.-]+", languageVersion)) {
            throw new BadRequestException("语言版本只能包含字母、数字、加号、下划线、点和短横线");
        }
    }

    private void validateCompiledFileNames(String compiledFileNames) {
        if (!StringUtils.hasText(compiledFileNames)) {
            return;
        }
        for (String fileName : compiledFileNames.split(",")) {
            String trimmed = fileName.trim();
            if (!StringUtils.hasText(trimmed)) {
                throw new BadRequestException("编译产物文件名不能为空");
            }
            validateSafeName("编译产物文件名", trimmed);
        }
    }

    private void validatePositive(String label, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(label + "必须大于0");
        }
    }

    private void validatePositiveInt(String label, Integer value) {
        if (value == null || value <= 0) {
            throw new BadRequestException(label + "必须大于0");
        }
    }

    private void validateCommandTemplate(String label, String command, boolean required) {
        if (!StringUtils.hasText(command)) {
            if (required) {
                throw new BadRequestException(label + "不能为空");
            }
            return;
        }
        if (command.contains("\n") || command.contains("\r")) {
            throw new BadRequestException(label + "不能包含换行");
        }
        if (command.contains(";") || command.contains("|") || command.contains("&&")
                || command.contains("||") || command.contains("`") || command.contains("$(")) {
            throw new BadRequestException(label + "不能包含 shell 控制符");
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(command);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!ALLOWED_PLACEHOLDERS.contains(placeholder)) {
                throw new BadRequestException(label + "包含不支持的占位符: {" + placeholder + "}");
            }
        }
    }

    private boolean referencesSourceFile(String command) {
        return command.contains("{CompileFileName}") || command.contains("{SourceFileName}");
    }

    private boolean referencesRunnableTarget(String command) {
        return command.contains("{ExecutableFileName}")
                || command.contains("{CompileFileName}")
                || command.contains("{SourceFileName}");
    }

    private void checkDuplicateNameVersion(LanguageSaveDTO dto, Long excludeId) {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<Language>()
                .eq(Language::getName, dto.getName())
                .eq(Language::getVersion, dto.getVersion())
                .ne(excludeId != null, Language::getId, excludeId);
        if (this.count(wrapper) > 0) {
            throw new BadRequestException("同名同版本的语言已存在");
        }
    }

    private LanguageVO toVO(Language language) {
        LanguageVO vo = new LanguageVO();
        BeanUtils.copyProperties(language, vo);
        return vo;
    }
}
