# Batch: V0-CLIENT-GATE

## Question

这个批次只回答：当前单人主线构建能否启动客户端并完成 V0 原生窗口验收。

## Scope

- 客户端：Java 17 `runClient`、Minecraft/Forge 原生窗口、OpenGL、资源与音频初始化。
- V0 交互：进入世界、生成友方/敌对/Boss、命令与 GUI、战斗/投射物/反馈、存档闭环。
- 证据：客户端窗口进程状态、`run/logs/latest.log` 启动日志、自动化环境可接管性。

## Out of scope

- 不修改 legacy 或 modern 生产代码。
- 不把启动日志当作进世界人工验收。
- 不重复运行已完成的 GameTest 或逐舰迁移批次。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
