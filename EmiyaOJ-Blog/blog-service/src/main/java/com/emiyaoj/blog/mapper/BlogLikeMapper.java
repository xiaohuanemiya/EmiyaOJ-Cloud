package com.emiyaoj.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.blog.domain.BlogLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogLikeMapper extends BaseMapper<BlogLike> {
}
