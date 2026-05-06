package com.emiyaoj.problem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.problem.domain.pojo.Contest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContestMapper extends BaseMapper<Contest> {
}
