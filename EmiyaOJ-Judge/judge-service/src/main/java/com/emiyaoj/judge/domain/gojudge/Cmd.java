package com.emiyaoj.judge.domain.gojudge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GoJudge 命令对象
 * 对应 go-judge REST API 的 Cmd 结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cmd {

    /** 执行参数, 如 ["/usr/bin/gcc", "a.c", "-o", "a"] */
    private List<String> args;

    /** 环境变量 */
    private List<String> env;

    /** 文件映射 (stdin, stdout, stderr) */
    private List<Map<String, Object>> files;

    /** CPU 时间限制 (纳秒) */
    private Long cpuLimit;

    /** 实际时间限制 (纳秒) */
    private Long clockLimit;

    /** 内存限制 (字节) */
    private Long memoryLimit;

    /** 栈内存限制 (字节) */
    private Long stackLimit;

    /** 进程数限制 */
    private Integer procLimit;

    /** 输出大小限制 (字节) */
    private Long stdoutMaxSize;

    /** 错误输出大小限制 (字节) */
    private Long stderrMaxSize;

    /** 拷入文件 */
    private Map<String, CopyInFile> copyIn;

    /** 拷出文件 */
    private List<String> copyOut;

    /** 拷出文件 (可选, 不存在不报错) */
    private List<String> copyOutCached;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CopyInFile {
        /** 文件内容 (与 fileId 二选一) */
        private String content;

        /** 已缓存的文件ID */
        @JsonProperty("fileId")
        private String fileId;
    }
}
