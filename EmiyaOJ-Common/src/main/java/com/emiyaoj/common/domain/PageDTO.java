package com.emiyaoj.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 通用分页请求参数 DTO
 */
@Data
public class PageDTO {

    /** 当前页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;

    /**
     * 转为 MyBatis-Plus 分页对象，默认按 create_time 降序
     */
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 转为 MyBatis-Plus 分页对象
     */
    public <T> Page<T> toMpPage() {
        return new Page<>(pageNum, pageSize);
    }
}
