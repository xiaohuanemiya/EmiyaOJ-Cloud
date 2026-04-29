package com.emiyaoj.judge.service;

import com.emiyaoj.problem.dto.LanguageVO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将语言配置中的命令模板渲染为 go-judge 可接受的 args/env。
 */
public final class LanguageCommandBuilder {

    private LanguageCommandBuilder() {
    }

    public static List<String> compileArgs(LanguageVO language) {
        return splitCommand(render(language, language.getCompileCommand()));
    }

    public static List<String> runArgs(LanguageVO language) {
        return splitCommand(render(language, language.getRunCommand()));
    }

    public static List<String> env(LanguageVO language) {
        String envVars = language.getEnvVars();
        if (!StringUtils.hasText(envVars)) {
            return List.of("PATH=/usr/bin:/bin");
        }
        return Arrays.stream(envVars.split("[,\\r\\n]+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    public static String sourceFileName(LanguageVO language) {
        return language.getCompileFileName() + "." + language.getSourceFileExt();
    }

    public static List<String> compiledFileNames(LanguageVO language) {
        if (StringUtils.hasText(language.getCompiledFileNames())) {
            return Arrays.stream(language.getCompiledFileNames().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return List.of(language.getExecutableFileName());
    }

    public static long runCpuLimitNs(LanguageVO language, long timeLimitMs) {
        return multiplyToLong(timeLimitMs, language.getTimeLimitMultiplier()) * 1_000_000L;
    }

    public static long runMemoryLimitBytes(LanguageVO language, long memoryLimitMb) {
        return multiplyToLong(memoryLimitMb, language.getMemoryLimitMultiplier()) * 1024L * 1024L;
    }

    private static long multiplyToLong(long base, BigDecimal multiplier) {
        BigDecimal factor = multiplier == null ? BigDecimal.ONE : multiplier;
        return BigDecimal.valueOf(base).multiply(factor).setScale(0, RoundingMode.CEILING).longValue();
    }

    private static String render(LanguageVO language, String commandTemplate) {
        return commandTemplate
                .replace("{LanguageVersion}", language.getLanguageVersion())
                .replace("{CompileFileName}", language.getCompileFileName())
                .replace("{SourceFileName}", sourceFileName(language))
                .replace("{ExecutableFileName}", language.getExecutableFileName());
    }

    static List<String> splitCommand(String command) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaping = false;

        for (int i = 0; i < command.length(); i++) {
            char ch = command.charAt(i);
            if (escaping) {
                current.append(ch);
                escaping = false;
                continue;
            }
            if (ch == '\\' && inDoubleQuote) {
                escaping = true;
                continue;
            }
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (Character.isWhitespace(ch) && !inSingleQuote && !inDoubleQuote) {
                addArg(args, current);
                continue;
            }
            current.append(ch);
        }
        addArg(args, current);
        if (inSingleQuote || inDoubleQuote) {
            throw new IllegalArgumentException("命令模板引号不完整");
        }
        return args;
    }

    private static void addArg(List<String> args, StringBuilder current) {
        if (!current.isEmpty()) {
            args.add(current.toString());
            current.setLength(0);
        }
    }
}
