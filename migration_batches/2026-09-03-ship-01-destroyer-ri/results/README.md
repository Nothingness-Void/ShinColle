# Results

本批次于 2026-09-03 使用 `C:\Program Files\Java\jdk-17.0.3.1` 完成最终验证；最终轮包含 `Grudge`/`BlockGrudge` 恢复映射回归。

| 检查 | 命令 | 结果 |
| --- | --- | --- |
| T0 | `git diff --check` | exit 0 |
| T1 | `.\gradlew.bat compileJava` | exit 0 |
| T1 | `.\gradlew.bat processResources` | exit 0 |
| T2 | `.\gradlew.bat runGameTestServer` | exit 0 |

GameTest 结果：`All 250 required tests passed`。完整测试日志：[run/logs/latest.log](../../../run/logs/latest.log)。结构化摘要：[ship-01.json](ship-01.json)。

本批次没有资源、渲染、GUI 或客户端代码变更，因此没有重复启动客户端；对应 no-change 范围见 [decision.md](../decision.md)。
