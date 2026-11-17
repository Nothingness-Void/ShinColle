# Batch: B3-MULTIPLAYER-TEAM

## Question

这个批次回答（并按阶段先完成核心第一小部分）：对照旧版 1.12.2 的 `TeamData`、`TeamHelper`、`ServerProxy`、`CapaTeitoku` 与 `TargetHelper`，现代 1.20.1 `TeamData` / `TeamSavedData` / `TeitokuHelper` / `TeitokuSavedData` / `GameplayCommandHandler` 在多人队伍创建、解散、改名、冷却、同盟与敌对双向关系对称、playerUID 分配持久化、以及同盟/敌对目标与权限判定上有哪些差异？

## Scope

- 舰种/class ID：无（多人 TeamData、同盟/敌对权限、UID 持久化与相关同步，M2 / M3 / M4）
- 行为族/模块：多人队伍创建/改名/解散/冷却、Ally & Ban 双向关系同步、UID 分配与 `OwnerPaperItem` / `TeamHelper` 所有权与战斗同盟判定
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- `B6-INTERMOD-DETAILS`（`ForgeConfigSpec` 全量配置、外置 mod 联动、`/ship*` 原版聊天指令、`ID.java` 历史名称）
- 非 `Team`/权限相关的单舰 AI/模型/渲染/配方/worldgen

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
