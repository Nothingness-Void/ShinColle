# Naming Rules

本文件只定义稳定命名，不记录状态。

## 文档

- 控制面使用大写根文档：`CURRENT_STATE.md`、`ROADMAP.md`、`TRACKS.md`。
- 批次目录使用 `YYYY-MM-DD-topic`，topic 使用小写 ASCII 和短横线。
- 每个批次固定包含：`README.md`、`protocol.md`、`STATUS.md`、`decision.md`、`results/`。
- `protocol.md` 是冻结问题和验收条件；`STATUS.md` 只写过程状态；`decision.md` 只写历史结论；`results/` 只写可复核证据。

## 批次与舰种

- 批次编号使用稳定主题 ID，不依赖临时的“第几个批次”。
- 舰种矩阵使用仓库中的唯一 class ID；展示名只是辅助，不作为唯一键。
- 反复出现的行为族使用固定小写 key，例如 `destroyer`、`cruiser`、`battleship`、`carrier`、`submarine`、`transport`、`abyssal`、`princess`、`installation`。

## 状态

允许的舰种状态：`BASELINE`、`PARTIAL`、`NEXT`、`SLICE`、`LOCKED`、`DONE-SP`。模块状态：`Done`、`Partial`、`Pending`。状态改变必须能链接到批次记录或测试证据。

## 测试证据

- 测试方法名描述行为和条件，不使用模糊的 `testEverything`。
- 资源/构建证据写入批次 `results/`，大日志只保留本地路径或摘要。
- `All N required tests passed` 只有在对应日志和命令存在时才能写入根部当前状态。
