# ShinColle Migration Agent Rules

本文件只定义协作规则，不记录当前进度、舰种结论或历史结果。

## 核心读取顺序

每次接手只需先读：

1. [CURRENT_STATE.md](CURRENT_STATE.md)：当前唯一事实和交接快照。
2. [ROADMAP.md](ROADMAP.md)：当前唯一下一步。

只有在要执行下一批时，才继续读该批次记录、[TRACKS.md](TRACKS.md)、[ARCHITECTURE.md](ARCHITECTURE.md)、[NAMING.md](NAMING.md) 和矩阵目标行。只有批次 `protocol.md` 明确列出文件后，才读取对应的 legacy 与 modern 实现。不得因为“熟悉上下文”而重新遍历整个项目。

## 压缩安全规则

- `CURRENT_STATE.md` 必须保留分支、工作区边界、当前任务、唯一下一步入口、最近证据、阻塞和非目标。
- `ROADMAP.md` 必须保留队列顺序、当前 `NEXT`、进入/退出条件和批次更新顺序。
- 详细事实只保存在其权威文档；核心文档只链接，不复制长结论。
- 每个批次目录必须自包含问题、范围、入口、状态、决定和证据，压缩上下文后可独立恢复。

## 不可违反的边界

- `src/main/java` 与 `src/main/resources` 是只读语义真源。
- 实现只能写入 `src/modern/java`、`src/modern/resources` 和文档。
- 现代版本 API 差异只做最小适配；不得用新的架构替换旧版行为。
- `LegacyShipEntity` 保持生命周期和公共协调职责；逐舰/行为族分派进入 catalog 或同级 helper。
- 不回退或覆盖工作区中不属于当前批次的已有改动。

## 批次工作协议

- 一个批次只回答一个明确问题，默认覆盖 5 个同类舰种或一个强联动行为族。
- `protocol.md` 在开始读取代码前冻结：目标、范围、旧版入口、现代入口、验收断言和非目标项都必须写清楚。
- 先完成整批代码，再运行一次该批次的静态检查、构建和 GameTest；不要每改一个小点就启动游戏。
- 客户端、资源、渲染、粒子或 screen 改动在整批完成后统一做一次客户端烟测。
- 无差异审计也必须留下记录，避免下一次重复阅读；使用 `decision.md` 记录“旧版已匹配，无需改动”。

## 事实归属

- 当前事实只写 `CURRENT_STATE.md`。
- 下一步队列只写 `ROADMAP.md`。
- 舰种状态只写 `MIGRATION-SHIP-MATRIX.md`。
- 测试命令、覆盖范围和通过条件只写 `MIGRATION-TEST-MATRIX.md`。
- 跨批次设计选择只写 `MIGRATION-DECISIONS.md`。
- 单批次过程、证据和结论只写 `migration_batches/<batch-id>/`。
- 其他文档只能链接，不复制这些结论。

## 完成与暂停

完成一个批次后，更新该批次记录、舰种矩阵、`CURRENT_STATE.md` 和变更索引，然后停止在 `ROADMAP.md` 指定的边界。除非用户明确要求，不自动提交、推送、启动客户端或关机。
