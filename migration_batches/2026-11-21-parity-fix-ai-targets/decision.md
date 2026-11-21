# 第4步：目标与杂项AI

## 冻结协议

- 前置本地提交：e2c9a74，保存第1—3步；禁止push。
- 只修改现代AI、实体协调与其GameTest。src/main只读。逐条复核后恢复旧语义，不重写远程攻击、模型、网络、配置或装卸系统。
- 旧入口：AIHandler:76-111，TargetHelper.Selector:80-253、SelectorForHostile:336-398、setRevengeTargetAroundPlayer:578-598，EventHandler:516-533，BasicEntityShipHostile:180-197，EntityAIShipAttackOnCollide、EntityAIShipWander、EntityAIShipPickItem:63-221。
- 现代入口：LegacyShipEntity.registerGoals/canEngage/setUseAttackMelee/doHurtTarget，LegacyShipBehaviorCatalog目标列表，goal下OwnerHurtBy/OwnerHurt/HostilePlayer/RangeTarget/PickItem，TeitokuEvents.propagatePlayerRevenge，GameplayParityGameTests。
- 验收：移除两个主人目标goal及敌对玩家独立goal；保留事件复仇；友方按旧显式类型、PVP/反潜/可见性/自定义列表选择；敌对同一列表排序后前三随机；近战宽平方乘16、初始延迟20、32tick刷新、复用attackDelay；游荡1/180、友10/5/0.8敌12/1/0.8与旧禁用条件；拾取允许守卫/有目标，保留旧坐下骑乘装载钓鱼断粮限制，音量读volumeShip。测试覆盖正常、空目标、失效/禁用与边界。
- 最后运行compileJava与runGameTestServer，检查diff/status，仅中文本地commit。

## 复核决定

1. 两个主人goal确实额外注册；旧版只注册Revenge/Range，复仇已由TeitokuEvents事件传播。没有直接引用这两个goal的独立GameTest；保留事件与复仇合法性测试。
2. canEngage确实有主人最近伤害/追击主人捷径、当前目标捷径及Enemy宽接口；旧版是EntityMob或EntitySlime，对应Monster或Slime，不是任意Enemy。Selector的可见性、PVPFirst、AntiSS与自定义列表也须协调统一，不能在catalog又用Enemy放宽/缩窄。
3. 玩家优先goal确实存在；catalog原有统一列表和前三随机实现可复用，删除独立注册/类即可，同时使SelectorForHostile不接受普通动物怪物。旧Selector无距离门槛，距离属于RangeTarget的搜索盒/继续判定，不放入类型Selector。
4. 原生MeleeAttackGoal和舰种距离表确实偏离。CombatHelper.getAttackDelay已由LegacyShipCombatHelper.attackDelay移植，可复用。UseMelee控制安装/移除，不能因registerGoals在构造早期执行而永久遗漏后续NBT/GUI开启。
5. RandomStrollGoal的0.9速度和原生概率确实偏离，改为专用Wander；继续条件只看路径是否结束，旧版不是每tick重查坐下/钓鱼。
6. 三个拾取门槛确实额外；FollowMax范围公式已匹配，为误报/已修复项，不改公式。原旧钓鱼、Crane门槛须显式保留；音量1.0须改配置。

## 实现协调

- 碰撞goal按旧mutex4映射JUMP，优先级15，仅友方UseMelee开启时安装；变体/NBT/开关变化时协调安装/移除。游荡优先级友24敌23；观察/闲看排25/26，避免新游荡mutex7被原现代7/8优先级的观察永久压制。未重写观察逻辑、浮水或远程攻击控制器。
- Selector必须与“已有指令/复仇目标能否执行攻击”分离；旧canEngage里当前目标捷径移到攻击入口专用校验。RangedAttackGoal对已经存在的目标使用canCommandOrRevengeEngage，保持中立目标指令与事件复仇可执行，不把中立动物混入自动RangeTarget。
- 攻击碰撞goal自有攻击计数器；复用已有伤害/特殊攻击hook，绕过普通入口的共享melee cooldown拒绝，避免第二个冷却造成实际AI间隔变长。普通doHurtTarget仍保留原冷却。
- 原分类优先测试使用未加自定义列表的Phantom，实际依赖Enemy/泛化飞行捷径，不是旧Selector。改为明确加入主人自定义列表的Ghast（旧EntityFlying），保留AA→ASM→PVP→普通列表顺序；移入独立batch，防止候选互扰。

## 检查过程

- 首次编译通过，315项GameTest中310通过、5失败。三个旧特殊攻击测试（specialCombatHooksApplyHeavyCooldowns、ssnhLightAttackLaunchesDoubleMissile、hostileTatsutaSkillKeepsLegacyFreeAttackEntry）暴露敌对主动攻击入口被自动Selector类型限制误伤：显式传入的Zombie不是RangeTarget候选，但旧攻击hook可接受。修正为敌对攻击入口继续用已有canCommandOrRevengeEngage；自动RangeTarget仍用严格Selector，不改测试放宽断言。
- 新近战测试在setTarget之后调用commandGuardBlock，而该方法:2881明确清空target。调整为命令后再设置目标，验证旧goal确实无额外指令/燃料门槛。
- 新混合列表测试的FakePlayer只加入level.players，未加入实体索引；getEntitiesOfClass查不到它，导致前三列表实际是三艘舰。改用原版helper.makeMockServerPlayerInLevel（其调用PlayerList.placeNewPlayer），并finally移除；该check用于反证生产列表错误的猜测。
- 补强新游荡的骑乘禁用/无燃料仍可游荡，以及装载拾取测试有实际物品，避免空候选造成假通过。

- 第二轮编译通过，312/315通过。SSNH测试在舰体放置实心块，显式攻击入口不应受自动Selector的OnSight门槛影响；将整个攻击执行入口而非仅敌对/当前目标与Selector分离。Tatsuta仍有catalog.performCombatHook内的Selector二次校验，导致charge hook未处理而回退普通攻击，现复用显式攻击校验，不改特殊攻击算法/测试。
- 原版makeMockServerPlayerInLevel在本Forge环境调用PlayerList.placeNewPlayer时抛出Connection.channel为空的NPE；改为已有FakePlayerFactory配合ServerLevel.addNewPlayer，仅进入实体索引，不建立网络连接，并finally removePlayerImmediately。

## 混合列表失败的最终定位

- 获授权继续诊断后，保留失败时goal数量、搜索盒、选中对象和附近实体的类型/id/坐标/距离/Selector结果，玩家另报创造、观察者与无敌状态。
- `/home/ubuntu/shincolle-ai-step4-diagnostic.log` 实测：goalCount=2；玩家#2030位于(338.5,100,498.5)，距离6，selectable=true，creative=false、spectator=false、invulnerable=false。不是mock玩家资格问题。
- 同一搜索盒里，#2021、#2025、#2032三艘友舰均位于(334.5,100,498.5)、距离2且可选，#2022在距离3；共5个可选候选，而非预期的一船一玩家。实际选中#2025（距离2、可选），不是当前fixture的near。这说明选中路径确实命中了超过2个候选的最近3随机分支，不是玩家优先或距离排序错误。
- 修复只在测试：独立batch保留，构建新候选前discard搜索盒内旧舰；显式断言初始候选集合恰好为当前一船一玩家，扩展后恰好为当前四个候选；64个随机种子断言结果仅属于最近三者并覆盖三者。生产随机/Selector不变。
- 初次隔离后315/315通过。随后补齐finally按实体引用清理全部fixture舰船，包括最后被移动到搜索盒外的目标，避免按搜索盒清理遗漏移出者；最终结果见STATUS.md。

## API边界

现代坐骑与舰载机实体为非LivingEntity，现有整个实体战斗/目标接口以LivingEntity为目标；本步不扩写所有战斗及投射物签名。LivingEntity范围内按旧Selector恢复；非LivingEntity攻击能力仍是既有移植边界，不能把泛化FlyingTarget当作旧飞机/导弹instanceof来冒充等价。
