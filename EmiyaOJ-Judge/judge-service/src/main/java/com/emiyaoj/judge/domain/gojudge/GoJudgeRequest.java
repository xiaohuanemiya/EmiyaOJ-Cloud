package com.emiyaoj.judge.domain.gojudge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GoJudge REST API 请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoJudgeRequest {

    /** 命令列表 */
    private List<Cmd> cmd;

    /** 可选: 管道ID */
    private String pipeMapping;
}
