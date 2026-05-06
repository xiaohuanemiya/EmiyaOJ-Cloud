package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.gojudge.Cmd;
import com.emiyaoj.judge.domain.gojudge.GoJudgeRequest;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GoJudge HTTP 调用服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoJudgeService {

    private static final long MB = 1024 * 1024L;
    private static final long DEFAULT_OUTPUT_MAX_SIZE = 64 * MB;

    private final WebClient.Builder webClientBuilder;

    @Value("${go-judge.url}")
    private String goJudgeUrl;

    /**
     * 编译源代码。
     */
    public GoJudgeResult compile(String code, LanguageVO language) {
        if (language.getIsCompiled() != null && language.getIsCompiled() == 0) {
            return null;
        }

        Cmd compileCmd = buildCompileCmd(code, language);
        GoJudgeRequest request = GoJudgeRequest.builder()
                .cmd(List.of(compileCmd))
                .build();

        List<GoJudgeResult> results = callGoJudge(request);
        if (results == null || results.isEmpty()) {
            GoJudgeResult errorResult = new GoJudgeResult();
            errorResult.setStatus(GoJudgeStatus.INTERNAL_ERROR.getValue());
            errorResult.setError("GoJudge returned empty response");
            return errorResult;
        }
        return results.get(0);
    }

    /**
     * 运行单个测试用例。
     */
    public GoJudgeResult run(LanguageVO language, Map<String, String> fileIds,
                             String code, TestCaseVO testCase,
                             long timeLimit, long memoryLimit) {
        Cmd runCmd = buildRunCmd(language, fileIds, code, testCase.getInput(), timeLimit, memoryLimit);
        GoJudgeRequest request = GoJudgeRequest.builder()
                .cmd(List.of(runCmd))
                .build();

        List<GoJudgeResult> results = callGoJudge(request);
        if (results == null || results.isEmpty()) {
            GoJudgeResult errorResult = new GoJudgeResult();
            errorResult.setStatus(GoJudgeStatus.INTERNAL_ERROR.getValue());
            errorResult.setError("GoJudge returned empty response");
            return errorResult;
        }
        return results.get(0);
    }

    /**
     * 删除 GoJudge 缓存文件。
     */
    public void deleteFile(String fileId) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri(goJudgeUrl + "/file/" + fileId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("Failed to delete cached file: {}", fileId, e);
        }
    }

    private List<GoJudgeResult> callGoJudge(GoJudgeRequest request) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(goJudgeUrl + "/run")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GoJudgeResult>>() {})
                    .block();
        } catch (Exception e) {
            log.error("GoJudge call failed", e);
            return null;
        }
    }

    private Cmd buildCompileCmd(String code, LanguageVO language) {
        return Cmd.builder()
                .args(LanguageCommandBuilder.compileArgs(language))
                .env(LanguageCommandBuilder.env(language))
                .files(defaultFiles())
                .cpuLimit(language.getCompileTimeLimit() * 1_000_000L)
                .clockLimit(language.getCompileTimeLimit() * 2_000_000L)
                .memoryLimit(language.getCompileMemoryLimit() * MB)
                .procLimit(language.getCompileProcLimit())
                .stdoutMaxSize(DEFAULT_OUTPUT_MAX_SIZE)
                .stderrMaxSize(DEFAULT_OUTPUT_MAX_SIZE)
                .copyIn(Map.of(LanguageCommandBuilder.sourceFileName(language),
                        Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stderr"))
                .copyOutCached(LanguageCommandBuilder.compiledFileNames(language))
                .build();
    }

    private Cmd buildRunCmd(LanguageVO language, Map<String, String> fileIds,
                            String code, String stdin,
                            long timeLimitMs, long memoryLimitMb) {
        long cpuLimitNs = LanguageCommandBuilder.runCpuLimitNs(language, timeLimitMs);
        long memoryLimitBytes = LanguageCommandBuilder.runMemoryLimitBytes(language, memoryLimitMb);

        return Cmd.builder()
                .args(LanguageCommandBuilder.runArgs(language))
                .env(LanguageCommandBuilder.env(language))
                .files(stdinFiles(stdin))
                .cpuLimit(cpuLimitNs)
                .clockLimit(cpuLimitNs * 2)
                .memoryLimit(memoryLimitBytes)
                .stackLimit(memoryLimitBytes)
                .procLimit(language.getRunProcLimit())
                .stdoutMaxSize(DEFAULT_OUTPUT_MAX_SIZE)
                .stderrMaxSize(DEFAULT_OUTPUT_MAX_SIZE)
                .copyIn(runCopyIn(language, fileIds, code))
                .copyOut(List.of("stdout", "stderr"))
                .build();
    }

    private Map<String, Cmd.CopyInFile> runCopyIn(LanguageVO language, Map<String, String> fileIds, String code) {
        if (language.getIsCompiled() != null && language.getIsCompiled() == 0) {
            return Map.of(LanguageCommandBuilder.sourceFileName(language),
                    Cmd.CopyInFile.builder().content(code).build());
        }

        Map<String, Cmd.CopyInFile> copyIn = new HashMap<>();
        if (fileIds == null) {
            return copyIn;
        }
        for (String compiledFileName : LanguageCommandBuilder.compiledFileNames(language)) {
            String fileId = fileIds.get(compiledFileName);
            if (fileId != null) {
                copyIn.put(compiledFileName, Cmd.CopyInFile.builder().fileId(fileId).build());
            }
        }
        return copyIn;
    }

    private List<Map<String, Object>> defaultFiles() {
        return List.of(
                Map.of("content", ""),
                Map.of("name", "stdout", "max", DEFAULT_OUTPUT_MAX_SIZE),
                Map.of("name", "stderr", "max", DEFAULT_OUTPUT_MAX_SIZE)
        );
    }

    private List<Map<String, Object>> stdinFiles(String stdin) {
        return List.of(
                Map.of("content", stdin != null ? stdin : ""),
                Map.of("name", "stdout", "max", DEFAULT_OUTPUT_MAX_SIZE),
                Map.of("name", "stderr", "max", DEFAULT_OUTPUT_MAX_SIZE)
        );
    }
}
