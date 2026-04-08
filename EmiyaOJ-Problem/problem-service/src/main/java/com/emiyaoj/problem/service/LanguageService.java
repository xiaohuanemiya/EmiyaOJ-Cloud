package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.problem.domain.pojo.Language;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.mapper.LanguageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 编程语言服务
 */
@Slf4j
@Service
public class LanguageService extends ServiceImpl<LanguageMapper, Language> {

    /**
     * 获取语言详情
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
}
