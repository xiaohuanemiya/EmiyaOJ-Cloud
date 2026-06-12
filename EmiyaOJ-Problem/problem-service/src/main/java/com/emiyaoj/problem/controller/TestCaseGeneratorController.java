package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.RunTestCaseGeneratorDTO;
import com.emiyaoj.problem.dto.RunTestCaseGeneratorVO;
import com.emiyaoj.problem.dto.TestCaseGeneratorSpecSaveDTO;
import com.emiyaoj.problem.dto.TestCaseGeneratorSpecVO;
import com.emiyaoj.problem.dto.TestCaseGeneratorUpdateDTO;
import com.emiyaoj.problem.dto.TestCaseGeneratorVO;
import com.emiyaoj.problem.service.TestCaseGeneratorService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test Case Generator Management")
@RestController
@RequestMapping("/test-case-generator")
@RequiredArgsConstructor
public class TestCaseGeneratorController {

    private static final String GENERATOR_SPEC_RULES = """
            测试数据生成器描述规范：
            1. 必须说明题目的输入格式和输出格式。
            2. 必须说明主要数据范围，例如变量上下界、字符串长度、数组规模等。
            3. 必须说明需要覆盖的边界场景，例如 0、负数、最大值、最小值、空结构、重复值等。
            4. 必须说明随机数据策略，例如随机数量、随机范围、固定随机种子要求。
            5. 必须说明样例用例要求，例如至少生成 1 组 isSample=1 的样例。
            6. 必须说明生成器 stdout 输出协议：Python 脚本必须输出 JSON 数组，每个元素表示一个测试用例。
            7. 每个测试用例对象中 input 和 output 可选，缺省或 null 表示空输入/空期望输出；isSample、score、sortOrder 可选。
            8. input/output 有内容时应保留真实换行，通常以 \\n 结尾，便于判题程序按标准输入输出读取。
            9. 描述中应要求生成器脚本基于标准 Python 框架编写：import json，构造 case/cases，最后 print(json.dumps(cases, ensure_ascii=False))。

            标准 Python 脚本框架：
            ```python
            import json

            case = {
                "input": "",
                "output": "",
                "isSample": 0,
                "score": 100,
                "sortOrder": 1
            }

            print(json.dumps([case], ensure_ascii=False))
            ```

            A+B 题目生成器描述示例：
            题目：两数之和。
            输入格式：一行包含两个整数 a 和 b，中间用一个空格分隔。
            输出格式：输出一个整数，表示 a+b 的结果。
            数据范围：普通数据覆盖 [-100,100]；大数数据覆盖 [-1000000000,1000000000]。
            边界场景：覆盖 0、正数、负数、相同数、互为相反数、最大值、最小值。
            生成要求：至少 1 组样例，若干组隐藏用例；stdout 输出 JSON 数组，元素包含 input、output、isSample、score、sortOrder。
            """;

    private static final String GENERATOR_STDOUT_RULES = """
            Python 生成器脚本规范：
            1. 脚本会在 Judge Service 的 Go-Judge 沙箱中以 /usr/bin/python3 generator.py 执行。
            2. 脚本必须向 stdout 输出 JSON 数组，不能输出额外解释文字、日志或 Markdown。
            3. JSON 数组中每个元素表示一个测试用例，input 和 output 可选；缺省或 null 表示空输入/空期望输出。
            4. isSample 只能为 0 或 1；score 不能为负数；sortOrder 不传时后端自动补齐。
            5. 推荐使用 json.dumps(cases, ensure_ascii=False) 输出，避免手写 JSON 转义错误。
            6. 推荐使用固定 random.seed，保证同一脚本多次运行结果可复现。
            7. 生成器必须自己计算标准输出，后端不会额外运行标程。

            标准 Python 脚本框架：
            ```python
            import json

            case = {
                "input": "",
                "output": "",
                "isSample": 0,
                "score": 100,
                "sortOrder": 1
            }

            print(json.dumps([case], ensure_ascii=False))
            ```

            stdout JSON 示例：
            [
              {"input":"1 2\\n","output":"3\\n","isSample":1,"score":0,"sortOrder":1},
              {"input":"100 200\\n","output":"300\\n","isSample":0,"score":10,"sortOrder":2}
            ]
            """;

    private static final String SPEC_REQUEST_EXAMPLE = """
            {
              "spec": "题目：两数之和。\\n输入格式：一行包含两个整数 a 和 b，中间用一个空格分隔。\\n输出格式：输出一个整数，表示 a+b 的结果。\\n数据范围：普通数据覆盖 [-100,100]；大数数据覆盖 [-1000000000,1000000000]。\\n边界场景：覆盖 0、正数、负数、相同数、互为相反数、最大值、最小值。\\n生成要求：至少 1 组样例，若干组隐藏用例；Python 生成器 stdout 必须输出 JSON 数组，元素包含 input、output、isSample、score、sortOrder。"
            }
            """;

    private static final String GENERATOR_CODE_FRAMEWORK_REQUEST_EXAMPLE = """
            {
              "generatorCode": "import json\\n\\ncase = {\\n    \\"input\\": \\"\\",\\n    \\"output\\": \\"\\",\\n    \\"isSample\\": 0,\\n    \\"score\\": 100,\\n    \\"sortOrder\\": 1\\n}\\n\\nprint(json.dumps([case], ensure_ascii=False))"
            }
            """;

    private static final String GENERATOR_CODE_REQUEST_EXAMPLE = """
            {
              "generatorCode": "import json\\nimport random\\n\\ncases = []\\n\\ndef add_case(a, b, is_sample=0, score=10):\\n    cases.append({\\n        \\"input\\": f\\"{a} {b}\\\\n\\",\\n        \\"output\\": f\\"{a + b}\\\\n\\",\\n        \\"isSample\\": is_sample,\\n        \\"score\\": score,\\n        \\"sortOrder\\": len(cases) + 1\\n    })\\n\\nadd_case(1, 2, is_sample=1, score=0)\\nadd_case(0, 0)\\nadd_case(1000000000, 1000000000)\\nadd_case(-1000000000, -1000000000)\\nadd_case(1000000000, -1000000000)\\n\\nrandom.seed(20260528)\\nfor _ in range(10):\\n    a = random.randint(-1000000, 1000000)\\n    b = random.randint(-1000000, 1000000)\\n    add_case(a, b)\\n\\nprint(json.dumps(cases, ensure_ascii=False))"
            }
            """;

    private static final String RUN_REQUEST_EXAMPLE = """
            {
              "saveMode": "APPEND"
            }
            """;

    private final TestCaseGeneratorService testCaseGeneratorService;

    @PostMapping("/{problemId}/spec")
    @Operation(summary = "Create test case generator spec", description = GENERATOR_SPEC_RULES)
    public ResponseResult<TestCaseGeneratorSpecVO> createTestCaseGeneratorSpec(
            @PathVariable Long problemId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "创建测试数据生成器描述。必须按描述规范说明输入输出、数据范围、边界场景和 stdout JSON 协议。",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "A+B 生成器描述示例", value = SPEC_REQUEST_EXAMPLE)))
            @RequestBody TestCaseGeneratorSpecSaveDTO dto) {
        return ResponseResult.success(testCaseGeneratorService.createTestCaseGeneratorSpec(
                problemId, dto));
    }

    @PutMapping("/{problemId}/spec")
    @Operation(summary = "Update test case generator spec", description = GENERATOR_SPEC_RULES)
    public ResponseResult<TestCaseGeneratorSpecVO> updateTestCaseGeneratorSpec(
            @PathVariable Long problemId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更新测试数据生成器描述。描述必须包含规范中的输入输出、数据范围、边界场景和 stdout JSON 协议。",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "A+B 生成器描述示例", value = SPEC_REQUEST_EXAMPLE)))
            @RequestBody TestCaseGeneratorSpecSaveDTO dto) {
        return ResponseResult.success(testCaseGeneratorService.updateTestCaseGeneratorSpec(
                problemId, dto));
    }

    @GetMapping("/{problemId}/spec")
    @Operation(summary = "Get test case generator spec")
    public ResponseResult<TestCaseGeneratorSpecVO> getTestCaseGeneratorSpec(
            @PathVariable Long problemId) {
        TestCaseGeneratorSpecVO vo = testCaseGeneratorService.getTestCaseGeneratorSpec(problemId);
        if (vo == null) {
            return ResponseResult.fail(404, "Test case generator does not exist");
        }
        return ResponseResult.success(vo);
    }

    @GetMapping("/{problemId}")
    @Operation(summary = "Get test case generator")
    public ResponseResult<TestCaseGeneratorVO> getTestCaseGenerator(
            @PathVariable Long problemId) {
        TestCaseGeneratorVO vo = testCaseGeneratorService.getTestCaseGenerator(problemId);
        if (vo == null) {
            return ResponseResult.fail(404, "Test case generator does not exist");
        }
        return ResponseResult.success(vo);
    }

    @PutMapping("/{problemId}")
    @Operation(summary = "Update test case generator", description = GENERATOR_STDOUT_RULES)
    public ResponseResult<TestCaseGeneratorVO> updateTestCaseGenerator(
            @PathVariable Long problemId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更新 Python 测试数据生成器脚本。脚本必须遵守 stdout JSON 数组输出规范。",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "标准 Python 脚本框架", value = GENERATOR_CODE_FRAMEWORK_REQUEST_EXAMPLE),
                                    @ExampleObject(name = "A+B Python 生成器脚本示例", value = GENERATOR_CODE_REQUEST_EXAMPLE)
                            }))
            @RequestBody TestCaseGeneratorUpdateDTO dto) {
        return ResponseResult.success(testCaseGeneratorService.updateTestCaseGenerator(
                problemId, dto));
    }

    @PostMapping("/{problemId}/run")
    @Operation(summary = "Run test case generator and save generated test cases",
            description = GENERATOR_STDOUT_RULES + "\n保存策略：APPEND 表示追加生成用例；REPLACE 表示先删除该题旧用例再保存生成用例。")
    public ResponseResult<RunTestCaseGeneratorVO> runTestCaseGenerator(
            @PathVariable Long problemId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "运行生成器并保存测试用例。saveMode 默认为 APPEND，可传 REPLACE 替换旧用例。",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "追加保存", value = RUN_REQUEST_EXAMPLE)))
            @RequestBody(required = false) RunTestCaseGeneratorDTO dto) {
        return ResponseResult.success(testCaseGeneratorService.runTestCaseGenerator(problemId, dto));
    }
}
