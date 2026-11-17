# Batch: B2-PACKET-PARITY

## Question

这个批次只回答：对照旧版 1.12.2 的 6 个网络核心文件（`C2SGUIPackets`、`C2SInputPackets`、`S2CGUIPackets`、`S2CEntitySync`、`S2CReactPackets`、`S2CSpawnParticle`，共 84 个有效 PID 与粒子/实体同步入口），现代 1.20.1 的 `ModNetwork` / `GameplayCommandType` / `ShipCommandAction` / Clientbound 包体系覆盖了哪些语义、哪些因 1.20.1 `ContainerData`/`SynchedEntityData` 已原生等价无需单独发包、还遗漏了哪些单人/通用同步或控制分支，以及如何按 1.12.2 语义补齐并通过全量 GameTest 验证。

## Scope

- 舰种/class ID：无（网络数据包、命令分发与实体/GUI/反应同步全集，M3 / M7 / M8）
- 行为族/模块：C2S GUI/Input 包、S2C Entity/GUI/React/Particle 包与单人高频交互入口
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- 多人队伍权限矩阵/结盟规则深度实现（归入 `B3-MULTIPLAYER-TEAM`，本批仅核对相关 packet 路由是否已存在/保留最小适配）
- `ForgeConfigSpec` 全量可配置化与跨 mod API（归入 `B6-INTERMOD-DETAILS`，本批仅审计 `SyncSystem_Config` 是否需要/已适配）
- 旧版 `src/main/java/com/lulan/shincolle/command/` 9 个原版聊天斜杠指令 `/ship*` 注册

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
