package com.emiyaoj.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.problem.domain.pojo.Language;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LanguageMapper extends BaseMapper<Language> {
}
