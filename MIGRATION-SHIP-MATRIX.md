# ShinColle 逐舰迁移矩阵

Review follow-ups 不改变逐舰状态：crane 的 tag 等价关系与旧 drop marker 持久化只做 legacy parity 修复；[范围及证据](migration_batches/2026-11-22-review-followups/result.md)。

更新时间：2026-11-22

Pointer client ownership 共通修复不改变逐舰 LOCKED 状态；[synced UUID 与回归证据](migration_batches/2026-11-22-riding-aircraft-formation/pointer-owner-result.md)。

共通D3墙顶缺少 legacy waypoint clearance 已修复，NF07/08/02/01/NF10 原生控制场景及双端回归通过；原 VM 场景分组仍待新包复测，不把控制场景冒充逐项验收。LEVITATION 保留明确 gap，禁止当前 LivingEntity 移植；逐舰 LOCKED 状态不变。Shimakaze +0.14 与残余穿模不变。见[当前详细证据](migration_batches/2026-11-22-riding-aircraft-formation/section-readiness-profile-result.md)。

GUI/audio 回归复核不改变任何舰种状态；[独立复核记录](migration_batches/2026-11-22-gui-audio-regressions/README.md)。

共通物品、补给与路线交互纠偏见 [方块与物品批次](migration_batches/2026-11-22-parity-fix-items-blocks/)。本批不新增舰种或改变逐舰状态，不将共通修正视为逐舰重新验收。

这是逐舰状态的唯一事实来源。当前批次只处理标记为 `NEXT` 的行；已经有独立批次证据的域不重复阅读，除非出现回归或明确的新证据。

当前 45 行均为 `LOCKED`，对应旧版行为域已有现代生产实现与批次审计证据；客户端 V0 人工验收已于 2026-09-16 在 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/) 确认通过，SP 主线已正式收口。未覆盖的边角与完整 1.12 特性保留在 parity backlog，待恢复 GameTest 回归护栏后按 [ROADMAP.md](ROADMAP.md) 推进。

19 个敌对 `*Mob` 变体的共享规模与战利品，以及长门、大和、天龙、爱宕、高雄和金刚四舰相关攻击边界的补充证据见 [舰种个体行为批次](migration_batches/2026-11-22-parity-fix-ship-variants/)。逐舰状态沿用下表。

## 状态码

- `BASELINE`：共用现代实体和数据管线已存在。
- `SLICE`：该行已有一个或多个旧版精确切片和回归。
- `PARTIAL`：仍有明确的旧版行为域需要补齐。
- `NEXT`：下一批优先处理。
- `LOCKED`：当前域已核对，暂不重复审计。
- `MANUAL`：代码基线存在，等待客户端进世界验证。

## 逐舰清单

| ID | 旧版类 | 现代 texture/spec | 已锁定行为域 | 当前状态 | 下一行为域 |
| ---: | --- | --- | --- | --- | --- |
| 0 | `EntityDestroyerI` | `EntityDestroyerI` | constructor、owner speed、light、pickup、mounted、combat cadence、resource accounting、runtime save | LOCKED | command / route / target boundary |
| 1 | `EntityDestroyerRo` | `EntityDestroyerRo` | constructor、owner haste、light、pickup、mounted、combat cadence、resource accounting、runtime save | LOCKED | command / route / target boundary |
| 2 | `EntityDestroyerHa` | `EntityDestroyerHa` | constructor、owner jump、light、pickup、mounted、combat cadence、resource accounting、runtime save | LOCKED | command / route / target boundary |
| 3 | `EntityDestroyerNi` | `EntityDestroyerNi` | step height、owner strength、light、pickup、mounted、combat cadence、resource accounting、runtime save | LOCKED | command / route / target boundary |
| 9 | `EntityHeavyCruiserRi` | `EntityHeavyCruiserRi` | night passive、owner resistance、mounted、combat cadence、resource accounting、runtime save | LOCKED | command / route / target boundary |
| 10 | `EntityHeavyCruiserNe` | `EntityHeavyCruiserNe` | night passive、push、mounted、command range、route/guard、target boundary、legacy waypoint NBT | LOCKED | target/charge cadence |
| 12 | `EntityCarrierWo` | `EntityCarrierWo` | carrier attack、air capacity、owner aura、emotion FX、command range、route/guard、target boundary、legacy waypoint NBT | LOCKED | recovery and combat cadence |
| 13 | `EntityBattleshipRu` | `EntityBattleshipRu` | heavy barrage、ring Luck、block target、damage profile、command range、route/guard、target boundary、legacy waypoint NBT | LOCKED | queued barrage edge cases |
| 14 | `EntityBattleshipTa` | `EntityBattleshipTa` | Rensouhou pool、light hook、model state、command range、route/guard、target boundary、legacy waypoint NBT | LOCKED | servant lifecycle edge cases |
| 15 | `EntityBattleshipRe` | `EntityBattleshipRe` | light laser、push、damage profile、mounted、command range、route/guard、target boundary、legacy waypoint NBT | LOCKED | post-attack motion edge cases |
| 16 | `EntityTransportWa` | `EntityTransportWa` | supply loop、task/cargo、mounted、attack eligibility、idle AP resource、64格Pointer实际crane派遣GT | LOCKED | route interruption / save |
| 17 | `EntitySubmKa` | `EntitySubmKa` | submarine path、invisibility、pickup、mounted、combat selection/cadence、idle SS resource | LOCKED | combat result / save |
| 18 | `EntitySubmYo` | `EntitySubmYo` | submarine path、invisibility、pickup、mounted、light missile、combat selection/cadence、idle SS resource | LOCKED | combat result / save |
| 19 | `EntitySubmSo` | `EntitySubmSo` | submarine path、invisibility、pickup、mounted、combat selection/cadence、idle SS resource | LOCKED | combat result / save |
| 20 | `EntityCarrierHime` | `EntityCarrierHime` | melee missile、mount、aura/heal、damage、air selection/capacity/recovery、launch height | LOCKED | boss runtime edge cases |
| 21 | `EntityAirfieldHime` | `EntityAirfieldHime` | mount、heal、air attack、damage、legacy model source/bake、model state bit 1、0.7F launch height、实际mount waypoint/Pointer host选择GT | LOCKED | boss/runtime edge cases / independent LEVITATION API |
| 26 | `EntityBattleshipHime` | `EntityBattleshipHime` | cluster heavy、mount、resistance aura、damage、legacy model source/bake、cluster projectile family | LOCKED | boss/runtime edge cases |
| 27 | `EntityDestroyerHime` | `EntityDestroyerHime` | mount、blindness effect、damage、legacy model source/bake、model state bits 0-5 | LOCKED | boss/runtime edge cases；[获批陆上开腿，水中保留旧 bit4](migration_batches/neoforge-1.21.1/decision.md) |
| 28 | `EntityHarbourHime` | `EntityHarbourHime` | railgun、mount、heal/attack effect、damage、legacy model source/bake、0.7F launch height、railgun particle | LOCKED | boss/runtime edge cases |
| 29 | `EntityIsolatedHime` | `EntityIsolatedHime` | black-hole effect、mount、dual attack effect、legacy model source/bake、model state bit 6 inverse rule | LOCKED | boss/runtime edge cases |
| 30 | `EntityMidwayHime` | `EntityMidwayHime` | mount、air height、heal/absorption、special runtime、model source/state | LOCKED | full parity backlog |
| 31 | `EntityNorthernHime` | `EntityNorthernHime` | floating fort、mount、dismount、emotion、launch height、model state | LOCKED | full parity backlog |
| 33 | `EntityCarrierWD` | `EntityCarrierWDemon` | friendly carrier role、dual aircraft、mount、air capacity/recovery、model state | LOCKED | full parity backlog |
| 36 | `EntityDestroyerShimakaze` | `EntityDestroyerShimakaze` | five-shot burst、servant pool、block target、servant lifecycle、hostile projectile boundary、model state | LOCKED | full parity backlog |
| 37 | `EntityBattleshipNGT` | `EntityBattleshipNagato` | AP strike、morale、engine state、boss color、phase cadence、model state | LOCKED | full parity backlog |
| 38 | `EntitySubmU511` | `EntitySubmU511` | submarine path、speed、invisibility、hostile scale、hostile combat/save defaults、selected model source/bake、scale branches、projectile boundary | LOCKED | full parity backlog |
| 39 | `EntitySubmRo500` | `EntitySubmRo500` | submarine path、speed、garuru、invisibility、hostile combat/save defaults、selected model source/bake、scale branches、projectile boundary | LOCKED | full parity backlog |
| 44 | `EntitySubmHime` | `EntitySubmHime` | hime submarine path、dual missile、side offsets/order、miss scatter、mount、model source/state/bake、projectile boundary | LOCKED | full parity backlog |
| 46 | `EntityBattleshipYMT` | `EntityBattleshipYamato` | beam phases、engine state、hostile cannon、target/cooldown boundary、model source/state/bake、scale branches、beam host boundary | LOCKED | full parity backlog |
| 47 | `EntityCarrierKaga` | `EntityCarrierKaga` | aircraft inventory、Zero/T、hostile scale、capacity/recovery、model source/state/bake、scale branches | LOCKED | full parity backlog |
| 48 | `EntityCarrierAkagi` | `EntityCarrierAkagi` | aircraft inventory、Zero/T、voice gate、capacity/recovery、legacy model source/bake、model state bits | LOCKED | full parity backlog |
| 49 | `EntityCAHime` | `EntityCAHime` | night crit/triple、push、hostile boundary、legacy model source/bake、TailBase/hat state branches | LOCKED | full parity backlog |
| 51 | `EntityDestroyerAkatsuki` | `EntityDestroyerAkatsuki` | rider state、fuel dismount、hostile state、attack cadence、equipment model state、legacy source/bake | LOCKED | full parity backlog |
| 52 | `EntityDestroyerHibiki` | `EntityDestroyerHibiki` | rider state、fixed eye height、hostile size、fuel dismount、attack cadence、equipment model state、legacy source/bake | LOCKED | full parity backlog |
| 53 | `EntityDestroyerIkazuchi` | `EntityDestroyerIkazuchi` | rider state、fixed eye height、hostile size、fuel/Raiden cleanup、attack cadence、equipment model state、legacy source/bake | LOCKED | full parity backlog |
| 54 | `EntityDestroyerInazuma` | `EntityDestroyerInazuma` | rider state、fixed eye height、hostile size、attack cadence/resource、fuel/Raiden cleanup、legacy model source/state/bake | LOCKED | full parity backlog |
| 56 | `EntityCLTenryuu` | `EntityCruiserTenryuu` | dash、skill phase、night/owner effect、hostile phase/scale、target/resource boundary、mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 57 | `EntityCLTatsuta` | `EntityCruiserTatsuta` | control wave、skill phase、night/owner effect、hostile phase/scale、target/resource boundary、mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 58 | `EntityCAAtago` | `EntityCruiserAtago` | inherited range/heavy attack、type-five missile gate、night reset、reaction effect、mounted、hostile scale、legacy model source/state/bake | LOCKED | full parity backlog |
| 59 | `EntityCATakao` | `EntityCruiserTakao` | inherited range/heavy attack、type-five missile gate、reaction effect、mounted、hostile scale、legacy model source/state/bake | LOCKED | full parity backlog |
| 60 | `EntityBBKongou` | `EntityBBKongou` | combo hook、owner health aura、cannon family、hostile scale/BossInfo、dimensions/mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 61 | `EntityBBHiei` | `EntityBBHiei` | combo hook、saturation aura、cannon family、hostile scale/BossInfo、dimensions/mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 62 | `EntityBBHaruna` | `EntityBBHaruna` | combo hook、luck aura、cannon family、hostile scale/BossInfo、dimensions/mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 63 | `EntityBBKirishima` | `EntityBBKirishima` | combo hook、night vision aura、melee damage、cannon family、hostile scale/BossInfo、dimensions/mounted、legacy model source/state/bake | LOCKED | full parity backlog |
| 72 | `EntitySSNH` / `EntitySubmHimeNew` | `EntitySubmHimeNew` | dual missile、depth path、morale/riding、invisibility、host/target boundary、legacy model source/state/bake | LOCKED | full parity backlog |

## 批次证据索引

- 舰娘核心公共资源、士气与断粮行为：见 [舰娘核心批次](migration_batches/2026-11-22-parity-fix-ship-core/)；不改逐舰注册、既有锁定域或敌我归属。

- `SHIP-03` class 16-20 的行为、攻击、资源与 runtime 审计：见 [SHIP-03 batch](migration_batches/2026-09-03-ship-03-combat-behavior/)，最终回归 `All 254 required tests passed`。
- `SHIP-05` class 30、31、33、36、37 的特殊 runtime、召唤物生命周期、攻击节奏、模型状态和 hostile servant 边界：见 [SHIP-05 batch](migration_batches/2026-09-04-ship-05-special-runtime/)，最终回归 `All 258 required tests passed`；Java 17 客户端启动、资源 reload 和单人集成连接通过，进世界人工交互留给 V0。
- `SHIP-06` class 38、39、44、46、47 的潜艇/Yamato/Kaga runtime、模型源与 scale 分支、双导弹、beam、host/miss 边界：见 [SHIP-06 batch](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/)，最终回归 `All 261 required tests passed`；Java 17 客户端启动、资源 reload 和单人集成连接通过，进世界人工交互留给 V0。
- `SHIP-07` class 48、49、51、52、53 的 Akagi/CA Hime/三驱逐 runtime、攻击边界、燃料解体、模型源与 `showEquip` 分支：见 [SHIP-07 batch](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/)，最终回归 `All 263 required tests passed`；Java 17 客户端启动、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `SHIP-08` class 54、56、57、58、59 的 Inazuma/Tenryuu/Tatsuta/Atago/Takao runtime、攻击/骑乘边界和五个旧模型 `showEquip` 分支：见 [SHIP-08 batch](migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/)，最终回归 `All 264 required tests passed`；Java 17 客户端启动、OpenGL、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `SHIP-09` class 60、61、62、63、72 的 Kongou/Hiei/Haruna/Kirishima/SSNH runtime、攻击/骑乘边界和模型分支：见 [SHIP-09 batch](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/)，最终回归 `All 265 required tests passed`；Java 17 客户端启动、OpenGL 4.6、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `HEAVY-SPECIAL-MISS` 共性 miss 语义审计：见 [heavy-special-miss batch](migration_batches/2026-09-04-heavy-special-miss/)。现代 catalog 特殊重攻击已保留 projectile-style miss，现有回归覆盖目标不掉血与 missed projectile；本批 `NO CODE CHANGE REQUIRED`。
- `HOSTILE-TARGET-RANGE` 敌对目标边界跟进：见 [hostile-target-range batch](migration_batches/2026-09-04-hostile-target-range/)。修正低整数攻击距离的 legacy `FollowMax + 2` 回退，并让 hostile Tenryuu/Tatsuta 技能替换目标保持旧版固定搜索半径与筛选族；敌对飞机/命中 actionbar gate 对照一致，无反馈代码变更。最终回归 `All 268 required tests passed`；V0 原生客户端人工验收仍待完成。
- `SHIP-UNIVERSE-AUDIT` 活动实体全集核对：见 [ship-universe-audit batch](migration_batches/2026-09-04-ship-universe-audit/)。旧版 64 个注册行（45 个 `BasicEntityShip` 基础类、19 个 hostile Mob 变体）与现代 64 个规格行、矩阵 45 个基础行完全匹配；没有活动舰种漏项。`ID.java` 中 40 个无实体源/注册入口的名称另记为 `HISTORICAL_SOURCE_GAP`，不计为可迁移实现。
- `ALL-SHIP-IDLE-SUPPLY` 公共待机/移动资源入口：见 [all-ship-idle-supply batch](migration_batches/2026-09-05-all-ship-idle-supply/)。45 个矩阵基础 class 已按旧版 `BasicEntityShip` / `BasicEntityShipSmall/CV` 继承层级接入 catalog；Hime 不因现代 hostile 标志被排除，19 个 hostile Mob 变体仍排除；Transport Wa 移动经验已补回。新增测试和后续全局回归均已通过。
- `COMBAT-REGRESSION-SUITE` 固定战斗回归：见 [combat-regression-suite batch](migration_batches/2026-09-05-combat-regression-suite/)。Nagato、Tenryuu、Tatsuta、Yamato 的失败用例按旧版入口完成夹具/断言收口，潜艇姬短距 ARC/DIRECT 规则同步为旧版语义；最终 `All 269 required tests passed`，无生产战斗代码变更。
- `ALL-SHIP-OVERRIDE-AUDIT` 全舰种实体 override 与攻击源审计：见 [all ship override audit batch](migration_batches/2026-09-05-all-ship-override-audit/)。64 个注册变体和 45 个基础 class 的现代入口覆盖无未解释遗漏；旧版光照伤害表、显式 damage type、飞机 direct source 和 Rensouhou 类型已完成最小迁移，最终 `All 270 required tests passed`。
- `SHIP-ATTACK-PROFILE-AUDIT` 攻击通道与特殊 hook 边界审计：见 [ship attack profile audit batch](migration_batches/2026-09-05-ship-attack-profile-audit/)。45 个基础 class、64 个注册变体和 19 个 hostile Mob 变体完成旧版 light/heavy/air 有效开关对照，历史批次回归 `All 271 required tests passed`。（注：2026-09-16 测试文件被意外还原至 `HEAD`，对应自动化用例需在 [ROADMAP.md](ROADMAP.md) 的 `GAMETEST-REBUILD` 补回。）
- `V0-CLIENT-GATE` 客户端与单人人工验收：见 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/)。Java 17 客户端启动检查与 2026-09-16 用户进世界单人人工验收均已确认通过，SP 主线收口。

## 批次锁定规则

- `SLICE` 不代表该舰种全部完成，只代表“已记录的域”已经有实现和回归。
- 迁移一个批次后，只把本次实际覆盖的域改成 `LOCKED`；不要把整行直接改成 Done。
- 下一行为域必须先绑定旧版类名和方法，再开始阅读；没有绑定来源的条目不得实现。
- 如果一次 targeted audit 没有发现差异，记录为 `LOCKED / no change`，并立即进入下一行，不重复启动测试。
- M8 的 pose/layer/进世界结果和 M6/M7 行为状态分开记录，不能互相替代。

## 客户端还原批次入口

[2026-11-22 客户端还原](migration_batches/2026-11-22-parity-fix-client/)：只链接批次问题、协议、决定与证据，不在本文件复制结论。
