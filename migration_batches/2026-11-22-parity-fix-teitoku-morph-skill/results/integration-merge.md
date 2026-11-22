# 最终 Teitoku / Morph / 技能合并验证

本记录承接九批 parity 集成的最后一次冲突合并。跨批次取舍只见 [D-018 归并约束](../../../MIGRATION-DECISIONS.md#九批集成的重复实现归并)，测试入口/门禁只见 [测试矩阵](../../../MIGRATION-TEST-MATRIX.md#九批-parity-integration-最终门禁)。

## 冲突处理证据

- GameplayClientEffects/GameplayParticleType：文字、表情、舰船动画、滴水和起重机粒子保持，加入 REPAIR_GODDESS。MorphClientEvents 保留旧技能 HUD/按键处理，拒绝 incoming 现代简化提示条。
- DeskReferenceScreen 按删除处理。incoming 相对共同祖先仅修改附录两句英文说明（获取不选择、坐骑与乘客宿主）；统一 DeskScreen 不含该现代附录，无需迁移。
- TargetWrench 的客户端内存方块选择/配对通道不变；实体获取使用 sameOwner，初始化档案资源/结婚标记但不改变选择。TeitokuData 不保留档案循环；-1、classId=0、addShipEntity、当前队伍语义和 UID 100 分配接入统一数据路径。
- LegacyShipEntity 同时保留 crane tick/关联清理和 chunkLoader tick；UID 使用 getOrCreateShipUid(UUID,savedUid)，并同步模型 UID。死亡保留旧蛋掉落，不恢复自创敌舰库存掉落，也不恢复生命周期全队清理。
- MorphHelper 保留外部能力承载、伤害与 HP 缩放，移除 Morph-only 坐骑；GameplayCommandHandler/Type 不恢复切换/激活命令；ModEntityTypes 保留普通舰娘坐骑和回收物实体。
- 三份语言 JSON 保留两侧有效文案，新增获取/重复获取翻译；解析检查无重复键。
- D-018 引用已同步最终批次、B1/B2/B6/REAUDIT 和索引。B1 同时保留 D-016 对简化界面的撤销，不恢复历史完整 parity 宣称。
- 编译首轮发现 MorphInventoryMenu 缺 MorphRuntimeState import、incoming 口粮夹具调用已删除 setAiAutoSupply；分别补 import、移除已不存在开关的设置（AutoCR 仍由 combatRationLevel 控制）。生产/测试最后改动后重新编译与全量 GameTest。

## 最终输出

独立编译退出码 0：
```text
BUILD SUCCESSFUL in 1m 31s
1 actionable task: 1 executed
```

全量 GameTest 首次运行退出码 0，无需偶发失败重跑：
```text
========= 340 GAME TESTS COMPLETE ======================
All 340 required tests passed :)
BUILD SUCCESSFUL in 3m 26s
11 actionable tasks: 4 executed, 7 up-to-date
```

- 编译日志：`/home/ubuntu/shincolle-teitoku-compile.log`；SHA-256 `3a0feb9558419633d6424d7f1b035485329c3bf426d527b9449069341ee0a87f`。
- GameTest 日志：`/home/ubuntu/shincolle-teitoku-gametest.log`；SHA-256 `aad3580a2e2ccaa3cb86d0c2a86f173a947ece872f2c2dfba5b0ec9c831bee9a`。
- 静态检查：现代源码无冲突标记、LegacyMountEntity/toggleMount/切换命令、RouteEnergyAccess、ModCriteriaTriggers/FriendlyShipDeployedTrigger 引用；语言 JSON 可解析且无重复键；相对 HEAD 的 whitespace 检查无输出。
- `src/main` 相对 `origin/codex/mc-1.20.1` 的 diff stat 无输出。

## 验证边界

未启动客户端，不声称视觉等价；未进行真实 Curios、多客户端网络互通、服务器重启后清票据或旧世界迁移验收。票据 GameTest 仅验证运行时 API/生命周期，NBT round-trip 不替代真实重启。Morph 档案外部激活、击杀数/航空机库存及航空攻击接入仍有缺口；服务端使用测试设置 active 不证明存在玩家激活入口。ParticleRepairGoddess 的独立实现不代表 17 类粒子缺口已整体完成。
