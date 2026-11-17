# Migration Tracks

本文件定义模块的所有权和晋级条件，不记录当前某一行舰种的状态；当前状态见 [CURRENT_STATE.md](CURRENT_STATE.md)。

| Track | 负责问题 | 主要代码归属 | 晋级条件 |
| --- | --- | --- | --- |
| M0 | mod 初始化、注册、数据命名 | `src/modern/java/.../init`、`src/modern/resources` | 注册与资源覆盖检查通过 |
| M1 | 实体创建、生命周期、同步 | `entity/ship` 公共层 | 生成、移除、同步和存档 round-trip 通过 |
| M2 | fuel、ammo、grudge、morale、repair、rescue、cooldown | runtime state/helper | 新旧字段、缺字段、默认值测试通过 |
| M3 | command packet、command service、menu | `network`、`menu`、command service | encode/decode、失败反馈和菜单命令测试通过 |
| M4 | follow、guard、route、formation、target、supply、pickup | goals、AI helper、ship entity | 优先级和边界 GameTest 通过 |
| M5 | per-ship 行为与行为族分派 | `LegacyShipBehaviorCatalog` 及同级 helper | 每个目标行为族有旧版入口和 modern 断言 |
| M6 | 命中、miss、crit、消耗、冷却、召唤、掉落 | combat helper、behavior catalog、loot | 结果、资源和目标有效性 GameTest 通过 |
| M7 | beam、torpedo、bomb、aircraft、static/guided projectile | projectile、particle、reaction bus | 投射物生命周期和反馈检查通过 |
| M8 | model、renderer、screen、客户端提示 | `client`、model、screen、renderer | dev client 整批烟测无崩溃/缺资源/fallback |
| M9 | ore、recipe、loot、advancement、encounter、DeskReference | `src/modern/resources` 与内容 helper | 单人资源来源可发现且内容验收通过 |
| V0 | 版本收口、文档和发布证据 | 根文档、构建脚本、批次记录 | `MIGRATION-FLOW-CHECKLIST.md` 全部通过 |

## 晋级方式

- 一个批次可以同时推进多个联动 track，但必须在 `protocol.md` 中列出每个 track 的入口和断言。
- 只有代码、资源和测试证据都存在，舰种行才能从 `PARTIAL` 变为 `SLICE`/`Done`。
- “旧版已经等价、无需现代改动”是有效结果，但必须有独立审计批次记录，不能仅在矩阵写一句“已检查”。
- `M8` 的客户端验证是批次级闸门，不要求每个小提交都启动客户端。
