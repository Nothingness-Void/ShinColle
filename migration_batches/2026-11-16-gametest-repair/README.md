# Batch: GAMETEST-REPAIR-B4

## Question

这个批次只回答：`GameplayParityGameTests.java` 在 `HEAD` 基线失败的 9 个测试，逐项严格对照旧版 1.12.2 源码后，究竟是旧测试断言/夹具过时还是现代生产实现仍存在偏差；修正后能否让 B4（Worldgen）及现有全部 91+ GameTest 全绿通过。

## Scope

- 舰种/class ID：涉及 `ShipEntitySpecs`（含 Hime/hostile flags）、`EntityTransportWa`（16）、`EntityCAAtago`（58）与共享 goal/command/worldgen
- 行为族/模块：M4、M5、M6、M9（B4 Worldgen 测试收口）
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- 不重建与这 9 个失败项无关的 ~180 个历史回归测试（留待 `GAMETEST-BACKFILL`）
- 不在本批实现 OreDictionary → Forge Tags 代码迁移（留待紧随其后的 `B5-ORE-TAGS`）
- 不发明旧版 1.12.2 源码中不存在的新机制，只做 Forge/MC 1.20.1 最小 API 适配

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
