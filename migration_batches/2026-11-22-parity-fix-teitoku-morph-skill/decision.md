# Batch Decision — PARITY-FIX-TEITOKU-MORPH-SKILL

- 结论：`COMPLETED_WITH_EXPLICIT_VALIDATION_LIMITS`（实现与服务端回归完成，不含视觉/真实重启验收）。
- 行为域：M1/M2/M3/M4/M6/M8；全局玩家与舰娘共享能力，不改舰种矩阵行状态。
- 跨批次约束：[D-018](../../MIGRATION-DECISIONS.md#d-018)。
- 测试方法/覆盖：[测试矩阵](../../MIGRATION-TEST-MATRIX.md#parity-fix-teitoku-morph-skill310310)；精确运行证据：[results](results/README.md)。

<a id="facts-13"></a>
## Facts established — 全部 13 项

以行为域归并为 13 项：classId=0 归入第 11 项；Morph-only 坐骑删除归入第 12 项。此编号覆盖全部分步简报，并非省略最后的 classId/编队补修。

| # | 结论与旧版依据 | modern 承载 / 改动边界 |
| --- | --- | --- |
| 1 | 补戒指能力[2]液体挖掘：EventHandler 209–225，持戒启用、cap>0，original×5×(1+min(婚数,cap)×0.2)。 | TeitokuEvents 双侧 BreakSpeed；客户端婚数/戒指状态与配置已有同步可复用。液体判断历史查证见 source-evidence。 |
| 2 | 补能力[3]液体雾：EventHandler 230–268，负配置禁用，0 或婚数达到阈值近似无雾。 | client-only RingClientEvents；EXP density 无直接现代对应，以 near/far 除以旧密度比率，最小比率0.0001。仅 API 近似，尚无视觉验收。 |
| 3 | 补能力[4]火伤免疫：EventHandler 497–506，持戒/启用、非负阈值及婚数满足时取消火伤并灭火。 | LivingAttackEvent + IS_FIRE；无新增免疫机制。 |
| 4 | 补玩家骑普通坐骑时 FALL/IN_WALL 免疫：EventHandler 487–495。 | 最终仅 LegacyShipMountEntity；前期覆盖的 Morph-only LegacyMountEntity 随第12项整体删除，不保留无依据类。 |
| 5 | 还原16t首枚戒指扫描及旧键 isActive：EventHandler 725–764；背包优先，Baubles 对应 Curios。丢戒只关 flying，不清 mayfly/ringFlying；无NBT不重置旧active。旧右键/结婚不直接声明持戒。 | 双侧 START 扫描及状态变更同步；复用反射 Curios；修 markRingState/incrementMarriageCount 的无条件 hasRing。按用户要求 Curios 首枚戒指也读取 NBT，旧 Baubles helper 本身仅查存在。 |
| 6 | 玩家/舰娘 UID 初值100，<100无效；计数器不领先现存UID才推进到 UID+100000，保留有效 capability/entity UID。ServerProxy 583–606、舰娘缓存分支及696–706；EventHandler 637–648。 | TeitokuSavedData 与玩家/舰娘注册恢复，正常分配不无故跳号；旧从1开始实现已撤销。 |
| 7 | EventHandler 616–705：32t UID检查，tick64/每256t刷新当前队伍与全量同步，每128t unit name，同首个16t边界 initSID。 | 复用现有完整同步与初始化标志；HostileEncounterSpawner 已有128t周期，确认后不重复添加。 |
| 8 | ChunkLoaderHandler 18–30、BasicEntityShip.updateChunkLoader 与 EventHandler 558–580：罗盘 LevelChunkLoader>0；初始申请需有效在线主人；已持票后离线不撤销。Mode_ChunkLoader 默认2、范围0..2（无/本块/3×3）。初始、128t、跨区块更新；撤装/移除/死亡/卸载/离维释放；重载释放旧票；Wrong location 恢复removed并下坐骑。 | ShipChunkLoader 使用实体UUID owner、ticking票据、差集更新、EnteringSection 和 validation callback；Forge47 API 替代证据见 results。没有增加旧版不存在的离线停载配置。 |
| 9 | MetamorphHelper.onMorphDamagedHelper 197–320，经 EventHandler LivingHurt 调用：地形三类归零、药水缩放/早返、自伤及禁友伤、闪避取消、DEF、舰娘攻击倍率、抗性/光属性、伤害下限、濒死女神、满血及120t计时/特效。 | MorphDamageHandler、runtime immunity/emotion、共享伤害 helper 与女神粒子。decrSupplies 的8为类型而非数量：只消耗1个 Repair Goddess。配置外部键 Metamorph_HPRatio（0.1,0..10）与 Metamorph_DmgTakenRatio（0.2,0..1）对齐并实际使用。 |
| 10 | 历史0aad501 calcShipAttributes 明确只向 morphHost 同步 MAX_HEALTH = 20 + buffedHP×morphHPRatio；未找到向玩家额外应用移动/攻击/追踪/击退属性依据。 | 按旧HP公式应用；删除四类玩家 modifier 的应用，保留遗留modifier清理；解除恢复最大HP基值20。原生替代 Metamorph 本身保留。 |
| 11 | 扳手只本人 owner UID，获取不自动选中；新形态 Level1/Exp0/弹药怨念0/Married=true；敌舰击杀原已仅限敌舰，保留条件只改消息本地化。tick32登录解除、换维解除；登出/死亡用既有capability保存/Clone复制。未查到空弹药/怨念自动解除，故不加。 | TargetWrenchItem/MorphHelper/MorphEvents/TeitokuData、三种lang。没有对应字段的旧UID/Crane/Task/Kills不虚构profile字段。追加修复classId0：NO_SELECTION=-1、SelectionVersion=1；旧无版本0一律迁移无选择，旧正值保留，新版本明确0可保存；缺class字段不制造零号profile。 |
| 12 | ShipSkillHandler 41–53、505–527：骑BasicEntityMount→首个舰娘乘客→Metamorph，owner UID按旧条件。旧自动召唤普通坐骑受 !isMorph 限制，未找到 Morph 专属召唤入口。 | Resolver顺序/早返对齐；删玩家骑舰娘RIDER、Morph+专属坐骑MOUNT及全局技能开关误封普通宿主；移除toggleMount、Shift+G、包入口、注册和专属renderer/entity。共用控制抽为LegacyMountControls，普通舰娘坐骑和共享模型纹理保留。 |
| 13 | CapaTeitoku.addShipEntity 1114–1189：非本人拒绝、当前队伍重复则移除且saveId指向该位、首空位、满员按saveId覆盖并0..5循环；getShipTeamIDArray允许跨队伍同舰。saveId没有NBT/同步写入。PointerItem普通重复点击只聚焦，蹲下才移除。 | TeitokuData.addShipEntity，Pointer和网络AddTeam调用；网络空目标按旧清slot0。显式UID移除仅当前队伍；撤销死亡/拆解/所有者setter提前清空全部队伍，保留周期刷新仅清当前队伍的旧规则；saveId只runtime。现代以非正SID表示空位，不另造实体引用缓存。 |

## Code changes

完整文件（含删除项、新文件、文档及历史撤销标记）列于 [changed-files](results/changed-files.md)。未修改 `src/main/**`、构建配置或 CI。

## No-change audits / 不引入的机制

- 原生 MorphHelper 技术替代、已有敌舰解锁筛选、敌舰生成128t、现有capability存储与同步包继续保留。
- 不新增无资源自动解除、不添加额外玩家属性、不保留 Morph-only 坐骑/玩家骑舰娘技能宿主。
- 未发现 MIGRATION-DECISIONS D-001..D-011 中独立批准“UID从1”或“全队去重”的条款；它们是被纠正的旧实现，不伪造不存在的决定。旧宿主/HP/Morph范围的过宽结论已逐处标记，见 D-018。
- 删除命令枚举项后不承诺旧现代客户端/服务端网络混用；两端应同步更新。被删旧原生专属坐骑实体不提供额外迁移器，不能据此声称所有旧现代存档实体均可无损迁移。

## Test fixture adjustments

见 [fixtures](results/fixtures.md)。passiveAi、Raiden、ration 只改夹具隔离/清理/合法前置条件，不改相关AI、配对或口粮业务规则，不把未证实的竞争归因写成“确认根因”。

<a id="validation-limits"></a>
## Follow-up / 明确未验证

- 客户端液体雾、戒指客户端实际状态表现、女神粒子、受伤表情状态、本地化提示/菜单、坐骑入口移除的实际视觉与交互未验收。
- 安装 Curios 的真实运行路径及联机实际收包未端到端测试；仅反射路径和现有同步代码复核/服务端回归。
- 没有真实保存→关服→重启验证区块 ticket validation 清理，也没有真实旧世界的classId选择迁移、登出重登/死亡保存恢复端到端验证。NBT round-trip 和服务端启动不等于这些场景通过。
- 最后生产代码编辑后已运行编译与全量测试；本轮仅整理文档和提交，不无意义重跑输入未变的游戏测试。
