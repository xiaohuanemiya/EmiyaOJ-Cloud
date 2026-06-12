package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.gojudge.Cmd;
import com.emiyaoj.judge.domain.gojudge.GoJudgeRequest;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseGeneratorRunner {

    private static final long MB = 1024 * 1024L;
    private static final long DEFAULT_STDOUT_MAX_SIZE = 64 * MB;
    private static final long DEFAULT_STDERR_MAX_SIZE = 4 * MB;
    private static final long DEFAULT_CPU_LIMIT_NS = 5_000_000_000L;
    private static final long DEFAULT_MEMORY_LIMIT_BYTES = 256 * MB;
    private static final int DEFAULT_PROC_LIMIT = 10;
    private static final String GENERATOR_FILE_NAME = "generator.py";

    private final WebClient.Builder webClientBuilder;

    @Value("${go-judge.url}")
    private String goJudgeUrl;

    public TestCaseGeneratorRunResultVO run(String generatorCode) {
        if (!StringUtils.hasText(generatorCode)) {
            TestCaseGeneratorRunResultVO result = new TestCaseGeneratorRunResultVO();
            result.setSuccess(false);
            result.setErrorMessage("Test case generator code cannot be empty");
            return result;
        }

        GoJudgeRequest request = GoJudgeRequest.builder()
                .cmd(List.of(buildRunCmd(generatorCode)))
                .build();
        List<GoJudgeResult> results = callGoJudge(request);
        if (results == null || results.isEmpty()) {
            TestCaseGeneratorRunResultVO result = new TestCaseGeneratorRunResultVO();
            result.setSuccess(false);
            result.setStatus(GoJudgeStatus.INTERNAL_ERROR.getValue());
            result.setErrorMessage("GoJudge returned empty response");
            return result;
        }
        return toRunResult(results.get(0));
    }

    Cmd buildRunCmd(String generatorCode) {
        return Cmd.builder()
                .args(List.of("/usr/bin/python3", GENERATOR_FILE_NAME))
                .env(List.of("PATH=/usr/bin:/bin", "PYTHONIOENCODING=UTF-8"))
                .files(defaultFiles())
                .cpuLimit(DEFAULT_CPU_LIMIT_NS)
                .clockLimit(DEFAULT_CPU_LIMIT_NS * 2)
                .memoryLimit(DEFAULT_MEMORY_LIMIT_BYTES)
                .stackLimit(DEFAULT_MEMORY_LIMIT_BYTES)
                .procLimit(DEFAULT_PROC_LIMIT)
                .stdoutMaxSize(DEFAULT_STDOUT_MAX_SIZE)
                .stderrMaxSize(DEFAULT_STDERR_MAX_SIZE)
                .copyIn(Map.of(GENERATOR_FILE_NAME, Cmd.CopyInFile.builder().content(generatorCode).build()))
                .copyOut(List.of("stdout", "stderr"))
                .build();
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
            log.error("GoJudge call failed for test case generator", e);
            return null;
        }
    }

    private TestCaseGeneratorRunResultVO toRunResult(GoJudgeResult runResult) {
        String stdout = fileContent(runResult, "stdout");
        String stderr = fileContent(runResult, "stderr");
        boolean accepted = GoJudgeStatus.ACCEPTED.getValue().equals(runResult.getStatus());

        TestCaseGeneratorRunResultVO result = new TestCaseGeneratorRunResultVO();
        result.setSuccess(accepted);
        result.setStatus(runResult.getStatus());
        result.setStdout(stdout);
        result.setStderr(stderr);
        result.setTimeUsed(runResult.getTime() == null ? 0L : runResult.getTime() / 1_000_000);
        result.setMemoryUsed(runResult.getMemory() == null ? 0L : runResult.getMemory() / 1024);
        if (!accepted) {
            result.setErrorMessage(resolveErrorMessage(runResult, stderr));
        }
        return result;
    }

    private String resolveErrorMessage(GoJudgeResult runResult, String stderr) {
        if (StringUtils.hasText(stderr)) {
            return stderr;
        }
        if (StringUtils.hasText(runResult.getError())) {
            return runResult.getError();
        }
        if (StringUtils.hasText(runResult.getStatus())) {
            return "Generator run failed: " + runResult.getStatus();
        }
        return "Generator run failed";
    }

    private String fileContent(GoJudgeResult result, String name) {
        if (result.getFiles() == null) {
            return "";
        }
        return result.getFiles().getOrDefault(name, "");
    }

    private List<Map<String, Object>> defaultFiles() {
        return List.of(
                Map.of("content", ""),
                Map.of("name", "stdout", "max", DEFAULT_STDOUT_MAX_SIZE),
                Map.of("name", "stderr", "max", DEFAULT_STDERR_MAX_SIZE)
        );
    }
}
