package com.emiyaoj.judge.api;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.SubmissionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 判题服务 Feign 客户端
 */
@FeignClient(name = "judge-service", path = "/submission")
public interface SubmissionFeignClient {

    /**
     * 根据ID查询提交记录
     */
    @GetMapping("/{id}")
    ResponseResult<SubmissionVO> getSubmissionById(@PathVariable("id") Long id);
}
