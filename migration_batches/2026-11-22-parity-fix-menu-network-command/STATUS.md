# Batch Status

- 状态：`COMPLETED`
- 开始：2026-11-22
- 最近更新：2026-11-22
- 当前阻塞：无

## Completed work

- 复核审计 8 条问题：全部属实（InvPage 以 `0aad501` 确认原意），按 1.12.2 还原；按钮 39 `NoFuel` 记录为不实现，理由见 [decision.md](decision.md)。
- 改写断言偏离行为的 GameTest（AutoSupply/RouteStay/单值跟随、`/shipcleardrop` 清除原版掉落、`hasPermission(2)` 权限），新增 `shipGuiButtonsAndInventoryPagingMirrorLegacy`；`passiveAiStopsAutomaticRangeTargeting` 改用测试结构相对坐标（原绝对坐标在新增测试后与其他结构重叠导致视线被挡）。
- 验证（Linux，JDK `17.0.20.1`）：`bash ./gradlew compileJava runGameTestServer --no-daemon` 通过，**`All 299 required tests passed :)`**（`299 passed / 0 failed / 0 skipped`）。

## Remaining work

- morph 模块后续复核 `MorphProfile` AI 字段与 `MorphBtn NoFuel`（超出本批次）。

## Evidence links

- 批次结论：[decision.md](decision.md)
- 机器可读结果：[results/parity-fix-menu-network-command.json](results/parity-fix-menu-network-command.json)
