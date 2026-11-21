# 批次决定

范围为审计第 9–15 条。七项均有可定位的现代偏离，当前 `src/main` 能判定这七项语义。

| 条目 | 1.12.2 依据 | 实现 |
| --- | --- | --- |
| 9. Boss 阶段与动作循环 | `BasicEntityShipHostile.initAttrs`、`updateAITasks` 与各 `*Mob.setBossInfo`。规模大于 1 时创建血条，更新血量比例，仍使用舰种自身 AI。 | 删除血量阶段、额外攻击和召唤循环及四个运行时存档字段。保留血条和舰种原有攻击阶段。 |
| 10. 敌舰额外战利品 | `BasicEntityShipHostile.getDropEgg`、`initAttrs`、`onDeathUpdate`。掉落基础舰种蛋，规模 0/1/2/3 的概率分别为 1/5、1/3、9/10、必掉。 | 死亡物品只保留对应舰娘蛋，沿用旧版的整型随机条件和一次性掉落标志。移除额外材料、弹药与敌舰货物掉落路径。 |
| 11. 敌舰等级和士气 | `BasicEntityShipHostile.getLevel` 固定返回 150，`getStateMinor` 仅返回舰种，其余状态为 0。`BuffHelper.applyBuffOnAttrsHostile` 不使用士气。 | 查询返回固定旧版等级和零士气。敌舰属性跳过士气修正，存档省略等级、经验和士气等成长字段。 |
| 12. 按舰种推导精英与 Boss | `BasicEntityShipHostile.readFromNBT` 从 `scaleLV` 读规模，缺省为 0。`EntityHelper` 提供自然生成规模，`ShipSpawnEgg` 对敌舰蛋使用玩家随机源的 `nextInt(4)`。 | 通用部署和无规模存档默认 0。敌舰蛋由使用入口独立随机，自然遭遇仍传入自身规模。新档存储并同步显式规模，旧现代 `Boss` / `Elite` 只作为历史规模回读。 |
| 13. 特殊钩子冷却 | `EntityAIShipRangeAttack` 的轻重炮使用 `CombatHelper.getAttackDelay` 统一设置延迟，舰种重炮覆写只处理攻法。 | 实体与方块目标的特殊钩子使用正常攻击延迟，去掉倍率和 8 刻下限。 |
| 14. 爱宕、高雄和金刚级变身技 | `MetamorphHelper.handlePlayerSkillOfMorph` 调用对应实体常规攻击入口。爱宕、高雄及金刚四舰没有这些专属轻炮或重炮覆写。 | 删除 class ID 58–63 的两个专属变身技能、对应预览冷却及无调用者的自创实体攻击实现。 |
| 15. 敌舰击杀经验 | `BasicEntityShipHostile.getExperiencePoints` 返回 `10 + 30 × scale + nextInt(1 + 30 × scale)`。 | 接入现代经验奖励入口。规模 0/1/2/3 的范围分别为 10、40–70、70–130、100–190。 |

该批曾保留的自创海上 Boss 进度已由 D-017 撤销并删除，不再保留进度条件或触发器。

死亡动画结束时刻及旧版耐损毁掉落实体属于审计第 16 条，未纳入本批。敌舰掉落使用 Forge 1.20.1 的死亡奖励入口。
