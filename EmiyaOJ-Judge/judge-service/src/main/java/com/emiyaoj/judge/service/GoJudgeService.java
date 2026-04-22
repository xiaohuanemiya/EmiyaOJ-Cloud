package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.gojudge.*;
import com.emiyaoj.problem.dto.TestCaseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * GoJudge HTTP 调用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoJudgeService {

    private final WebClient.Builder webClientBuilder;

    @Value("${go-judge.url}")
    private String goJudgeUrl;

    // ===================== 语言编译 & 运行参数配置 =====================

    private static final long NANOSECOND = 1_000_000_000L;
    private static final long MB = 1024 * 1024L;

    /**
     * 编译源代码
     *
     * @param code         源代码
     * @param languageName 语言名称 (如 C, C++, Java, Python3, Go)
     * @return 编译结果, 如果编译成功, fileIds 中包含编译产物; 失败则 error/files.stderr 有信息
     */
    public GoJudgeResult compile(String code, String languageName) {
        Cmd compileCmd = buildCompileCmd(code, languageName);
        if (compileCmd == null) {
            // Python 不需要编译
            return null;
        }

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
     * 运行单个测试用例
     *
     * @param languageName 语言名称
     * @param fileIds      编译产物文件ID映射 (可为空, Python 不需要)
     * @param code         源代码 (Python 需要)
     * @param testCase     测试用例
     * @param timeLimit    时间限制(ms)
     * @param memoryLimit  内存限制(MB)
     * @return 运行结果
     */
    public GoJudgeResult run(String languageName, Map<String, String> fileIds,
                             String code, TestCaseVO testCase,
                             long timeLimit, long memoryLimit) {
        Cmd runCmd = buildRunCmd(languageName, fileIds, code, testCase.getInput(),
                timeLimit * 1_000_000L, memoryLimit * MB);

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
     * 删除缓存文件
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

    // ===================== 私有方法 =====================

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

    private Cmd buildCompileCmd(String code, String languageName) {
        return switch (languageName.toLowerCase()) {
            case "c" -> buildCCompileCmd(code);
            case "c++" -> buildCppCompileCmd(code);
            case "java" -> buildJavaCompileCmd(code);
            case "go" -> buildGoCompileCmd(code);
            default -> null; // Python 等解释型语言不需要编译
        };
    }

    private Cmd buildCCompileCmd(String code) {
        return Cmd.builder()
                .args(List.of("/usr/bin/gcc", "main.c", "-o", "main", "-std=c11", "-O2", "-lm"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(defaultFiles())
                .cpuLimit(10 * NANOSECOND)
                .memoryLimit(256 * MB)
                .procLimit(50)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(Map.of("main.c", Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stderr"))
                .copyOutCached(List.of("main"))
                .build();
    }

    private Cmd buildCppCompileCmd(String code) {
        return Cmd.builder()
                .args(List.of("/usr/bin/g++", "main.cpp", "-o", "main", "-std=c++17", "-O2"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(defaultFiles())
                .cpuLimit(10 * NANOSECOND)
                .memoryLimit(256 * MB)
                .procLimit(50)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(Map.of("main.cpp", Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stderr"))
                .copyOutCached(List.of("main"))
                .build();
    }

    private Cmd buildJavaCompileCmd(String code) {
        return Cmd.builder()
                .args(List.of("/usr/bin/javac", "Main.java"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(defaultFiles())
                .cpuLimit(10 * NANOSECOND)
                .memoryLimit(512 * MB)
                .procLimit(50)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(Map.of("Main.java", Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stderr"))
                .copyOutCached(List.of("Main.class"))
                .build();
    }

    private Cmd buildGoCompileCmd(String code) {
        return Cmd.builder()
                .args(List.of("/usr/bin/go", "build", "-o", "main", "main.go"))
                .env(List.of("PATH=/usr/bin:/bin", "GOPATH=/tmp/go", "GOCACHE=/tmp/go-cache"))
                .files(defaultFiles())
                .cpuLimit(15 * NANOSECOND)
                .memoryLimit(512 * MB)
                .procLimit(50)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(Map.of("main.go", Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stderr"))
                .copyOutCached(List.of("main"))
                .build();
    }

    private Cmd buildRunCmd(String languageName, Map<String, String> fileIds,
                            String code, String stdin,
                            long timeLimitNs, long memoryLimitBytes) {
        return switch (languageName.toLowerCase()) {
            case "c", "c++", "go" -> buildNativeRunCmd(fileIds, stdin, timeLimitNs, memoryLimitBytes);
            case "java" -> buildJavaRunCmd(fileIds, stdin, timeLimitNs, memoryLimitBytes);
            case "python3", "python" -> buildPythonRunCmd(code, stdin, timeLimitNs, memoryLimitBytes);
            default -> buildPythonRunCmd(code, stdin, timeLimitNs, memoryLimitBytes);
        };
    }

    private Cmd buildNativeRunCmd(Map<String, String> fileIds, String stdin,
                                  long timeLimitNs, long memoryLimitBytes) {
        Map<String, Cmd.CopyInFile> copyIn = new HashMap<>();
        if (fileIds != null && fileIds.containsKey("main")) {
            copyIn.put("main", Cmd.CopyInFile.builder().fileId(fileIds.get("main")).build());
        }

        return Cmd.builder()
                .args(List.of("main"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(stdinFiles(stdin))
                .cpuLimit(timeLimitNs)
                .clockLimit(timeLimitNs * 2)
                .memoryLimit(memoryLimitBytes)
                .stackLimit(memoryLimitBytes)
                .procLimit(1)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(copyIn)
                .copyOut(List.of("stdout", "stderr"))
                .build();
    }

    private Cmd buildJavaRunCmd(Map<String, String> fileIds, String stdin,
                                long timeLimitNs, long memoryLimitBytes) {
        Map<String, Cmd.CopyInFile> copyIn = new HashMap<>();
        if (fileIds != null && fileIds.containsKey("Main.class")) {
            copyIn.put("Main.class", Cmd.CopyInFile.builder().fileId(fileIds.get("Main.class")).build());
        }

        return Cmd.builder()
                .args(List.of("/usr/bin/java", "Main"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(stdinFiles(stdin))
                .cpuLimit(timeLimitNs * 2) // Java 给双倍时间
                .clockLimit(timeLimitNs * 4)
                .memoryLimit(memoryLimitBytes * 2)
                .stackLimit(memoryLimitBytes)
                .procLimit(50)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(copyIn)
                .copyOut(List.of("stdout", "stderr"))
                .build();
    }

    private Cmd buildPythonRunCmd(String code, String stdin,
                                  long timeLimitNs, long memoryLimitBytes) {
        return Cmd.builder()
                .args(List.of("/usr/bin/python3", "main.py"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(stdinFiles(stdin))
                .cpuLimit(timeLimitNs * 3) // Python 给三倍时间
                .clockLimit(timeLimitNs * 6)
                .memoryLimit(memoryLimitBytes * 2)
                .stackLimit(memoryLimitBytes)
                .procLimit(1)
                .stdoutMaxSize(64 * MB)
                .stderrMaxSize(64 * MB)
                .copyIn(Map.of("main.py", Cmd.CopyInFile.builder().content(code).build()))
                .copyOut(List.of("stdout", "stderr"))
                .build();
    }

    /**
     * 默认文件映射: stdin(空), stdout(collector), stderr(collector)
     */
    private List<Map<String, Object>> defaultFiles() {
        return List.of(
                Map.of("content", ""),             // stdin
                Map.of("name", "stdout", "max", 64 * MB), // stdout
                Map.of("name", "stderr", "max", 64 * MB)  // stderr
        );
    }

    /**
     * 带标准输入的文件映射
     */
    private List<Map<String, Object>> stdinFiles(String stdin) {
        return List.of(
                Map.of("content", stdin != null ? stdin : ""),
                Map.of("name", "stdout", "max", 64 * MB),
                Map.of("name", "stderr", "max", 64 * MB)
        );
    }
}
