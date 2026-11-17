# Migration Flow Checklist

本文件只定义验收门，不记录当前进度和逐舰结论。当前状态见 [CURRENT_STATE.md](CURRENT_STATE.md)，测试映射见 [MIGRATION-TEST-MATRIX.md](MIGRATION-TEST-MATRIX.md)。

## 批次开始

- [ ] 从 [ROADMAP.md](ROADMAP.md) 选择唯一 `NEXT` 批次。
- [ ] 创建 `migration_batches/<batch-id>/` 并复制模板。
- [ ] 在 `protocol.md` 冻结 legacy 入口、modern 入口、行为断言和非目标。
- [ ] 确认工作区已有改动，不覆盖不属于本批次的文件。

## 批次完成

- [ ] 现代实现只位于允许目录，legacy 没有改动。
- [ ] 逐舰分派位于 catalog/helper，公共实体没有继续膨胀 class-id 分派。
- [ ] 行为断言覆盖本协议声明的 constructor/runtime、AI/combat、resource/save、反馈域。
- [ ] 按测试矩阵完成 T0-T2；资源或客户端改动追加对应专项门。
- [ ] `STATUS.md`、`decision.md` 和 `results/` 有可复核记录。
- [ ] 更新矩阵、`CURRENT_STATE.md` 和 `MIGRATION-CHANGELOG.md`，再提升下一批。

## 单人主线 V0

- [x] 客户端可启动并完成资源加载。
- [x] 进世界生成友方、敌对和 Boss，验证目标边界与基本战斗。（2026-09-16 用户人工确认通过）
- [x] 验证 Pointer、ShipInventory、命令、路线、护卫、停止、坐下、补给和存档恢复。（2026-09-16 用户人工确认通过）
- [x] 验证骑乘、变身、技能输入、beam/torpedo/bomb/aircraft/static projectile 和战斗反馈。（2026-09-16 用户人工确认通过）
- [x] 验证 GUI、模型、纹理、粒子、声音无崩溃、缺失资源或静默 fallback。（2026-09-16 用户人工确认通过）
- [x] 将真实客户端结果写入一个 V0 批次记录。

## 最终收口

- [x] M0-M9 的单人主线验收门均有批次证据。
- [x] 矩阵中的主线域没有未解释的 `PARTIAL`/`NEXT`。
- [x] `CURRENT_STATE.md`、`ROADMAP.md`、矩阵、测试矩阵和 changelog 互相只通过链接关联，没有复制冲突事实。
- [x] 完整 1.12 parity backlog（B1–B6）已全部完成并有可复核证据；40 个无实体源 `ID.java` 历史名称按 D-009 维持 `HISTORICAL_SOURCE_GAP`。
- [ ] 用户明确要求时才提交、推送或关机。
