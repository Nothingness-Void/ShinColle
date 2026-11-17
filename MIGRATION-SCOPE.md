# ShinColle 迁移范围与完成定义

更新时间：2026-11-16

## 目标

将 1.12.2 的单人主线行为迁移到 Minecraft 1.20.1 / Forge 47。旧版行为、数值、状态和资源语义是唯一真源；现代代码只负责承载这些语义并做必要的 API 适配。

## 硬约束

- 只修改 `src/modern/java`、`src/modern/resources` 和迁移文档。
- `src/main/java`、`src/main/resources` 和 `mc-1.12.2` 分支只读。
- Minecraft/Forge API 变化只做最小适配，不用现代设计替换旧版规则。
- 每个 per-ship 分派入口放入 `LegacyShipBehaviorCatalog` 或同级 helper；实体只负责调用。
- 没有旧版依据的行为不加入主线实现；不以“测试能过”证明语义正确。
- friendly、hostile、boss、servant、mount 必须分别保留旧版边界。

## 单人主线范围

- 活动舰种全集按旧版 `ModEntity.regShipEntity` 与实际实体源文件定义；`ID.java` 中没有实体实现/注册入口的历史名称不作为可推导行为来源。
- 舰娘和深海舰的生成、所有权、跟随、停止、坐下、护卫、目标选择和基本拾取。
- 驱逐、巡洋、战舰、航母、潜艇、运输、深海和姬级的主要攻击族、资源消耗、miss/crit、冷却和战斗反馈。
- 单人所需的 ShipInventory、路线/命令、补给、修理、存档、恢复蛋、配方、掉落和主线资源来源。
- 可到达的模型、纹理、投射物和 GUI 的稳定渲染，以及进世界客户端验收。

## 后续 Parity Backlog（SP 主线已收口，按队列推进）

- 完整多人 ally UI 和所有多人权限组合（`B3-MULTIPLAYER-TEAM`）。
- 完整旧版 packet-heavy GUI 页面与全部 C2S/S2C packet 分支（`B1-GUI-PARITY`、`B2-PACKET-PARITY`）。
- 默认 worldgen 与 chest loot 已于 `WORLDGEN-AUDIT` 审计完成；runtime worldgen 配置开关留作可选 backlog。
- OreDictionary → Forge Tags 的代码侧匹配审计（`B5-ORE-TAGS`，16 个 Forge Tag JSON 已存在，待核对 `InventoryHelper` 与配方引用）。
- 真实外部 inter-mod API 与不阻塞单人主线的冷门舰种/模型细节（`B6-INTERMOD-DETAILS`；`MorphCompatBridge` 保持软集成）。
- `ID.java` 中 40 个没有旧版实体源文件/注册入口的历史名称；它们需要先恢复独立旧版实现来源，不能凭名称重造。

## 完成定义

### 一个舰种行为切片完成

- 矩阵中有明确的 legacy class id 和旧版来源。
- 现代入口已接入 catalog/helper，实体没有新增重复分派。
- 行为覆盖 constructor/runtime、combat/AI、resource/save 中声明的域。
- 有针对该行为域的回归断言，且批末测试通过。
- 若涉及客户端资源，已在同一批次记录一次客户端结果。

### 单人主线完成

- `M0-M9` 的代码基线和批次回归通过。
- `MIGRATION-SHIP-MATRIX.md` 中所有主线域不再有未解释的 `P` 状态。
- `V0` 进世界验收完成并记录真实结果。
- 剩余 parity 项已明确写入 backlog，不伪装成已完成。
