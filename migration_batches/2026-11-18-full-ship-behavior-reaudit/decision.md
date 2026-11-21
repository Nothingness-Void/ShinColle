# Batch Decision: FULL-SHIP-BEHAVIOR-REAUDIT

## Result

通过两轮多 Worker 并发逐行审计（Round 1：7 路全舰种审计 + 4 路修复；Round 2：6 路跨子系统深查 + 5 路修复 + 独立服务端安全复审），将现代实现中全部 64 个注册舰船变体与共享子系统的隐藏偏差逐一对标 1.12.2 源码收口，新增 9 项综合 `@GameTest` 回归断言，全量 **`298/298`** GameTests 全绿通过。

## Key 1.12.2 Parity Fixes

1. **11 个深海栖姬基础规格恢复 1.12.2 友方身份（`ShipEntitySpecs`）**：
   - `eggMeta` `22, 23, 28, 29, 30, 31, 32, 33, 46, 51, 74` 在 1.12.2 全部继承 `BasicEntityShipSmall`/`BasicEntityShipCV`（大型造船厂建造与刷怪蛋孵化的可驯服友方舰娘，仅 `> 2000` 为 `BasicEntityShipHostile`）。修正为 `hostile = false`，彻底解除孵化成敌对 Boss、无法开包/指挥/拾取/共享婚戒光环与骑乘的连锁故障。
2. **潜艇隐身、同队发光、远程闪避与视锥防剔除（直接根治「时不时消失」问题）**：
   - `EntitySubmHime`（44）与 `EntitySSNH`（72）不再叠加触发普通潜艇的 `tickSubmarineInvisibilityBehavior`，且仅在友方启用婚戒效果且有怨念时执行 128t 隐身。
   - `LegacyShipEntity` 重写 `isCurrentlyGlowing()`（经 `FMLEnvironment.dist.isClient()` 安全委托 `ClientShipVisibilityHelper`），恢复 1.12.2 `BasicEntityShip.isGlowing()` 对同主人/同盟客户端显示隐身舰娘发光轮廓的行为。
   - `LegacyShipEntity` 启用 `noCulling = true` 并放大 `getBoundingBoxForCulling()`（对齐 1.12.2 `ignoreFrustumCheck = true`），防止大舰装边缘闪烁剔除。
   - `LegacyShipEntity.hurt()` 将真实射手实体（`damageSource.getEntity()`）传入 `canDodge`，修复投射物贴脸距离为 0 导致潜艇 `invisibleLevel` 远程闪避失效的问题。
3. **基类生命周期、AI 与自然刷怪消散对齐**：
   - 此项中采用现代士气阈值的决定[已撤销](../../MIGRATION-DECISIONS.md#d-012已撤销现代士气阈值和独立舰娘燃料)，其余结论不变。
   - 补齐 `BasicEntityShip` 的 32t 自动快修桶（含空母轻/重舰载机各 `+1`）、32t `SATURATION` 药水回血与 `+100*lv` 士气、128t `updateMorale()`（按现代 `1500/4000/8000/12000` 阈值：脱战 `< 5000` `+40`、`> 8000` `-10`；交战 `-11/-7/-5/-11`）及 256t 基础自动回血（`0.03 * maxHP + 1`）。
   - 新增 `LegacyShipFleeGoal`（`FleeHP` 撤退）与 `LegacyShipOpenDoorGoal`，为 `LegacyShipFollowOwnerGoal` 接入 `ShinColleConfig.canTeleport()`；新增 `despawnBoss`（`12000`）与 `despawnMinion`（`600`）配置并修正自然敌舰仅在 `> 128` 格立即消散、`32..128` 格按闲置计时消散。
   - 补齐 `EntityDestroyerAkatsukiMob`（`2053`）16t 探照灯光块放置与 `MarriageRingItem` 非激活态水下呼吸、手持 64t 害羞爱心及婚舰数量 Tooltip。
4. **逐舰特殊数值、阵型槽位、附魔与完整受伤管线对齐**：
   - **已撤销**：本项将 `LegacyShipCombatHelper` 攻击端接入 `dmgSvS()` 视为一致性修复的决定；其余结论不变。复核与落实见 [战斗与投射物批次](../2026-11-22-parity-fix-combat-projectile/decision.md)。
   - 修正 `NorthernHime`（31）重炮 `0.75F` 倍率、`NorthernHime`/`SSNH` 潜行下坐骑、`BattleshipRe`（15）与 `TransportWa`（16）骑乘 Y 偏移、`SubmYo`（18）轻攻击不叠加 AA/ASM、`Tenryuu`（56）瞬移目标候选格安全判定、`Tatsuta`（57）友方纯垂直 vs 敌对水平+垂直击退与光束轨迹坐标。
   - `LegacyShipStatTables` / `LegacyShipStats` / `LegacyShipEntity` 补齐 5 种阵型全部 `0..5` 槽位独立属性表与编队 `MinMOV + formation[MOV] * scale[MOV]` 航速同步；`ShipEquipmentProfile` 补齐 `EquipCalc` 装备附魔加成计算；`LegacyShipCombatHelper` 接入 `ShinColleConfig.dmgSvS()` 与舰载机宿主夜视判定。
   - `LegacyShipEntity.hurt()` 完整对齐 1.12.2：免疫摔落/窒息/仙人掌/饥饿伤害、友方掉入虚空传送回 `Y=4` 救援、纯魔法 `< 1% maxHP` 忽略、女神复活 `>= health - 1` 触发及完整 120t `goddessImmuneTicks` 无敌、抗性/抗火药水 `20% * min(4, lv)` 减伤、最小 `1.0F` 伤害保底、受击起身与 `-5` 士气；`TeitokuEvents` 补齐玩家 32 格与敌舰 64 格群体复仇传播；修正 `AntiAir`/`AntiSub` 不误伤蝙蝠/蜜蜂且不阻断原版恶魂/幻翼/溺尸/守卫者，并接入 `TargetClass` 自定义白名单。
5. **坐骑、召唤物、投射物、指挥棒、起重机、道具与模型渲染对齐**：
   - 修复 `LegacyShipMountEntity` 与 `LegacyMountEntity` 双人座位 `rotateXZByAxis` 的 X/Z 轴向反置与水面浮力；修复坐骑、舰载机、连装炮、浮游要塞渲染器缺失 `scale(-1, -1, 1)` + `translate(0, -1.501F, 0)` 导致的模型倒置钻地，补齐 `Glow*` 全亮渲染通道、敌舰 `shadowRadius` 缩放、情绪表情切换与坐姿。
   - `LegacyShipModelTransformCatalog` 补齐缺失的 15 个舰娘 `scaleLevel`（`0..3`）缩放偏移表；`LegacyShipModelStateCatalog` 补齐 `ModelCarrierHime`、`ModelSubmHime`（bit 2 `GlowEquipBase` 与平时隐藏鱼雷架）及全部 13 个深海基础舰（含 `DestroyerI/Ro/Ha/Ni` `isKisaragi` 发饰位）的部件显隐规则。
   - 修正舰载机 `3x`/`2.5x` 攻速倍率、基于母舰 `MOV` 的航速与可击落 HP/闪避/减伤；连装炮补齐敌对 `+0.15` 暴击与召唤物闪避/减伤；浮游要塞补齐可击落 HP 与黑洞特效；导弹消除 `ThrowableProjectile` 空气/水阻力衰减、补齐 `moveType=2` 鱼雷入水延迟重算与 `accY2` 加速、子母弹 5t 引信门控、黑洞弹 miss 照常生成引力场及可拦截属性。
   - 修正指挥棒 `friendlyFire=false` 移动回退、64 格水面/远距射线、左键非己方船不污染目标表、编队增删重置阵型、护卫同一目标二次点击取消；补齐钓鱼任务任意护卫方块支持与浮点等级+幸运药水加成；补齐起重机电源门控、`WaitMode` `1..4` 与红石模式 `1/2` 信号输出；补齐沉船回收蛋 `level - 1` 与经验清零、生存孵化 `level / 3` 经验等级消耗、手持战斗口粮 16t 吸引舰娘、变身饮用快修桶及 `SOUNDRATE` 语音概率。
