# Results

此目录仅保存整理后的精确证据，不纳入完整运行日志、临时文件或截图。完整日志保留在记录中的外部路径；日志摘要有 SHA-256 以便核对来源。

- [verification.json](verification.json)：JDK、实际命令、退出码、逐阶段与最终摘要、最终日志校验和。
- [source-evidence.md](source-evidence.md)：0aad501 三处历史查证与 Forge47 实际票据 API。
- [fixtures.md](fixtures.md)：passiveAi、Raiden、ration 的夹具隔离及归因边界。
- [changed-files.md](changed-files.md)：本次全部实现与文档文件清单。
- 测试命令、覆盖及门槛的权威入口：[测试矩阵](../../../MIGRATION-TEST-MATRIX.md#parity-fix-teitoku-morph-skill310310)。

本轮文档收口未重跑生产输入未变的游戏测试；引用最后一次生产代码编辑后的通过结果。不把服务端启动或 NBT round-trip 解释为客户端视觉/真实重启验收。

## integration 最终重验

九批集成后的新证据见 [integration-merge.md](integration-merge.md)；上方 310 项是专题历史结果，不是 integration 的最终数量。
