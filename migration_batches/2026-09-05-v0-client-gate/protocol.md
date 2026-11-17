# Frozen Protocol

## Legacy references

本批次不读取 legacy 源码；V0 行为清单以 [MIGRATION-FLOW-CHECKLIST.md](../../MIGRATION-FLOW-CHECKLIST.md) 和已完成的 `SP-CLOSE` 批次为准。

## Modern references

- `runClient` 启动入口与 Gradle 运行环境。
- `run/logs/latest.log` 客户端启动与资源加载证据。
- [CURRENT_STATE.md](../../CURRENT_STATE.md) 和 [ROADMAP.md](../../ROADMAP.md) 的 V0 状态入口。

## Acceptance assertions

- [x] Java 17 客户端进程可启动并形成 Minecraft 原生窗口。
- [x] OpenGL、Forge 注册、资源 reload、OpenAL 和纹理图集初始化完成，捕获日志无 ShinColle 崩溃、缺失模型/贴图或静默 fallback 信号。
- [ ] 进入单人世界并生成友方、敌对和 Boss。
- [ ] 完成 Pointer、ShipInventory、命令、路线、护卫、停止、坐下、补给和存档恢复流程。
- [ ] 完成骑乘、变身、技能输入、beam/torpedo/bomb/aircraft/static projectile 与战斗反馈流程。
- [x] 本次客户端结果写入批次记录，并明确记录自动化环境限制。
- [ ] V0 T4 全部人工验收通过。

## Frozen non-goals

- 不通过 PowerShell 或其他 native API 绕过 CUA 操作 Minecraft 原生窗口。
- 不因自动化环境无法接管窗口而改写战斗、AI、GUI 或渲染实现。
- 不在本批次引入新的协议、模型架构或兼容层。
