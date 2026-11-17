# Batch Decision

- 结论：`DONE`
- 影响矩阵：[MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md) 全 45 行 LOCKED；V0 闸门已通过。
- 关联模块：客户端启动、渲染/资源加载、单人世界人工验收。

## Facts established

- Java 17 `runClient` 可启动，Minecraft Forge 原生窗口存在，OpenGL 4.6 与资源初始化完成。
- 现有启动日志片段未见 ShinColle 崩溃、模型/贴图缺失或静默 fallback；原版 goat horn 声音警告、shader sampler 警告和 Realms 授权解析警告不构成 ShinColle V0 失败证据。
- 重新核对现代侧全集映射：矩阵 45 行、45 个唯一 class ID，全部为 `LOCKED`；现代主规格 45 个；矩阵到 `ShipEntitySpecs` 的映射缺口为 0，重复主规格为 0。
- 2026-09-16：用户人工确认 V0 单人世界验收通过，SP mainline 正式完成。

## Code changes

- 无 modern 生产代码变更。
- 无 legacy 文件变更。

## No-change audits

- 不因缺少原生窗口自动化而修改敌对目标范围、提示抑制、模型、GUI 或战斗实现；这些行为继续以旧版对照批次和 `271/271` GameTest 证据为准。

## Follow-up

- SP mainline 已收口。后续迁移工作转入 backlog 阶段（多人 UI、packet-heavy GUI、worldgen、inter-mod 等）。
