# Migration Architecture

本文件定义代码、资源和文档的归属。它不记录某个舰种是否完成。

## 四个平面

| 平面 | 内容 | 权威入口 |
| --- | --- | --- |
| Control | 规则、当前事实、路线、模块边界、命名 | `AGENTS.md`、`CURRENT_STATE.md`、`ROADMAP.md`、`TRACKS.md`、本文件、`NAMING.md` |
| Question | 一个批次要回答的问题、冻结协议、过程、结论、验收证据 | `migration_batches/<batch-id>/` |
| Implementation | 可运行的现代代码、资源、测试 | `src/modern/java`、`src/modern/resources`、`src/test`、GameTest 入口 |
| Retention | 构建日志、客户端截图、临时输出和大文件 | 本地 `build/` 或外部存档；根文档只保留链接和摘要 |

## 代码所有权

| 路径 | 责任 | 规则 |
| --- | --- | --- |
| `src/main/java` | 1.12 语义真源 | 只读；只按批次协议定向阅读 |
| `src/main/resources` | 1.12 资源/数据真源 | 只读；现代资源只做必要格式适配 |
| `src/modern/java/.../entity/ship` | 实体公共生命周期、同步和薄协调 | 不继续堆积逐舰分派 |
| `LegacyShipBehaviorCatalog` | per-ship/per-family 行为语义入口 | 新增行为先放这里或同级 helper |
| combat/projectile helper | 攻击结果、资源消耗、冷却、miss/crit、召唤 | 复用既有 packet/reaction bus |
| AI goals/helper | route、follow、guard、target、supply、pickup 优先级 | 保持旧版优先级，只做 API 适配 |
| `network`、command service、menu | 单人命令和状态交互 | 先复用既有协议，不复制第二套状态通道 |
| `client`、model、renderer、screen、particle | 可见反馈 | 资源路径与模型来源必须可追溯 |
| `src/modern/resources` | 现代数据、配方、loot、模型、纹理 | 变动批次必须追加资源检查 |

## 文档与实现关系

一个批次目录只描述一个问题，并指向具体代码文件。代码注释不承担进度记录；根文档不复制批次结论。需要了解“为什么这样做”时读批次 `decision.md` 或根部跨批次决策文档。
