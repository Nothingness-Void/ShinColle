# Frozen Protocol — PARITY-FIX-TEITOKU-MORPH-SKILL

## Question / boundary

按用户分步简报严格还原 1.12.2 的戒指、UID、周期任务、区块加载、变身、技能宿主与编队语义。仅修改 `src/modern/**` 与文档；`src/main/**` 只读。保留 D-011 原生替代 Metamorph 的技术方案，不保留其上的无旧版依据机制。

本目录为用户要求的最终合并批次。此前各步按当时简报执行；原 `2026-11-21-morph-zero-pointer-team` 的局部记录合并于此，本 protocol 不冒充所有子步骤之前已经存在的统一计划。提交/推送仅在用户本轮明确授权后执行，不开 PR、不触碰 `codex/mc-1.20.1`。

## Legacy references

- `handler/EventHandler.java`：BreakSpeed、液体雾、LivingAttack/LivingHurt、玩家 tick、维度/登录/死亡/登出。
- `item/MarriageRing.java`、`utility/BlockHelper.java`、`proxy/ServerProxy.java`。
- `handler/ChunkLoaderHandler.java`、`entity/BasicEntityShip.java`：updateChunkLoader、calcShipAttributes、非 morph 坐骑分支。
- `intermod/MetamorphHelper.java`：受伤、获取、解除、decrSupplies；`utility/CombatHelper.java`、`BuffHelper.java`、`TeamHelper.java`。
- `playerskill/ShipSkillHandler.java`、`item/TargetWrench.java`、`capability/CapaTeitoku.java`、`network/C2SGUIPackets.java`、`item/PointerItem.java`。
- 历史 `0aad501`：BasicEntityShip.calcShipAttributes、BlockHelper.checkBlockIsLiquid、MetamorphHelper.decrSupplies；精确片段见 [source-evidence](results/source-evidence.md)。

## Modern references

完整受影响文件见 [文件清单](results/changed-files.md)。主要承载：
- TeitokuEvents/Helper/Data/SavedData、MarriageRingItem、RingClientEvents。
- ShipChunkLoader、ShinColleConfig、LegacyShipEntity、ShinColle。
- MorphHelper/Events/RuntimeState/DamageHandler、TargetWrenchItem、PlayerSkillHostResolver。
- 普通 LegacyShipMountEntity 与抽离的 LegacyMountControls；移除 Morph-only LegacyMountEntity/renderer/入口。
- GameplayCommandHandler、PointerCommandEvents、MorphInventoryMenu、客户端特效与本地化。
- GameplayParityGameTests、ShipChunkLoaderGameTests、MorphParityGameTests、MorphAndTeamParityGameTests。

## Acceptance assertions

- [x] 13 个行为域逐条对照旧版；误报/已有对应实现不重复添加，见 [decision](decision.md)。
- [x] 仅必要 API 适配；Forge 47 实际 API 与历史半重构处有证据。
- [x] UID/owner 条件、资源消耗、周期、空值/错误路径、跨队伍边界和存档兼容有回归断言。
- [x] JDK17 compileJava 与全量 GameTest；命令/覆盖/门槛只在 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#parity-fix-teitoku-morph-skill310310) 定义。
- [x] 最终提交前 diff/staged 检查；legacy 零改动，不提交运行日志、临时文件或秘密。
- [ ] 客户端视觉、真实服务器重启/旧世界迁移端到端：未执行，不按通过记录。

## Frozen non-goals

- 不引入空弹药/怨念自动解除、Morph 专属坐骑或新的技能宿主。
- 不增加旧版不存在的配置/协议总线；saveId 不新增持久化/同步契约。
- 不宣称现代客户端旧/新版本网络互通；删除命令枚举和实体后需两端同步更新。
- 不借夹具隔离修改 AI、Raiden 配对或口粮业务规则；不清理既有工作区改动。
