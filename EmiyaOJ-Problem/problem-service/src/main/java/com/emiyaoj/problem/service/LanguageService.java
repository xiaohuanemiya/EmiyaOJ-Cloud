package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.stream.Collectors;

/**
 * 编程语言服务
 */
@Slf4j
@Service
public class LanguageService extends ServiceImpl<LanguageMapper, Language> {

    /**
     * 获取语言详情（供 Feign 调用，仅返回启用语言）
     */
    public LanguageVO getLanguageDetail(Long id) {
        Language language = this.getById(id);
        if (language == null || language.getStatus() == 0) {
            return null;
        }
        LanguageVO vo = new LanguageVO();
        BeanUtils.copyProperties(language, vo);
        return vo;
    }

    /**
     * 查询所有启用的编程语言（前台）
     */
    public List<LanguageVO> listEnabled() {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Language::getStatus, 1)
               .orderByAsc(Language::getId);
        return this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 查询所有编程语言（管理端，含禁用）
     */
    public List<LanguageVO> listAll() {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Language::getId);
        return this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 管理端根据 ID 查询语言详情（不过滤状态）
     */
    public LanguageVO getAdminById(Long id) {
        Language language = this.getById(id);
        if (language == null) {
            return null;
        }
        return toVO(language);
    }

    /**
     * 新增编程语言（同名同版本不允许重复）
     */
    public LanguageVO saveLanguage(LanguageSaveDTO dto) {
        LambdaQueryWrapper<Language> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Language::getName, dto.getName())
               .eq(StringUtils.hasText(dto.getVersion()), Language::getVersion, dto.getVersion());
        long count = this.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("同名同版本的语言已存在");
        }
        Language language = new Language();
        BeanUtils.copyProperties(dto, language);
        if (language.getStatus() == null) {
            language.setStatus(1);
        }
        if (language.getTimeLimitMultiplier() == null) {
            language.setTimeLimitMultiplier(BigDecimal.ONE);
        }
        if (language.getMemoryLimitMultiplier() == null) {
            language.setMemoryLimitMultiplier(BigDecimal.ONE);
        }
        language.setCreateTime(LocalDateTime.now());
        language.setUpdateTime(LocalDateTime.now());
        this.save(language);
        return toVO(language);
    }

    /**
     * 更新编程语言信息
     */
    public boolean updateLanguage(LanguageSaveDTO dto) {
        Language language = this.getById(dto.getId());
        if (language == null) {
            throw new RuntimeException("语言不存在");
        }
        BeanUtils.copyProperties(dto, language);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 启用编程语言
     */
    public boolean enableLanguage(Long id) {
        Language language = this.getById(id);
        if (language == null) {
            throw new RuntimeException("语言不存在");
        }
        language.setStatus(1);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 禁用编程语言
     */
    public boolean disableLanguage(Long id) {
        Language language = this.getById(id);
        if (language == null) {
            throw new RuntimeException("语言不存在");
        }
        language.setStatus(0);
        language.setUpdateTime(LocalDateTime.now());
        return this.updateById(language);
    }

    /**
     * 物理删除编程语言
     */
    public boolean deleteLanguage(Long id) {
        return this.removeById(id);
    }

    // ======================== 私有方法 ========================

    private LanguageVO toVO(Language language) {
        LanguageVO vo = new LanguageVO();
        BeanUtils.copyProperties(language, vo);
        return vo;
    }
}
