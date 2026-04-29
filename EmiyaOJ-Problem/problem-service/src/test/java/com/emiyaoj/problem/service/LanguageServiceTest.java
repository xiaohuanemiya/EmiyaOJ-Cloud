package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.common.exception.BadRequestException;
import com.emiyaoj.problem.domain.pojo.Language;
import com.emiyaoj.problem.dto.LanguageSaveDTO;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.mapper.LanguageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 编程语言配置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageService languageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(languageService, "baseMapper", languageMapper);
    }

    @Test
    void getLanguageDetailReturnsEnabledLanguage() {
        when(languageMapper.selectById(1L)).thenReturn(buildLanguage(1L, "C++", "C++20", 1));

        LanguageVO vo = languageService.getLanguageDetail(1L);

        assertNotNull(vo);
        assertEquals("C++", vo.getName());
        assertEquals("c++20", vo.getLanguageVersion());
    }

    @Test
    void getLanguageDetailReturnsNullWhenDisabled() {
        when(languageMapper.selectById(2L)).thenReturn(buildLanguage(2L, "C++", "C++14", 0));

        assertNull(languageService.getLanguageDetail(2L));
    }

    @Test
    void listEnabledMapsLanguageConfigFields() {
        when(languageMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(buildLanguage(1L, "C++", "C++20", 1)));

        List<LanguageVO> result = languageService.listEnabled();

        assertEquals(1, result.size());
        assertEquals("main", result.get(0).getCompileFileName());
        assertEquals("./{ExecutableFileName}", result.get(0).getRunCommand());
    }

    @Test
    void saveLanguageSetsDefaultsAndPersistsValidConfig() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.insert(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = validCppDto();
        dto.setEnvVars(null);
        dto.setCompileTimeLimit(null);
        dto.setRunProcLimit(null);

        LanguageVO vo = languageService.saveLanguage(dto);

        assertNotNull(vo);
        assertEquals("PATH=/usr/bin:/bin", vo.getEnvVars());
        assertEquals(10000, vo.getCompileTimeLimit());
        assertEquals(1, vo.getRunProcLimit());
        verify(languageMapper, times(1)).insert(any(Language.class));
    }

    @Test
    void saveLanguageRejectsUnsupportedPlaceholder() {
        LanguageSaveDTO dto = validCppDto();
        dto.setCompileCommand("/usr/bin/g++ {BadPlaceholder} main.cpp");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> languageService.saveLanguage(dto));

        assertEquals("编译命令包含不支持的占位符: {BadPlaceholder}", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    @Test
    void saveLanguageRejectsShellControlOperators() {
        LanguageSaveDTO dto = validCppDto();
        dto.setRunCommand("./{ExecutableFileName}; rm -rf /");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> languageService.saveLanguage(dto));

        assertEquals("运行命令不能包含 shell 控制符", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    @Test
    void saveLanguageRejectsCompiledLanguageWithoutCompileCommand() {
        LanguageSaveDTO dto = validCppDto();
        dto.setCompileCommand(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> languageService.saveLanguage(dto));

        assertEquals("编译型语言必须配置编译命令", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    @Test
    void saveLanguageAllowsInterpretedLanguageWithoutCompileCommand() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.insert(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = validPythonDto();

        LanguageVO vo = languageService.saveLanguage(dto);

        assertEquals(0, vo.getIsCompiled());
        assertNull(vo.getCompileCommand());
        verify(languageMapper, times(1)).insert(any(Language.class));
    }

    @Test
    void saveLanguageRejectsDuplicateNameVersion() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> languageService.saveLanguage(validCppDto()));

        assertEquals("同名同版本的语言已存在", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    @Test
    void updateLanguageUpdatesExistingConfig() {
        Language existing = buildLanguage(1L, "C++", "C++20", 1);
        when(languageMapper.selectById(1L)).thenReturn(existing);
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.updateById(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = validCppDto();
        dto.setId(1L);
        dto.setVersion("C++23");
        dto.setLanguageVersion("c++23");

        assertTrue(languageService.updateLanguage(dto));
        verify(languageMapper, times(1)).updateById(any(Language.class));
    }

    private LanguageSaveDTO validCppDto() {
        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("C++");
        dto.setVersion("C++20");
        dto.setLanguageVersion("c++20");
        dto.setCompileFileName("main");
        dto.setSourceFileExt("cpp");
        dto.setExecutableFileName("main");
        dto.setCompiledFileNames("main");
        dto.setCompileCommand("/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp");
        dto.setRunCommand("./{ExecutableFileName}");
        dto.setEnvVars("PATH=/usr/bin:/bin");
        dto.setIsCompiled(1);
        dto.setTimeLimitMultiplier(BigDecimal.ONE);
        dto.setMemoryLimitMultiplier(BigDecimal.ONE);
        dto.setCompileTimeLimit(10000);
        dto.setCompileMemoryLimit(512);
        dto.setCompileProcLimit(50);
        dto.setRunProcLimit(1);
        dto.setStatus(1);
        return dto;
    }

    private LanguageSaveDTO validPythonDto() {
        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("Python3");
        dto.setVersion("Python 3.12");
        dto.setLanguageVersion("3.12");
        dto.setCompileFileName("main");
        dto.setSourceFileExt("py");
        dto.setExecutableFileName("main.py");
        dto.setCompileCommand(null);
        dto.setRunCommand("/usr/bin/python3 {SourceFileName}");
        dto.setIsCompiled(0);
        dto.setTimeLimitMultiplier(new BigDecimal("3.0"));
        dto.setMemoryLimitMultiplier(new BigDecimal("2.0"));
        return dto;
    }

    private Language buildLanguage(Long id, String name, String version, Integer status) {
        Language lang = new Language();
        lang.setId(id);
        lang.setName(name);
        lang.setVersion(version);
        lang.setLanguageVersion(version.startsWith("C++") ? "c++20" : version);
        lang.setCompileFileName("main");
        lang.setSourceFileExt("cpp");
        lang.setExecutableFileName("main");
        lang.setCompiledFileNames("main");
        lang.setCompileCommand("/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp");
        lang.setRunCommand("./{ExecutableFileName}");
        lang.setEnvVars("PATH=/usr/bin:/bin");
        lang.setStatus(status);
        lang.setIsCompiled(1);
        lang.setTimeLimitMultiplier(BigDecimal.ONE);
        lang.setMemoryLimitMultiplier(BigDecimal.ONE);
        lang.setCompileTimeLimit(10000);
        lang.setCompileMemoryLimit(512);
        lang.setCompileProcLimit(50);
        lang.setRunProcLimit(1);
        lang.setCreateTime(LocalDateTime.now());
        lang.setUpdateTime(LocalDateTime.now());
        return lang;
    }
}
