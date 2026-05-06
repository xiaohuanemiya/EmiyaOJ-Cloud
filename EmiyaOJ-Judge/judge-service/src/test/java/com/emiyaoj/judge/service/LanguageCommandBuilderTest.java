package com.emiyaoj.judge.service;

import com.emiyaoj.problem.dto.LanguageVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageCommandBuilderTest {

    @Test
    void compileArgsRenderCpp20Template() {
        LanguageVO language = cpp20();

        List<String> args = LanguageCommandBuilder.compileArgs(language);

        assertEquals(List.of(
                "/usr/bin/g++", "-std=c++20", "-O2", "-Wall", "-Wextra",
                "-o", "main", "main.cpp"
        ), args);
    }

    @Test
    void runArgsRenderExecutableName() {
        assertEquals(List.of("./main"), LanguageCommandBuilder.runArgs(cpp20()));
    }

    @Test
    void sourceAndCompiledFilesUseLanguageConfig() {
        LanguageVO java = LanguageVO.builder()
                .compileFileName("Main")
                .sourceFileExt("java")
                .executableFileName("Main")
                .compiledFileNames("Main.class")
                .build();

        assertEquals("Main.java", LanguageCommandBuilder.sourceFileName(java));
        assertEquals(List.of("Main.class"), LanguageCommandBuilder.compiledFileNames(java));
    }

    @Test
    void envSplitsCommaAndNewLineSeparatedValues() {
        LanguageVO language = LanguageVO.builder()
                .envVars("PATH=/usr/bin:/bin,GOPATH=/tmp/go\nGOCACHE=/tmp/go-cache")
                .build();

        assertEquals(List.of("PATH=/usr/bin:/bin", "GOPATH=/tmp/go", "GOCACHE=/tmp/go-cache"),
                LanguageCommandBuilder.env(language));
    }

    @Test
    void resourceLimitsApplyMultipliers() {
        LanguageVO language = LanguageVO.builder()
                .timeLimitMultiplier(new BigDecimal("2.5"))
                .memoryLimitMultiplier(new BigDecimal("1.5"))
                .build();

        assertEquals(2500_000_000L, LanguageCommandBuilder.runCpuLimitNs(language, 1000));
        assertEquals(384L * 1024L * 1024L, LanguageCommandBuilder.runMemoryLimitBytes(language, 256));
    }

    private LanguageVO cpp20() {
        return LanguageVO.builder()
                .languageVersion("c++20")
                .compileFileName("main")
                .sourceFileExt("cpp")
                .executableFileName("main")
                .compileCommand("/usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp")
                .runCommand("./{ExecutableFileName}")
                .envVars("PATH=/usr/bin:/bin")
                .build();
    }
}
