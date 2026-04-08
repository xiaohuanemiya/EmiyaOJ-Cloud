package com.emiyaoj.judge.domain.gojudge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * GoJudge 判题结果
 */
@Data
public class GoJudgeResult {

    /** 运行状态: Accepted, Memory Limit Exceeded, Time Limit Exceeded, etc. */
    private String status;

    /** 退出状态码 */
    private Integer exitStatus;

    /** CPU 时间 (纳秒) */
    private Long time;

    /** 内存使用 (字节) */
    private Long memory;

    /** 实际运行时间 (纳秒) */
    private Long runTime;

    /** 文件输出 (stdout/stderr 的内容) */
    @JsonProperty("files")
    private Map<String, String> files;

    /** 已缓存的文件ID映射 */
    @JsonProperty("fileIds")
    private Map<String, String> fileIds;

    /** 错误信息 (internal error) */
    @JsonProperty("error")
    private String error;
}
