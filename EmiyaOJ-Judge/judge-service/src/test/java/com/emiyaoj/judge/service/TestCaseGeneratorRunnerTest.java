package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.gojudge.Cmd;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunResultVO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCaseGeneratorRunnerTest {

    @Test
    void buildRunCmdRunsPythonGeneratorInSandbox() {
        TestCaseGeneratorRunner runner = new TestCaseGeneratorRunner(WebClient.builder());

        Cmd cmd = runner.buildRunCmd("print('[]')");

        assertEquals(List.of("/usr/bin/python3", "generator.py"), cmd.getArgs());
        assertEquals("print('[]')", cmd.getCopyIn().get("generator.py").getContent());
        assertEquals(List.of("stdout", "stderr"), cmd.getCopyOut());
        assertEquals(10, cmd.getProcLimit());
    }

    @Test
    void runRejectsBlankCodeBeforeCallingGoJudge() {
        TestCaseGeneratorRunner runner = new TestCaseGeneratorRunner(WebClient.builder());

        TestCaseGeneratorRunResultVO result = runner.run(" ");

        assertFalse(result.getSuccess());
        assertEquals("Test case generator code cannot be empty", result.getErrorMessage());
    }

    @Test
    void runReadsLargeGoJudgeResponse() throws Exception {
        String stdout = "1".repeat(1024 * 1024);
        String response = """
                [{"status":"Accepted","time":1000000,"memory":1048576,"files":{"stdout":"%s","stderr":""}}]
                """.formatted(stdout);

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/run", exchange -> {
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            ExchangeStrategies strategies = ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                    .build();
            TestCaseGeneratorRunner runner = new TestCaseGeneratorRunner(
                    WebClient.builder().exchangeStrategies(strategies));
            ReflectionTestUtils.setField(runner, "goJudgeUrl",
                    "http://localhost:" + server.getAddress().getPort());

            TestCaseGeneratorRunResultVO result = runner.run("print('[]')");

            assertTrue(result.getSuccess());
            assertEquals(stdout.length(), result.getStdout().length());
            assertEquals(stdout, result.getStdout());
        } finally {
            server.stop(0);
        }
    }
}
