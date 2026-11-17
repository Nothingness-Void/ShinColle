# Batch: ALL-SHIP-OVERRIDE-AUDIT

## Question

这个批次只回答：64 个旧版注册变体的实体级 override 是否都能在现代共享实体、catalog/helper、目标 Goal 或渲染入口中找到明确承载；若有遗漏，是否存在可由旧版来源证明的单人主线缺口。

## Scope

- 舰种全集：45 个基础 class ID、64 个旧版注册变体。
- 旧版：注册表、基础船体继承层、AI/目标公共入口、45 个实际实体源文件。
- modern：`ShipEntitySpecs`、`LegacyShipBehaviorCatalog`、`LegacyShipEntity`、目标 Goal、攻击/资源/渲染入口。
- 输出：逐类 override 覆盖结论、真实缺口或 `NO DIFFERENCE` 证据。

## Out of scope

- 不恢复 `ID.java` 中没有实体源/注册入口的历史名称。
- 不修改 legacy 文件，不凭名称重造行为。
- 不重复启动客户端或重跑已完成的全局 GameTest，除非发现新的现代代码缺口。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)

## Completion

本批已完成。旧版注册变体、实体 override 和攻击源类型均已完成静态覆盖；发现的单人主线差异已在 modern 共享 helper/实体入口中完成最小迁移。详细决定和运行证据见 [decision.md](decision.md) 与 [results/](results/)。
