# Batch: B6-INTERMOD-DETAILS

## Question

这个收官批次回答：对照旧版 1.12.2 的 `/ship*` 聊天指令（`CommandHandler` / `ShipCmd*`）、`BlockLightAir` / `BlockLightLiquid` + `TileEntityLightBlock` 动态光方块与 Searchlight/Flare 联动、完整 `ConfigHandler` + `SyncSystem_Config(81)` + `polyAsMn` 运行时/条件配方切换、`ShipKills` / `Raiden` 角色标识、可选 Curios 婚戒联动，以及外置 mod（Metamorph / IC2 / Baubles）与 40 个 `ID.java` 无实体源历史名称边界，现代 1.20.1 如何补齐全部可移植真实缺口并完成全量 `1.12.2 -> 1.20.1` parity backlog 最终收口？

## Scope

- 舰种/class ID：全矩阵及联合体 `Raiden` 标识（外加 40 个无实体源 `ID.java` 历史名称 `HISTORICAL_SOURCE_GAP` 收口声明）
- 行为族/模块：M0–M9 遗留收官细节（Brigadier 指令、`ShinColleConfig` + `SystemConfig`、`polyAsMn` Tag/ICondition、动态光方块、击杀统计、`Raiden` 角色显示、Curios 反射软钩与外置 mod 等价边界）
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- 不根据 `ID.java` 仅存名称生成 40 个无旧版实体源文件的猜测性实体
- 不修改只读 `src/main/java` 与 `src/main/resources`
- 不在未获对应 1.20.1 第三方 mod 硬依赖下引入编译期强依赖

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
