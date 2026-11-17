# Migration Documentation Index

本仓库采用“一个事实一个权威文件，其他位置只放链接”的文档架构。这个索引只说明归属，不复制结论。核心入口只有 [CURRENT_STATE.md](CURRENT_STATE.md) 和 [ROADMAP.md](ROADMAP.md)；不再维护 AI handoff 文档。

## 四个平面

| 平面 | 负责内容 | 入口 |
| --- | --- | --- |
| Control | 规则、当前事实、路线、模块、命名、范围 | `AGENTS.md`、`CURRENT_STATE.md`、`ROADMAP.md`、`TRACKS.md`、`ARCHITECTURE.md`、`NAMING.md`、`MIGRATION-SCOPE.md` |
| Question | 单一批次的问题、冻结协议、状态、决策、精确证据 | `migration_batches/<batch-id>/` |
| Implementation | modern 代码、资源和测试 | `src/modern/java`、`src/modern/resources`、`src/test`、GameTest 入口 |
| Retention | 大日志、截图、临时客户端输出 | 本地 `build/` 或外部存档；根文档只保留链接 |

## 权威文档表

| 文档 | 只负责 | 不负责 |
| --- | --- | --- |
| `AGENTS.md` | 协作规则和硬边界 | 当前状态、历史结论 |
| `CURRENT_STATE.md` | 当前事实快照和压缩安全交接信息 | 历史日志、下一步细节 |
| `ROADMAP.md` | 唯一下一步队列 | 当前逐舰状态、测试结果 |
| `TRACKS.md` | 模块所有权和晋级条件 | 当前模块百分比 |
| `ARCHITECTURE.md` | 代码/资源/文档归属 | 行为结论 |
| `NAMING.md` | 稳定命名和记录格式 | 进度 |
| `MIGRATION-SCOPE.md` | 范围、约束、完成定义、非目标 | 批次过程 |
| `MIGRATION-SHIP-MATRIX.md` | 逐 class-id 状态和下一行为域 | 构建命令、长历史 |
| `MIGRATION-TEST-MATRIX.md` | 测试类型、触发条件、批末策略 | 业务结论 |
| `MIGRATION-DECISIONS.md` | 跨批次设计决定 | 逐日过程 |
| `MIGRATION-CHANGELOG.md` | 已完成批次的短历史索引 | 当前队列 |
| `MIGRATION-FLOW-CHECKLIST.md` | 批次和 V0 验收门 | 逐舰清单 |
| `PORTING-1.20.1.md` | API/资源适配知识 | 当前迁移状态 |
| `PORTING-MISTAKES.md` | 已发生错误和永久防线 | 排期 |

## 新接手阅读顺序

`CURRENT_STATE → ROADMAP → 当前批次`。只有执行代码时，才按批次协议补读 `AGENTS`、`TRACKS`、`ARCHITECTURE`、`NAMING` 和适配文档。

已完成批次只读其 `decision.md` 和 `results/`；只有相关代码发生变化或出现新证据时，才重新打开旧版入口。
