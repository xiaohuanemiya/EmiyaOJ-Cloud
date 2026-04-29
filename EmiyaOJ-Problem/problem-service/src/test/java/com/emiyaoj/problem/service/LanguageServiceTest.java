package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 编程语言服务单元测试
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

    // ======================== getLanguageDetail ========================

    @Test
    void getLanguageDetail_shouldReturnVO_whenEnabledExists() {
        Language lang = buildLanguage(1L, "Java", "17", 1);
        when(languageMapper.selectById(1L)).thenReturn(lang);

        LanguageVO vo = languageService.getLanguageDetail(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("Java", vo.getName());
        assertEquals(1, vo.getStatus());
    }

    @Test
    void getLanguageDetail_shouldReturnNull_whenDisabled() {
        Language lang = buildLanguage(2L, "Cobol", "1.0", 0);
        when(languageMapper.selectById(2L)).thenReturn(lang);

        LanguageVO vo = languageService.getLanguageDetail(2L);

        assertNull(vo);
    }

    @Test
    void getLanguageDetail_shouldReturnNull_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        LanguageVO vo = languageService.getLanguageDetail(99L);

        assertNull(vo);
    }

    // ======================== listEnabled ========================

    @Test
    void listEnabled_shouldReturnOnlyEnabledLanguages() {
        Language lang1 = buildLanguage(1L, "Java", "17", 1);
        Language lang2 = buildLanguage(2L, "C++", "14", 1);
        when(languageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(lang1, lang2));

        List<LanguageVO> result = languageService.listEnabled();

        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getName());
        assertEquals("C++", result.get(1).getName());
    }

    // ======================== listAll ========================

    @Test
    void listAll_shouldReturnAllLanguages() {
        Language lang1 = buildLanguage(1L, "Java", "17", 1);
        Language lang2 = buildLanguage(2L, "Cobol", "1.0", 0);
        when(languageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(lang1, lang2));

        List<LanguageVO> result = languageService.listAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getStatus());
        assertEquals(0, result.get(1).getStatus());
    }

    // ======================== getAdminById ========================

    @Test
    void getAdminById_shouldReturnVO_regardlessOfStatus() {
        Language lang = buildLanguage(2L, "Cobol", "1.0", 0);
        when(languageMapper.selectById(2L)).thenReturn(lang);

        LanguageVO vo = languageService.getAdminById(2L);

        assertNotNull(vo);
        assertEquals("Cobol", vo.getName());
        assertEquals(0, vo.getStatus());
    }

    @Test
    void getAdminById_shouldReturnNull_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        LanguageVO vo = languageService.getAdminById(99L);

        assertNull(vo);
    }

    // ======================== saveLanguage ========================

    @Test
    void saveLanguage_shouldSaveAndReturnVO_whenNoDuplicate() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.insert(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("Go");
        dto.setVersion("1.22");
        dto.setExecuteCommand("go run {src}");
        dto.setSourceFileExt("go");
        dto.setIsCompiled(1);
        dto.setTimeLimitMultiplier(new BigDecimal("1.0"));
        dto.setMemoryLimitMultiplier(new BigDecimal("1.0"));

        LanguageVO vo = languageService.saveLanguage(dto);

        assertNotNull(vo);
        assertEquals("Go", vo.getName());
        assertEquals("1.22", vo.getVersion());
        assertEquals(1, vo.getStatus()); // default status = 1
        verify(languageMapper, times(1)).insert(any(Language.class));
    }

    @Test
    void saveLanguage_shouldSetDefaultValues_whenNullProvided() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.insert(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("Python3");
        dto.setVersion("3.12");
        dto.setExecuteCommand("python {src}");
        dto.setSourceFileExt("py");
        // status, isCompiled, timeLimitMultiplier, memoryLimitMultiplier all null

        LanguageVO vo = languageService.saveLanguage(dto);

        assertNotNull(vo);
        assertEquals(1, vo.getStatus());
        assertEquals(1, vo.getIsCompiled());
        assertEquals(BigDecimal.ONE, vo.getTimeLimitMultiplier());
        assertEquals(BigDecimal.ONE, vo.getMemoryLimitMultiplier());
    }

    @Test
    void saveLanguage_shouldThrow_whenDuplicateNameVersion() {
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("Java");
        dto.setVersion("17");
        dto.setExecuteCommand("java Main");
        dto.setSourceFileExt("java");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.saveLanguage(dto));
        assertEquals("同名同版本的语言已存在", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    @Test
    void saveLanguage_shouldThrow_whenRequiredFieldsMissing() {
        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setName("Java");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.saveLanguage(dto));
        assertEquals("语言版本不能为空", ex.getMessage());
        verify(languageMapper, never()).insert(any(Language.class));
    }

    // ======================== updateLanguage ========================

    @Test
    void updateLanguage_shouldUpdateSuccessfully_whenExists() {
        Language existing = buildLanguage(1L, "Java", "17", 1);
        when(languageMapper.selectById(1L)).thenReturn(existing);
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(languageMapper.updateById(any(Language.class))).thenReturn(1);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setId(1L);
        dto.setName("Java");
        dto.setVersion("21");
        dto.setExecuteCommand("java Main");
        dto.setSourceFileExt("java");
        dto.setStatus(1);

        boolean result = languageService.updateLanguage(dto);

        assertTrue(result);
        verify(languageMapper, times(1)).updateById(any(Language.class));
    }

    @Test
    void updateLanguage_shouldThrow_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setId(99L);
        dto.setName("Unknown");
        dto.setVersion("1.0");
        dto.setExecuteCommand("unknown {src}");
        dto.setSourceFileExt("unknown");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.updateLanguage(dto));
        assertEquals("语言不存在", ex.getMessage());
    }

    @Test
    void updateLanguage_shouldThrow_whenDuplicateNameVersionExists() {
        Language existing = buildLanguage(1L, "Java", "17", 1);
        when(languageMapper.selectById(1L)).thenReturn(existing);
        when(languageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        LanguageSaveDTO dto = new LanguageSaveDTO();
        dto.setId(1L);
        dto.setName("Java");
        dto.setVersion("21");
        dto.setExecuteCommand("java Main");
        dto.setSourceFileExt("java");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.updateLanguage(dto));
        assertEquals("同名同版本的语言已存在", ex.getMessage());
        verify(languageMapper, never()).updateById(any(Language.class));
    }

    // ======================== enableLanguage ========================

    @Test
    void enableLanguage_shouldSetStatusToOne_whenExists() {
        Language lang = buildLanguage(1L, "Java", "17", 0); // currently disabled
        when(languageMapper.selectById(1L)).thenReturn(lang);
        when(languageMapper.updateById(any(Language.class))).thenReturn(1);

        boolean result = languageService.enableLanguage(1L);

        assertTrue(result);
        assertEquals(1, lang.getStatus()); // should be updated to 1
        verify(languageMapper, times(1)).updateById(lang);
    }

    @Test
    void enableLanguage_shouldThrow_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.enableLanguage(99L));
        assertEquals("语言不存在", ex.getMessage());
    }

    // ======================== disableLanguage ========================

    @Test
    void disableLanguage_shouldSetStatusToZero_whenExists() {
        Language lang = buildLanguage(1L, "Java", "17", 1); // currently enabled
        when(languageMapper.selectById(1L)).thenReturn(lang);
        when(languageMapper.updateById(any(Language.class))).thenReturn(1);

        boolean result = languageService.disableLanguage(1L);

        assertTrue(result);
        assertEquals(0, lang.getStatus()); // should be updated to 0
        verify(languageMapper, times(1)).updateById(lang);
    }

    @Test
    void disableLanguage_shouldThrow_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.disableLanguage(99L));
        assertEquals("语言不存在", ex.getMessage());
    }

    // ======================== deleteLanguage ========================

    @Test
    void deleteLanguage_shouldDeletePhysically() {
        when(languageMapper.selectById(1L)).thenReturn(buildLanguage(1L, "Java", "17", 1));
        when(languageMapper.deleteById(1L)).thenReturn(1);

        boolean result = languageService.deleteLanguage(1L);

        assertTrue(result);
        verify(languageMapper, times(1)).deleteById(1L);
    }

    @Test
    void deleteLanguage_shouldThrow_whenNotExists() {
        when(languageMapper.selectById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> languageService.deleteLanguage(99L));
        assertEquals("语言不存在", ex.getMessage());
        verify(languageMapper, never()).deleteById(99L);
    }

    // ======================== helper ========================

    private Language buildLanguage(Long id, String name, String version, Integer status) {
        Language lang = new Language();
        lang.setId(id);
        lang.setName(name);
        lang.setVersion(version);
        lang.setStatus(status);
        lang.setIsCompiled(1);
        lang.setTimeLimitMultiplier(new BigDecimal("1.0"));
        lang.setMemoryLimitMultiplier(new BigDecimal("1.0"));
        lang.setCreateTime(LocalDateTime.now());
        lang.setUpdateTime(LocalDateTime.now());
        return lang;
    }
}
