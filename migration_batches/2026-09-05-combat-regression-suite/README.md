# COMBAT-REGRESSION-SUITE

本批次只处理当前全局 GameTest 中固定失败的四个单人战斗回归。

## 固定失败

- `gameplayparitygametests.nagatomoraleeventmatcheslegacynearbytargets`
- `gameplayparitygametests.tenryuuheavyattackrunslegacyskillphases`
- `gameplayparitygametests.tatsutaheavyattackrunslegacyskillphases`
- `gameplayparitygametests.yamatoheavyattackuseslegacytwophasebeam`

## 目标

严格对照 1.12 对应实体和攻击入口，修复真实现代迁移差异；若测试假设偏离旧版，则只调整测试夹具或断言。不得用新的战斗架构替换旧版行为。

## 记录

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [decision.md](decision.md)
- [results](results/)

## 结果

- 四个固定战斗回归均确认不是现代生产逻辑缺失；按旧版入口修正测试夹具/断言。
- 批末 `runGameTestServer`：`All 269 required tests passed`。
- 本批未改生产战斗代码；V0 原生客户端人工验收仍由上层流程负责。
