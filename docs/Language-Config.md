# 编程语言配置说明

语言配置存放在 `emiya_oj_problem.language`，DDL 和示例数据见 `sql/emiya_oj_language.sql`。判题服务通过 `ProblemFeignClient.getLanguageById()` 获取配置，再将命令模板渲染成 go-judge 的 `Cmd.args`。

## 命令占位符

| Placeholder | Description | Example |
| --- | --- | --- |
| `{LanguageVersion}` | 语言标准或版本参数 | `c++20` |
| `{CompileFileName}` | 源文件基础名，不含扩展名 | `main` |
| `{SourceFileName}` | 完整源文件名 | `main.cpp` |
| `{ExecutableFileName}` | 运行命令中的可执行目标名 | `main` |

C++20 示例：

```text
compileCommand = /usr/bin/g++ -std={LanguageVersion} -O2 -Wall -Wextra -o {ExecutableFileName} {CompileFileName}.cpp
runCommand = ./{ExecutableFileName}
```

渲染后：

```text
/usr/bin/g++ -std=c++20 -O2 -Wall -Wextra -o main main.cpp
./main
```

## 关键字段

| Field | Description |
| --- | --- |
| `name` | 语言名称，如 `C++`、`Java` |
| `version` | 展示版本，如 `C++20` |
| `languageVersion` | 填入 `{LanguageVersion}` 的值，如 `c++20` |
| `compileFileName` | 源文件基础名，不含扩展名 |
| `sourceFileExt` | 源文件扩展名，不含点 |
| `executableFileName` | 填入 `{ExecutableFileName}` 的值 |
| `compiledFileNames` | go-judge 编译缓存产物，多个文件用英文逗号分隔；为空时使用 `executableFileName` |
| `compileCommand` | 编译命令模板，解释型语言可为空 |
| `runCommand` | 运行命令模板 |
| `envVars` | go-judge 环境变量，逗号或换行分隔 |
| `isCompiled` | `1` 需要编译，`0` 解释执行 |
| `timeLimitMultiplier` | 运行 CPU 时间倍数 |
| `memoryLimitMultiplier` | 运行内存倍数 |
| `compileTimeLimit` | 编译 CPU 时间限制，毫秒 |
| `compileMemoryLimit` | 编译内存限制，MB |
| `compileProcLimit` | 编译进程数限制 |
| `runProcLimit` | 运行进程数限制 |

## 校验规则

- `runCommand` 必填；编译型语言必须填写 `compileCommand`。
- 命令模板只允许 `{LanguageVersion}`、`{CompileFileName}`、`{SourceFileName}`、`{ExecutableFileName}`。
- 命令模板不能包含换行、`;`、`|`、`&&`、`||`、反引号或 `$(`。
- 文件名字段不能包含路径，只能使用字母、数字、下划线、点和短横线。
- `sourceFileExt` 只能包含字母和数字。
- 资源限制、进程限制和倍数必须大于 0。

## GoJudge 映射

- 编译时，判题服务把用户代码写入 `{CompileFileName}.{sourceFileExt}`。
- 编译成功后，`compiledFileNames` 会作为 `copyOutCached` 传给 go-judge，并在运行阶段按同名文件 `copyIn`。
- 解释型语言不会走编译阶段，运行时直接把源文件写入 go-judge 的工作目录。
- 命令不会经过 shell 执行，而是拆分为 go-judge `args` 数组；因此模板里不要依赖 shell 展开、管道或重定向。
