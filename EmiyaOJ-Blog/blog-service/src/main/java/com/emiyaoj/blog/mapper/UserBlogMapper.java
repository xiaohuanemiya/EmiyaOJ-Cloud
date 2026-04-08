package com.emiyaoj.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.blog.domain.UserBlog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBlogMapper extends BaseMapper<UserBlog> {
}
