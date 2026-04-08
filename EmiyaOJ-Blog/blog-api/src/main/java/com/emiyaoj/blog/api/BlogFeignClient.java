package com.emiyaoj.blog.api;

import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.common.domain.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 博客服务 Feign 远程调用接口
 */
@FeignClient(value = "blog-service", contextId = "blogFeignClient")
public interface BlogFeignClient {

    /**
     * 根据 ID 查询博客
     */
    @GetMapping("/blog/{bid}")
    ResponseResult<BlogVO> getBlogById(@PathVariable("bid") Long bid);
}
