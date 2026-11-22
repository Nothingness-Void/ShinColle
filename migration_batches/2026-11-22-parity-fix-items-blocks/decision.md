# 批次结论

本批只以旧版实际代码为语义真源。下表现代路径以 `src/modern/java/com/lulan/shincolle/` 为前缀；完整改动和删除文件见 [文件清单](results/changed-files.md)。

## 四轮 27 条逐条结论

| 编号／原问题 | 结论 | 旧版依据、恢复结果及主要现代文件 |
| --- | --- | --- |
| 01／第一轮 1：配置与容量 | 已修；起重机描述部分误报 | `ConfigHandler` 的小型／大型造船厂、VolCore、起重机及 ShipTank 四档配置恢复运行时读取；容量、速度、瞬间建造量和燃料倍率不再以硬编码取代。起重机不是已有罐容量硬编码，而是缺内部罐；旧 EU 容量仅配置、储能 TODO，不自创实现。涉及 `config/ShinColleConfig.java`、四类方块实体、`item/ShipTankItem.java`、`registry/ModItems.java` 和造船厂菜单。 |
| 02／第一轮 2：小型造船厂液体 | 已修 | 按 `TileEntitySmallShipyard` 与 `TileEntityHelper.decrLiquidFuel` 恢复 2000mB 岩浆罐、流体能力、根级液体 NBT 和禁止外抽；空间至少 800、岩浆至少 40 时，每 tick 消耗 40mB 换 `800 × 燃料倍率`。涉及 `SmallShipyardBlockEntity.java`。 |
| 03／第一轮 3：大型岩浆批次 | 已修 | `HeavyGrudgeBlockEntity.java` 删除现代 `1000mB → 20000` 批次，恢复旧每 tick 的 40mB／800 基础换能规则及倍率。 |
| 04／第一轮 4：大型 canBuild | 已修 | 旧 `TileMultiGrudgeHeavy` 仅要求目标功耗大于零且输出槽空；缺燃料只暂停推进，不清已有进度。涉及 `HeavyGrudgeBlockEntity.java` 的建造判定和推进。 |
| 05／第一轮 5：easyMode 释放 | 已修；用户已确认理解 | 普通模式扣压缩块／普通物品库存 9／1，easyMode 扣 90／10；输出仍一个对应压缩块／普通物品。涉及 `HeavyGrudgeBlockEntity.java`。 |
| 06／第一轮 6：方块破坏权限 | 已修 | 按旧方块 `removedByPlayer` 与 `EntityHelper.checkOP` 恢复主人、OP、创造玩家破坏权限，不能把盟友操作权限当作破坏权限。除大型造船厂、起重机，旧版确有同检查的小型造船厂、VolCore、路标也恢复；涉及对应五类方块和方块实体。 |
| 07／第二轮 A：起重机主控循环 | 已修 | 按 `TileEntityCrane` 恢复方块实体在 `tick > 64` 后每 16 tick 检查配对库存及停靠船，`CraneState == 2` 执行；装卸每项一叠，过滤器、NOT、元数据／标签／NBT、等待模式恢复；箱子到罐 64000mB、罐到箱子 16000mB、船侧按 `rateLiquid` 与泵／附魔／等级规则；运输舰旧版循环遍历四工作项各加经验，不额外按成功布尔值过滤。恢复粒子、红石、铃声、下一路标；删除船侧替代的直接搬运，仅保留停靠与状态协调。涉及 `CraneBlockEntity.java`、`LegacyShipEntity.java`、配置和粒子适配。 |
| 08／第二轮 A：EU 边界 | 无 IC2 时不工作；已删除现代替代 | 旧 EU 只在 `CommonProxy.activeIC2` 时执行，满／空判定仍是 TODO；现代无 IC2，不接 Forge Energy 或造船燃料作替代，不补造不存在的旧逻辑。涉及起重机和船侧删除路径。 |
| 09／第二轮 B：RouteEnergyAccess | 自创机制，已整体删除 | 旧版不存在船抽取／注入大型造船厂、VolCore、Polymetal 从机燃料能量的接口。删除 `RouteEnergyAccess.java`、实现、船缓存与 NBT／同步、调用、菜单和屏幕显示、三语文案及四项路线能量测试。 |
| 10／第三轮 1：物品堆叠 | 已修 | 旧 `ModernKit`、`TrainingBook` 继承 `BasicItem`，默认堆叠 64；`registry/ModItems.java` 删除两者 `stacksTo(1)`。 |
| 11／第三轮 2：变身修复桶补机 | 已修计数与持久化；战斗接线未完成 | 旧 `BucketRepair` 修复受伤玩家时，CV 轻／重舰载机各加 1。`BucketRepairItem.java` 与 `MorphProfile.java` 恢复航空变身计数、容量限制及 NBT；满血／无变身不消耗不补机。计数尚未被变身航空攻击读取或消耗，不能称为完整生命周期已恢复。 |
| 12／第三轮 3：变身现代化配置 | 已修 | 旧 `ModernKit → InteractHelper → Attrs.addAttrsBonusRandom` 按配置控制各属性上限；`MorphProfile.java` 改读 `ShinColleConfig.modernLimit()`，保留随机选项满后顺序寻找未满项的算法，读档不再截断为 3。 |
| 13／第三轮 4：未婚 100 级训练 | 已修 | 旧训练书在等级小于 150 时直接调用无婚姻钳制的等级 setter，允许训练至 150；不是未婚 100 级停滞但仍扣书。`MorphProfile.java` 的 setter／读档允许保留训练等级，普通经验升级仍保留婚姻上限；150 级不消费。`TrainingBookItem.java` 原有训练分支本身无需改。 |
| 14／第三轮 5：结核／飞机补机 | 已修 | 按旧 `InteractHelper`，多金属结核对 CV 有 50% 概率轻／重各加 1；玩具飞机分别随机加 1–3。`LegacyShipEntity.java` 恢复。 |
| 15／第三轮 6：喂食资源倍率 | 已修 | 旧喂食最终增量乘 buffed `GRUDGE`／`AMMO`；`LegacyShipEntity.java` 的怨念、轻重弹药和口粮路径恢复倍率，`CombatRationItem.java` 的变身食用也恢复怨念倍率。 |
| 16／第三轮 7：口粮吸引主人限制 | 已修 | 旧 `CombatRation` 没有同主人条件；`CombatRationItem.java` 删除新增限制，保留其他既有条件。 |
| 17／第三轮 8：OwnerPaper 情绪／消耗 | 部分误报；消耗已修 | 旧成功转让为 T_T；现代本来已有 `setEmotion(2)`，对应 T_T，不修改正确情绪。偏离在创造模式免消耗；`LegacyShipEntity.java` 成功后清空手中纸张，恢复旧版消耗。 |
| 18／第三轮 9：主人未签字转让 | 已修 | 旧 `InteractHelper` 要求两正数签名；若 A 是主人则目标为 B，否则目标为 A，不要求主人必须签名。`OwnerPaperItem.java` 恢复该选择，保留目标在线要求。 |
| 19／第三轮 10：拆船锤左击 | 已修 | 旧普通玩家伤害 `maxHealth × 1.1` 的实际结果决定死亡；`LegacyShipEntity.java` 删除清无敌时间、最低 10000 伤害、绕过本类 hurt 与强制 kill。伤害被拒绝时非创造仍扣耐久。 |
| 20／第三轮 11：创造拆船掉落 | 已修 | 旧 `InteractHelper` 只在非创造时掉材料和库存、扣耐久、播放拆船音效；现代对应代码移入非创造分支。创造模式仍移除船，不新增掉落。 |
| 21／第四轮 1：回收蛋免伤防火 | 已修 | 旧死亡回收用 `item/BasicEntityItem`，拒绝伤害并防火。新增 `entity/other/RecoveredShipItemEntity.java`，注册防火实体类型和物品渲染适配，`LegacyShipEntity.java` 使用它生成回收蛋；保护可保存重载。普通船蛋物品不统一增加保护。 |
| 22／第四轮 2：水面部署编辑权限 | 已修 | 旧 `ShipSpawnEgg` 显式检查世界可修改与玩家可编辑；`LegacyShipSpawnEggItem.java` 恢复 `mayInteract`、`mayUseItemAt`，拒绝不消费。 |
| 23／第四轮 3：生成碰撞拒绝 | 已修 | 旧部署没有现代额外 `noCollision` 失败门槛；`LegacyShipSpawnEggItem.java` 删除该条件，不删主人／经验检查。 |
| 24／第四轮 4：ShipTank 实际目标 | 已修；保留旧版疑似笔误的实际效果 | 旧先检查机器流体能力，却从 `stack.getCapability` 获取 `tilefh`，因此目标实际是物品自身，不是机器。`ShipTankItem.java` 恢复该实际目标；对已有流体 capability 的小型造船厂、起重机等，不灌入或抽出机器流体。不是按“看似应当”的意图新增机器传输。 |
| 25／第四轮 5：扳手选择持久化 | 已修；字段 static 猜测不准确 | 旧 `tilePoint`／`pointID` 是注册 Item 单例的实例内存字段，且仅客户端收集，不是 static，不是逐 ItemStack NBT。`TargetWrenchItem.java` 恢复客户端单例选择，网络只在配对时传两坐标，服务端校验；不引入服务端跨玩家共享选择。删除 NBT 选择与维度持久化逻辑。 |
| 26／第四轮 6：OPTool 操作及目标 | 已修 | 旧 `OPTool` 只通过 `TargetHelper` 数字键操作；删除 `OpToolItem.java` 新增的右键列表／右键实体切换。`MorphClientEvents.java` 和 `GameplayCommandHandler.java` 的键盘路径排除舰娘、召唤物与坐骑，保留原有服务端权限门禁。 |
| 27／第四轮 7：婚戒激活键 | 已修 | 旧读写 `isActive`；`MarriageRingItem.java` 将现代 `Active` 改回 `isActive`，不再自创存档键。 |

## 历史源码参考与决定撤销

- 当前旧版起重机的物品搬运存在半重构残片，执行时参照可编译历史 `0aad501` 的 `TileEntityCrane.java` 确认过滤器与搬运原意；该参考不表示本批重编译了整个旧版。
- `MIGRATION-DECISIONS.md` 中只找到 D-011 的“IC2 由 RouteEnergyAccess 替代”对应决定，现明确撤销；其余用户举例机制在该文件没有独立条目，未新增伪造的历史决定。
- 最终回归暴露上一轮起重机完成时的跟随距离 2 被同步回调截为 4。失败现场其他完成状态、计时、目标、红石都正确；同步与读档最小放开合法值 2 后通过，没有将断言改为接受错误值 4。见 [证据](results/README.md)。

## 未验证项与后续边界

- 起重机工作粒子使用 Dust 适配，不是旧自定义 Sparkle 渲染的逐像素移植；视觉未验证。
- 变身舰载机计数可补给和持久化，但未接入变身航空攻击的库存读取／消耗，不能据此宣称完整变身航空行为一致。
- 本批客户端未启动、未烟测；粒子、回收蛋渲染、菜单和实际客户端点选／按键交互没有视觉验收。
- 基线抵抗药水测试本次通过而独立基线失败，疑似不稳定；没有定位随机性或调度根因，不将其称为本批修复。唯一最终失败保留不修，详见 [基线对照](results/README.md)。
