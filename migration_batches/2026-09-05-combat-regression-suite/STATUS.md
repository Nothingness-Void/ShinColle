# Batch Status

- 状态：`COMPLETED`
- 开始：2026-09-05
- 完成：2026-09-05
- 当前阻塞：无；269 个 required GameTest 全部通过。客户端人工验收仍按上层 V0 闸门执行，不属于本批次范围。

## Completed work

- Nagato：旧版 16 x 12 x 16 搜索盒、驱逐舰/Northern Hime 筛选、7650 阈值和每目标 150 士气均已与现代 catalog 对照；失败来自 GameTest 邻近结构污染，测试实体改到隔离的 y=200。
- Tenryuu/Tatsuta/Yamato：旧版技能阶段、计时、资源和冷却入口均已对照；失败来自直接调用实体 `tick()` 时测试器不递增 `tickCount`，冷却夹具改为模拟服务器进入非 128 tick 边界。
- Submarine Hime：旧版 `EntityAbyssMissile` 的短水平距离规则已对照；水平距离不超过 4 格时保留 ARC 类型编号但使用直线运动，测试不再把随机 miss 一律断言为 DIRECT。
- 本批没有新增生产战斗分派、协议或架构；只修正了与旧版不符的测试环境和断言。

## Verification

- `git diff --check`：通过。
- `compileJava`：通过。
- `processResources`：通过。
- `runGameTestServer`：通过，`All 269 required tests passed`。

## Evidence links

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results](results/)
