# 当前状态

本文件只保存当前事实与交接快照；历史入口见 [迁移变更索引](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。

<a id="当前任务"></a>
## 当前集成与任务边界

- 分支：`devin/parity-fix-integration`，基于 `origin/codex/mc-1.20.1`。
- 已集成九个 parity 修复批次：ship-core、combat-projectile、ai-movement、ship-variants、menu-network-command、client、items-blocks、crafting-world-config、teitoku-morph-skill。各批次过程和历史验收从 [索引](MIGRATION-CHANGELOG.md#九批-parity-集成收尾) 进入，不以专题历史分支/通过数覆盖当前集成状态。
- 最终 merge：`042a3696bef65c712614ea19ba4ae76ddec3f8af`；前序物品方块 merge 为 `ba0288157aa67cbad12c65b229f1d66106328e82`，crafting/world/config merge 为 `351e3ab5ca02ccf1cc1940cc019e5e99876a2d75`。
- 当前任务：VM A/B/C/D/E/F 回归，新增 F round2 和 D round3，Forge-first → merge NeoForge，无force-push。先前Forge ee1ffba已推送、354/354，NeoForge已merge88952bf并修复旧829de12内orphan config-base段为f63d392（未push），build和362/362已过。现在Forge有新WIP，不能以先前结果代替最终门禁。
- F2 WIP：Curios ring tag与Optional/LazyOptional适配、五组Attrs配置和实际stat接线已写，新增两GT；D3 WIP：shared LegacyShipObstaclePath复用aircraft/fort/all8mount，删除两noPhysics，新增GT。最近Forge build过；GT357项失败2（fort root waypoint贴顶僵住、Curios测试未配置ring slot，provider按设计返回empty）。已给shared helper跳过root节点，尚未重验；需修真实Curios测试夹具，不能降低断言。Forge5.14.1须外部init /home/ubuntu/shincolle-curios-forge.init.gradle（fg.deobf + mixin refmap mapping）；不要把生产obfuscated JAR直接放run/mods，已清走。Neo9.6 JAR在home待真实测试。
- 当前VM：我们启动的client shell3a8187，window58720265，独立/home/ubuntu/shincolle-queue-visual，从GUI Audio Regression复制；原始window56623113是他人的旧client，不要关闭。最后画面石头在手、waypoint无marker；之前/home/ubuntu/screenshots/shincolle-121-queue-waypoint-{stone,wrench}.png均是未稳定帧，不能作证。目标完整截图仍待。启动init /home/ubuntu/shincolle-queue-visual.init.gradle。私有NBT工具环境/home/ubuntu/shincolle-qa-venv可用（system Python无nbtlib，已安全隔离安装）。
- F2 morph：尚未改代码；unlock只添加profile，不选中、不激活；MorphHelper.openMorphScreen发SelectedClassId=-1，MorphInventoryScreen.containerTick因!hasProfile立即关闭；legacy C2SInputPackets.MorphGUI/GuiHandler要求真正capa.morphEntity。没有Metamorph桥时unlock不等于变身，现象有legacy gating依据，需定案。OpenAL report的完整日志路径在本机不存在；Stop在vanilla Channel.stop/destroy均能打印，模组无自有stop/loop实现，不能仅凭单条错误归因。PR10又通知CI失败，未委托CI loop，不处理但要报告。
- 全队列13项状态、配方完整diff及唯一未解项（line-abreast实际到位振荡，未证实迁移根因）见 [本批权威记录](migration_batches/2026-11-22-riding-aircraft-formation/STATUS.md)。不以源码匹配宣称实际轨迹完全等价。
- 唯一下一步入口 ROADMAP.md。完成NeoForge门禁和目标截图后更新结果并按Forge→NeoForge顺序推送，停止，不启动历史NEXT。
- 工作区边界：实现只在 `src/modern/java`、`src/modern/resources`；本轮其余改动仅迁移文档。`src/main/**` 继续只读，相对 `origin/codex/mc-1.20.1` 无差异。
- 唯一下一步入口：[ROADMAP.md](ROADMAP.md)。无服务端验证阻塞；下述未验证项/遗留不因此消失。

## 最近证据与权威入口

- 最新06:50：后续共享helper的feet-ray shortcut在Neo失败，已Forge-first改成block-only swept AABB，GT加入真实已跟踪host并断言解析，全部8种实际profile均覆盖。最终Forge build12s、357/357 GT、54s通过（真实Curios5.14.1 ring槽），日志同round2路径。下一步push此补充，再Neo移植/完成merge、private QA gameDir完整门禁，最后截图。Neo原run/config与他人旧client FileWatcher冲突，改用外部init /home/ubuntu/shincolle-queue-gametest.init.gradle，复制run/gameteststructures到私有gameDir；不能关闭他人原client56623113。

- **最终Forge06:38（覆盖下方WIP checkpoints）**：最新source已完成build16s、357/357 required GT，含真实Curios5.14.1 ring配置槽的全能力/禁用/移除分支，日志/home/ubuntu/shincolle-queue-forge-round2-{build,gametest}.log。isRingFlying legacy缓存flag在scan中保留，不修改旧GameplayParity断言；禁用新增清理只依据owned标记清flying，不碰mayfly。所有Forge新代码已ready，下一步显式stage/commit/push，然后Neo merge+API适配+完整门禁+最终截图。下方写setRingFlying(false)的历史尝试已撤回，不能作为最终实现。Neo尚未合入F2/D3，也未push88952bf/f63d392。

- 最新checkpoint06:35：Forge此前真实Curios5.14.1配置ring槽后build19s和357/357通过；随后补充Curios关戒指仍应有legacy passive breathing/禁飞断言，发现scan只在移除时清flight。已改onRingScan也在data.isRingFlying&&!active清理owned飞行并setRingFlying(false)，不碰mayfly。optional Curios tick改为find(false)+ring.inventoryTick(...,-1,false)，与Baubles.onWornTick相同，确保关戒指仍有legacy被动呼吸。新最终build/fullGT正待工具shell，下次先读日志，不以先前357当最终。实际Curios配置在run/config/curios-common.toml slots=["id=ring;size=1"]（非跟踪文件）；先前API empty是未配置槽，随后错误是GT试图改getCurios不可变Map，现GT直接取真实ring handler保存/还原stack。规则：未装Curios或无配置槽只检查tag，不计集成；真实最终运行有ring槽，完整能力断言必执行。
- D3 helper跳过新路径root节点后实际357全量已过，全部8mount和fort路径断言已成功。F Attrs最终生产引用审计已做，仅默认constants及GT保留SCALE_*；GT也已过。全配方脚本再次83/83、坐标360 casts audit无raw Y通过。所有新源文件仍未commit，NeoForge f63d392还没merge此WIP也没push。两端工作树独立、共享.git refs。
- VM我们启动的客户端已经保存退出world并Alt-F4关闭，非他人window56623113不要动。stone/wrench截图已覆盖为正确稳定帧：/home/ubuntu/screenshots/shincolle-121-queue-waypoint-stone.png（石头在手无marker），...-wrench.png（wrench在手红蓝marker可见）；镜头相同，都是Neo此前362通过的源版本。其余飞机/骑乘/岛风/D3截图仍待最终Neo merge后客户端验证。Morph G/H counter-check未做；文档round2-decision完整记录源码gate，实际报告玩家NBT不可得，不能宣称已复现其具体profile选择状态。NBT fixture可用/home/ubuntu/shincolle-qa-venv/bin/python，独立worldlevel.Data.Player.neoforge:attachments.shincolle:teitoku中MorphProfiles=[]、MorphRuntime.SelectedClassId=-1；如需验证callback，离线添加LegacyClassId0 profile并selected0（仅私有复制世界）再按G/H。当前无Curios客户端，最终可copy Neo9.6 jar到私有gameDir/mods并配置槽。

- WIP 最后检查：Forge JDK17 build SUCCESSFUL in 18s，日志 `/home/ubuntu/shincolle-queue-forge-build.log`。Full GT `/home/ubuntu/shincolle-queue-forge-gametest.log` 失败两项：`guardinvalidtargetscancelandrestorefollow` 仍断言 nonpositive Y invalid（应按获批 min build height 更新夹具）；`hostilebossdropsonlyitslegacyegg` 仍断言立即蛋（应验证 deadline 前无蛋、推进 Death_Time 后正确蛋）。其余新测试未报失败，勿声称全绿。新增 inventory/book source guard 在英语文本比较失败：旧 chap0 text0d0 为 are a group/现代 is a group，实际译文不同；之前全部 page placement 比较已通过，需调查 recipe 页译文，不能声称英语完全相同。diff --check 无输出。尚未 commit WIP、merge 或 push。

- 本轮 GUI/audio 复核完成：仅恢复大型船坞旧布局/错误提示；雷达长名字和冻结整点报时按旧版保留，报时 A/B 已复现。双端门禁和 scale-8 GUI 见[复核记录](migration_batches/2026-11-22-gui-audio-regressions/STATUS.md)。最终同步推送后停止，不启动历史 NEXT。

- 渲染回移：JDK17 build 成功，全量 **344/344** GameTests 通过；资源零大小写冲突，45 模型源码核对（仅精确豁免获批陆上开腿）及 64 变体/1536 姿态 CPU 测量通过。源提交、API 适配、review 不适用结论与视觉边界见 [本批记录](migration_batches/rendering-backport-1.20.1/STATUS.md)。

- 最后生产/测试代码修改后：JDK 17 独立编译与全量 GameTest 均通过，`All 340 required tests passed :)`。最终集成 GameTest 首轮全绿，未重跑；详细输出、首轮编译修复与日志校验和见 [最终集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)，命令/覆盖/门禁只见 [测试矩阵](MIGRATION-TEST-MATRIX.md#九批-parity-integration-最终门禁)。
- 重复实现的归并约束与编号变更只见 [D-018 集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)：菜单网络 D-015、客户端 D-016、crafting D-017、Teitoku D-018，不覆盖 D-012～D-014。
- 既有起重机关联与夹具证据：[物品方块](migration_batches/2026-11-22-parity-fix-items-blocks/results/README.md#integration-merge)、[crafting 集成验证](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md#integration-merge)。
- 舰种身份与状态只见 [舰种矩阵](MIGRATION-SHIP-MATRIX.md)；本次不扩大逐舰或客户端验收口径。

## 未验证与当前遗留

- 客户端画面/GUI 热区、模型/Glow/持物、HUD/粒子视觉未在本次集成渲染验证；无截图证据。
- 真实 Curios、真实多人网络互通、重启清票据与旧世界迁移未验。运行时票据 GameTest / NBT round-trip 不是实际服务器重启证据。
- [客户端粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) 的 17 类自定义粒子仍未完整移植；部分作业/女神实现不代表整类完成。阵型 `HEALING_SPARKLE` 仍用原版近似。
- crafting 未接回的配置仍包括 `Attrs_*`、`Consume_*`、`Attack_*`、`Exp_*`、`Slots_*`、`Death_Time`、`Mod_IC2`、`Polymetal_Ore` 等，详见 [crafting 决定](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md)。
- 飞机/坐骑未接入舰船 3D 寻路，见 [AI 审计](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。
- 变身没有玩家可用的外部激活入口；不恢复自创激活/档案切换。MorphProfile 缺击杀数与航空机库存；已有变身舰载机计数的补充/持久化未接入航空攻击。见 [客户端激活边界](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) 与 [最终批次验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。
- 非目标：本次不补完这些遗留，不以服务端全绿宣称全面 parity，不新增旧版无依据的玩法/开关。
