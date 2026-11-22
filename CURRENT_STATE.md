# 当前状态

> 最新09:12：[E3/D4结果](migration_batches/2026-11-22-riding-aircraft-formation/followup-result.md)为当前入口，详细恢复见[FOLLOWUP](migration_batches/2026-11-22-riding-aircraft-formation/FOLLOWUP.md)。Forge offset+Pointer长距/crane+实际mount waypoint修复最终build/full364GT绿；待本地commit→普通merge Neo/API/gate/push（Forge继续禁止push），再stationary-owner mount图。Neo当前5eb457f含offset，四张factory/carrier/fort/cart有效图已存。F3水飞三种生存原生场景可升起未复现；RingFly残留经源码证实旧版相同，清理需行为变更授权；cache/audio和独立mount浮空药水API仍open。src/main未变；PR10 CI不在本任务。

> 最新08:00：[queue-final-result](migration_batches/2026-11-22-riding-aircraft-formation/queue-final-result.md)为当前证据入口。源代码门禁Forge362/362、Neo370/370绿；Neo4302073已push，Forge44bfc38仅本地（严格保持push暂停）。真实右键骑Airfield挂载成功；三种玻璃墙图仍缺，Shimakaze两张仅诊断图且仍见靴外露，不算视觉通过。原live单横停滞未复现。唯一NEXT为有效客户端factory夹具和剩余视觉，不再猜测改移动/偏移；PR10 CI未接管。

> 最新07:44：line-abreast动态六slot×384tick已完成，逐tick独立旧公式/4与7平方距离/continue条件/path端点记录通过，均进入旧arrival范围；原用户4.6–5.9停滞未复现，不做猜测修改。手动driver修复原生ServerLevel负责tickCount++的夹具时钟遗漏。Forge最终build19s、362/362 GT93s绿。OpenAL显式stop/custom sound audit无mod入口，vanilla/unattributed。下一步本地commit（Forge推送已被lead暂停）→Neo merge/API/gate/push→VM指定六图。详细line/openal-result在riding-aircraft-formation批次。

> 最新07:30：B2第二根因已证实：/kill的MAX伤害随机放大成Infinity→Neo DamageContainer Armor的Infinity-Infinity→HP NaN（seed0原始失败日志有tracked=true/hpNaN）。公共伤害放大结果限有限MAX；普通数值/随机序列不变。加上先前保存HP恢复，Forge build20s、361/361 GT69s；Neo build18s、369/369 GT54s绿（最终seed0）。证据inventory-book-layout/round2-result.md。提交同步后唯一NEXT=line-abreast动态跟随trace，之后OpenAL→VM指定截图。旧mayfly不动。

> 最新07:19：B2真实/kill+实体region磁盘重开已复现并修复：readAdditionalSaveData选variant的refresh(false)覆盖原HP，DeathTime0复活/受伤回血；最后恢复数值Health，保留DeathTime锁和缺失Health默认。新增3GT（friendly/hostile、多时刻、受伤高等级、空region、缺失/冲突Health）。Forge最终build15s、360/360 GT1m16s绿；D3 Neo9³barrier夹具根因已解，Neo2200559已push365/365，Forge已同步宽夹具。下一步提交push本端→Neo merge/API适配/门禁→动态line-abreast→OpenAL→截图。旧checkpoint不再是阻塞。详见inventory-book-layout/round2-result.md。

> 权威最新06:53：Forge d375353已push，build12s、357/357 GT54s绿；Neo本地merge c6fc3fd→bb8eb58（未push），build9s绿但365 GT中1个D3 mount绕玻璃角失败。同一失败三次停止重试，先诊断随机远坐标path/partial-path，再新增B2 /kill复现。源码已提交，剩余dirty仅checkpoint文档；缺失目标截图尚未做。详细错误、排除项、命令、SHA和新B2范围见[migration_batches/2026-11-22-riding-aircraft-formation/CHECKPOINT.md](migration_batches/2026-11-22-riding-aircraft-formation/CHECKPOINT.md)。下方旧checkpoints遇冲突以此为准。


> 最新07:09覆盖旧checkpoint：D3失败已定位为1.21 GameTest自动barrier封闭9³模板，目标和detour在模板外，并非已证实的游戏路径/远坐标bug。新增完整flight_space模板，原始随机远位置/墙/速度/160tick/所有断言不变，另断言目标AABB无遮挡；Neo build10s、365/365 GT42s通过，即刻commit/push，再按用户顺序调查B2 /kill。证据见[D3-resolution](migration_batches/2026-11-22-riding-aircraft-formation/D3-resolution.md)。旧checkpoint仅历史，不再是阻塞。

> 权威最新06:53：Forge d375353已push，build12s、357/357 GT54s绿；Neo本地merge c6fc3fd→bb8eb58（未push），build9s绿但365 GT中1个D3 mount绕玻璃角失败。同一失败三次停止重试，先诊断随机远坐标path/partial-path，再新增B2 /kill复现。源码已提交，剩余dirty仅checkpoint文档；缺失目标截图尚未做。详细错误、排除项、命令、SHA和新B2范围见[migration_batches/2026-11-22-riding-aircraft-formation/CHECKPOINT.md](migration_batches/2026-11-22-riding-aircraft-formation/CHECKPOINT.md)。下方旧checkpoints遇冲突以此为准。

本文件只保存当前事实与交接快照；历史入口见 [迁移变更索引](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。

<a id="当前任务"></a>
## 当前集成与任务边界

- 分支：`devin/mc-1.21.1-neoforge`；Minecraft1.21.1 / NeoForge21.1.267 / JDK21。
- 当前任务：merge Forge603a32d（已push，build16s、真实Curios5.14.1下357/357）。本端上一轮362/362通过；当前新F2/D3尚未验证。仅机械API适配，无force-push，src/main只读。
- 下一步：新GT的NeoForge事件/Optional/ItemStack组件适配、curios tag folder改单数item，然后安装Curios9.6和配置ring槽做build/fullGT，提交push；完成目标客户端截图。调查和边界见batch STATUS与round2-decision。
- 唯一下一步入口：[ROADMAP.md](ROADMAP.md)。无服务端验证阻塞；下述未验证项/遗留不因此消失。

## 最近证据与权威入口

- 最新06:50：后续共享helper的feet-ray shortcut在Neo失败，已Forge-first改成block-only swept AABB，GT加入真实已跟踪host并断言解析，全部8种实际profile均覆盖。最终Forge build12s、357/357 GT、54s通过（真实Curios5.14.1 ring槽），日志同round2路径。下一步push此补充，再Neo移植/完成merge、private QA gameDir完整门禁，最后截图。Neo原run/config与他人旧client FileWatcher冲突，改用外部init /home/ubuntu/shincolle-queue-gametest.init.gradle，复制run/gameteststructures到私有gameDir；不能关闭他人原client56623113。

- **最终Forge06:38（覆盖下方WIP checkpoints）**：最新source已完成build16s、357/357 required GT，含真实Curios5.14.1 ring配置槽的全能力/禁用/移除分支，日志/home/ubuntu/shincolle-queue-forge-round2-{build,gametest}.log。isRingFlying legacy缓存flag在scan中保留，不修改旧GameplayParity断言；禁用新增清理只依据owned标记清flying，不碰mayfly。所有Forge新代码已ready，下一步显式stage/commit/push，然后Neo merge+API适配+完整门禁+最终截图。下方写setRingFlying(false)的历史尝试已撤回，不能作为最终实现。Neo尚未合入F2/D3，也未push88952bf/f63d392。

- 最新checkpoint06:35：Forge此前真实Curios5.14.1配置ring槽后build19s和357/357通过；随后补充Curios关戒指仍应有legacy passive breathing/禁飞断言，发现scan只在移除时清flight。已改onRingScan也在data.isRingFlying&&!active清理owned飞行并setRingFlying(false)，不碰mayfly。optional Curios tick改为find(false)+ring.inventoryTick(...,-1,false)，与Baubles.onWornTick相同，确保关戒指仍有legacy被动呼吸。新最终build/fullGT正待工具shell，下次先读日志，不以先前357当最终。实际Curios配置在run/config/curios-common.toml slots=["id=ring;size=1"]（非跟踪文件）；先前API empty是未配置槽，随后错误是GT试图改getCurios不可变Map，现GT直接取真实ring handler保存/还原stack。规则：未装Curios或无配置槽只检查tag，不计集成；真实最终运行有ring槽，完整能力断言必执行。
- D3 helper跳过新路径root节点后实际357全量已过，全部8mount和fort路径断言已成功。F Attrs最终生产引用审计已做，仅默认constants及GT保留SCALE_*；GT也已过。全配方脚本再次83/83、坐标360 casts audit无raw Y通过。所有新源文件仍未commit，NeoForge f63d392还没merge此WIP也没push。两端工作树独立、共享.git refs。
- VM我们启动的客户端已经保存退出world并Alt-F4关闭，非他人window56623113不要动。stone/wrench截图已覆盖为正确稳定帧：/home/ubuntu/screenshots/shincolle-121-queue-waypoint-stone.png（石头在手无marker），...-wrench.png（wrench在手红蓝marker可见）；镜头相同，都是Neo此前362通过的源版本。其余飞机/骑乘/岛风/D3截图仍待最终Neo merge后客户端验证。Morph G/H counter-check未做；文档round2-decision完整记录源码gate，实际报告玩家NBT不可得，不能宣称已复现其具体profile选择状态。NBT fixture可用/home/ubuntu/shincolle-qa-venv/bin/python，独立worldlevel.Data.Player.neoforge:attachments.shincolle:teitoku中MorphProfiles=[]、MorphRuntime.SelectedClassId=-1；如需验证callback，离线添加LegacyClassId0 profile并selected0（仅私有复制世界）再按G/H。当前无Curios客户端，最终可copy Neo9.6 jar到私有gameDir/mods并配置槽。

- WIP 最后检查：Forge JDK17 build SUCCESSFUL in 18s，日志 `/home/ubuntu/shincolle-queue-forge-build.log`。Full GT `/home/ubuntu/shincolle-queue-forge-gametest.log` 失败两项：`guardinvalidtargetscancelandrestorefollow` 仍断言 nonpositive Y invalid（应按获批 min build height 更新夹具）；`hostilebossdropsonlyitslegacyegg` 仍断言立即蛋（应验证 deadline 前无蛋、推进 Death_Time 后正确蛋）。其余新测试未报失败，勿声称全绿。新增 inventory/book source guard 在英语文本比较失败：旧 chap0 text0d0 为 are a group/现代 is a group，实际译文不同；之前全部 page placement 比较已通过，需调查 recipe 页译文，不能声称英语完全相同。diff --check 无输出。尚未 commit WIP、merge 或 push。

- 本轮 GUI/audio 复核完成：仅恢复大型船坞旧布局/错误提示；雷达长名字和冻结整点报时按旧版保留，报时 A/B 已复现。双端门禁和 scale-8 GUI 见[复核记录](migration_batches/2026-11-22-gui-audio-regressions/STATUS.md)。最终同步推送后停止，不启动历史 NEXT。

- 最新 Destroyer Hime 取舍已获批准：仅陆上开腿，水中原样，显式 bit4 始终优先。最终使用既有模型 depth > 0（vanilla isInWater 漏掉水面悬浮薄层，已由实测排除）。Forge build 与 **344/344** GameTests、NeoForge build 与 **352/352** 均通过；两端 CPU 姿态各 1536 项，最终 VM 指定画面通过。[Forge 证据](migration_batches/rendering-backport-1.20.1/STATUS.md)、[NeoForge 本轮记录](migration_batches/neoforge-1.21.1/status.md#rendering-backport-and-approved-land-legs)。

- 此前渲染修复后的 JDK 21 `compileJava` 成功，全量 `runGameTestServer` **351/351 通过、0 失败**；资源检查为 0 个大小写路径冲突。舰娘站立/坐下与 hitbox、办公桌/起重机/小船坞/航点/火山核心的针对性截图与根因见 [客户端修复](migration_batches/neoforge-1.21.1/status.md#client-rendering-follow-up-ship-placement)。本轮未重跑完整 `build`，以下 PR #11 的 348 项结果是历史证据。

- PR #11 最后生产/测试代码修改后：JDK 21 的 `compileJava` 与完整 `runGameTestServer` 通过，`All 348 required tests passed :)`（原 340 项与 8 项兼容/诊断测试；0 失败）。本轮未重跑 `build`；初次移植的构建结果保留在批次记录。准确输出、范围与日志校验和见 [复核证据](migration_batches/neoforge-1.21.1/status.md#pr-11-review-follow-up)。1.20.1 的 340 项结果保留在 [历史集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)，不替代本轮结果。
- 重复实现的归并约束与编号变更只见 [D-018 集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)：菜单网络 D-015、客户端 D-016、crafting D-017、Teitoku D-018，不覆盖 D-012～D-014。
- 既有起重机关联与夹具证据：[物品方块](migration_batches/2026-11-22-parity-fix-items-blocks/results/README.md#integration-merge)、[crafting 集成验证](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md#integration-merge)。
- 舰种身份与状态只见 [舰种矩阵](MIGRATION-SHIP-MATRIX.md)；本次不扩大逐舰或客户端验收口径。

## 未验证与当前遗留

- NeoForge 21.1.267 在 `LivingIncomingDamageEvent` 被取消后直接返回而未弹出 damage container；普通原版实体的独立取消监听器、戒指及坐骑免疫均实际复现同样的记录滞留。保留正确的伤害前取消语义，不在 ShinColle 监听器里擅自修改引擎栈；详见复核记录。
- 仅完成本轮舰娘位置与指定方块/物品的针对性截图检查；全舰型/骑乘组合、Glow、GUI 热区、HUD/粒子仍未全面视觉验收，不能以编译或服务端通过替代。旧桌子所在区块可能需一次方块/光照更新，详见批次记录。
- 真实 Curios、真实多人网络互通、跨重启票据与完整旧世界升级未验。ForgeCaps、嵌套 ItemStack、流体与坐标已有合成旧数据测试；这不是用户实际存档升级证据，实际升级前仍应备份。
- 除用户批准的 Destroyer Hime 陆上开腿视觉例外，未发现必须接受的玩家可见行为差异；这不是所有客户端/第三方模组场景已完全等价的声明。旧基线遗留仍如下。
- [客户端粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) 的 17 类自定义粒子仍未完整移植；部分作业/女神实现不代表整类完成。阵型 `HEALING_SPARKLE` 仍用原版近似。
- crafting 未接回的配置仍包括 `Attrs_*`、`Consume_*`、`Attack_*`、`Exp_*`、`Slots_*`、`Death_Time`、`Mod_IC2`、`Polymetal_Ore` 等，详见 [crafting 决定](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md)。
- 飞机/坐骑未接入舰船 3D 寻路，见 [AI 审计](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。
- 变身没有玩家可用的外部激活入口；不恢复自创激活/档案切换。MorphProfile 缺击杀数与航空机库存；已有变身舰载机计数的补充/持久化未接入航空攻击。见 [客户端激活边界](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) 与 [最终批次验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。
- 非目标：本次不补完这些遗留，不以服务端全绿宣称全面 parity，不新增旧版无依据的玩法/开关。
