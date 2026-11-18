# Batch: 2026-11-18-full-ship-behavior-reaudit

## Question

这个批次只回答：全量 64 个注册舰船变体（45 个基础舰种 + 19 个敌对 `*Mob` 变体）、公共基类生命周期、AI、隐身/发光、坐骑、召唤物、投射物与实体模型渲染是否严格遵守 1.12.2 源码语义，是否仍存在已写代码中的行为偏差或遗漏？

## Scope

- 舰种/class ID：全 45 个矩阵基础 class ID 及对应 19 个 `+2000` `*Mob` 敌对变体（共 64 个活动注册规格）
- 行为族/模块：M1 生命周期与敌我身份、M4 AI/移动/骑乘、M5 逐舰周期行为/光环/潜艇隐身/栖姬坐骑、M6 战斗属性/攻击通道/闪避/资源消耗、M7 导弹/光束/飞机/连装炮/浮游要塞、M8 舰娘与坐骑模型状态/缩放偏移/隐身发光与裁剪框
- 旧版入口：`src/main/java/com/lulan/shincolle/entity/**`、`ai/**`、`utility/CombatHelper.java`、`client/render/**`、`client/model/**`、`init/ModEntity.java`、`item/ShipSpawnEgg.java`、`crafting/ShipCalc.java`
- modern 入口：`src/modern/java/com/lulan/shincolle/entity/**`、`item/LegacyShipSpawnEggItem.java`、`worldgen/HostileEncounter*`、`client/renderer/entity/**`、`client/model/**`、`gametest/GameplayParityGameTests.java`

## Out of scope

- 修改 `src/main/java` 或 `src/main/resources`（严格只读）
- `ID.java` 中无 1.12.2 实体源码与注册入口的 40 个 `HISTORICAL_SOURCE_GAP` 名称（遵守 D-009）
- 未经用户要求自动提交、推送或发版

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
