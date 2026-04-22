package com.emiyaoj.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果 VO
 *
 * @param <T> 返回数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> list;

    /** 当前页码 */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /**
     * 由 MyBatis-Plus 分页对象直接转换（无类型转换）
     */
    public static <T> PageVO<T> of(Page<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        vo.setPageNum(page.getCurrent());
        vo.setPageSize(page.getSize());
        return vo;
    }

    /**
     * 由 MyBatis-Plus 分页对象转换并映射类型
     */
    public static <P, T> PageVO<T> of(Page<P> page, Function<P, T> converter) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords() == null ? Collections.emptyList()
                : page.getRecords().stream().map(converter).collect(Collectors.toList()));
        vo.setPageNum(page.getCurrent());
        vo.setPageSize(page.getSize());
        return vo;
    }

    /**
     * 空分页
     */
    public static <T> PageVO<T> empty(Long pageNum, Long pageSize) {
        return new PageVO<>(0L, Collections.emptyList(), pageNum, pageSize);
    }
}
