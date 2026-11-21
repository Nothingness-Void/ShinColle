# 批次决定

## 结果

13项审计的逐条处理与依据统一见 [审计表](evidence/audit-13.md)。第1—5步恢复旧版行为，仅做必要1.20.1 API适配；第6步仅合并文档、撤销历史偏离说明并修正引用，不更改代码。

状态/配置、跟随与阵型、守卫与路线、移动攻击、Selector、碰撞近战、游荡、拾取和舰船3D导航分别按旧版入口落实。FollowMax拾取范围在第4步复核时已由第1步恢复，属于已匹配项，不重复改公式。两个目标列表测试的随机失败为fixture候选污染，修测试隔离/候选集合，不改“候选>2才最近3随机”的生产规则。完整过程保存在evidence，不复写旧阶段的原始失败。

## 明确降级与未接入项

- 阵型粒子：旧数字类型25的 `ParticleTeam` 箭头现代未实现；使用已有 **`HEALING_SPARKLE`（HAPPY_VILLAGER绿色闪光）替代**。保留单播主人、维度与手持指挥棒/配置门槛；不复现箭头字形，也不区分标记4/6外观。没有客户端视觉验证。
- 飞机/坐骑 **未接入3D寻路**：旧 `BasicEntityMount`、CaH/AfH/HbH/CaWD（45度）、MiH/IsH/SuH（60度），Airplane/Zero（36度）、TMob/Takoyaki（30度）、FloatingFort（45度）也使用旧ShipPath。现代 `LegacyMountEntity`/`LegacyShipMountEntity` 与 `LegacyShipAircraftEntity` 直接继承Entity，缺少Mob导航/跳跃/移动控制和LivingEntity更新阶段，接入需独立移植移动生命周期，未扩大本次舰船接线范围。`LegacyShipMountEntity`只迁移其host舰船停止路径；现代为PathfinderMob的Rensouhou/RensouhouMob对应实体已一并接入60F helper。
- **debug路径粒子未接**：现代没有对应debugMode配置与旧路径点列表粒子包；未创建新开关或协议，不把已有普通粒子包说成路径调试已移植。
- 非LivingEntity飞机/坐骑目标仍受既有现代攻击接口边界限制；本批不扩写整个目标/伤害系统。客户端运动观感、多人同步、GUI与音量听感未新验收。

## API适配与保留项

六类自定义路径算法不换成vanilla PathFinder/NodeEvaluator。IntHashMap→FastUtil，ChunkCache→PathNavigationRegion，Material/Block判定→BlockState/FluidState，MathHelper→Mth；旧更新顺序、60F转向、液体/空中物理保留。现代原版navigator仍存在，用于旧版对应的路径清理，坐下清两个路径。

现代已有resolveEffectiveMoveSpeed承载阵型minMOV+formation MOV，helper读取该属性，不二次叠加；RangeAttack追击移除现代装备倍率，Rensouhou追击参数恢复1D。口粮0.75D、撤退1.2D等保留各自旧值。未借移植修正旧算法自身的可疑公式或下探界线。

半重构缺失处使用 **0aad501**，具体文件、行号与用途见 [对照清单](evidence/historical-source.md)，并非任意用历史版本覆盖现有真源。跨批次撤销索引见 [D-013](../../MIGRATION-DECISIONS.md#d-013ai移动与目标的现代偏离已撤销)。
