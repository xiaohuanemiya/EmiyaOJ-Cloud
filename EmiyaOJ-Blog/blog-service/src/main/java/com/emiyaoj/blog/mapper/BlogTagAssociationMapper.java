package com.emiyaoj.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.blog.domain.BlogTagAssociation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogTagAssociationMapper extends BaseMapper<BlogTagAssociation> {
}
