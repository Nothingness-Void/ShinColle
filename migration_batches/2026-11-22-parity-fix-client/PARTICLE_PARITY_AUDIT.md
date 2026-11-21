# 粒子与 HUD 对照审计

真源为完整 `0aad501` 的 `utility/ParticleHelper.java`，不是局部重构后的旧目录。
类型号局限于各重载；位置型 type 28 是滴水，不是 type 36 家族里的表情 28。

## 已移植部分
- ParticleEmotion → LegacyEmotionParticle：0/default 与 1–34 全构造分支、帧数/循环/尺寸、淡入淡出和停留状态机、宿主跟踪、朝向相对位置及 Boss 偏移。现代 CUSTOM 显式开启/结束缓冲，沿用原四边形/UV/全亮；零尺寸粒子适配现代视锥剔除。
- ParticleTexts → LegacyTextParticle：五行纹理、0.8/0.2 四边形、25 tick（原后增过期判定）、初始向上 0.1、阻尼 0.9；实体文字从宿主高度×1.3 出发。TEXT_* 不再由战斗反馈重复叠烟。
- 北方嘴部效果为真滴水，保留水平 ±0.35 随机偏移，不使用表情精灵替代。
- HUD 为旧 HOTBAR 前置阶段、Position_HUD={0.5,0.6}、18px 槽/21px 间距、旧冷却文字/UV 遮罩、航空机数、按键高亮和准星；删除自创标题等文本（原为字面量，无独占语言键）。
- 三份贴图为原始字节的小写路径副本。只有 MorphProfile、没有舰实体时无航空机库存字段，显示初始化 0，不宣称已恢复库存管理。

## 剩余 17 类：仍是原版近似或缺少对应分支

重载标识：
A=spawnAttackParticleAt(x,y,z,lookX,lookY,lookZ,type)；
B=spawnAttackParticleAtEntity(entity,par1,par2,par3,type)；
C=spawnAttackParticleAtEntity(entity,type,double[])；
D=spawnAttackParticleAtEntity(host,target,par1,par2,par3,type,setAtkTime)；
E=spawnAttackParticleAt(text,x,y,z,type,int...)。

| 原类 | 重载/类型 | 未完成部分 |
| --- | --- | --- |
| ParticleLaser | A:14,21 | 纹理光束平面/动画 UV 未移植，原版轨迹不等价 |
| ParticleLaserNoTexture | B:8；D:0–6 | LEGACY_RE_LASER 仍用 DRAGON_BREATH；BEAM_* 用 END_ROD/ELECTRIC_SPARK/爆炸近似 |
| ParticleSpray | A:15–18,21–23,26,27,29,32,33,37–39,41,47；C:1,2 | 云/泡/水花/导弹近似缺逐类型颜色、大小、速度限制和寿命 |
| ParticleLightning | B:3 | 电火花不等价于自定义闪电几何 |
| ParticleStickyLightning | B:4,6,7,9 | 未实现宿主绑定多段闪电 |
| ParticleCube | B:7；D:1,2 | 未实现立方体及振动/目标联动生命周期 |
| ParticleLine | A:44–46 | 未实现多线高速光环 |
| ParticleSphereLight | B:11,24 | 未实现内向光束/球体 |
| ParticleGradient | B:12,14 | 未实现外扩渐变/双渐变 |
| ParticleCraning | A:40 | 未实现方块/模型作业粒子 |
| ParticleSparkle | B:13,17,21–23 | HEALING_SPARKLE 用 HAPPY_VILLAGER；填充/作业/眼/女神变体未实现 |
| ParticleSweep | B:15,16 | 未实现垂直/水平彩色剑气 |
| Particle91Type | A:21 | 未实现穿甲拳第四阶段复合粒子 |
| ParticleChi | B:1 | 未实现能量球 |
| ParticleDebugPlane | B:18–20 | 未实现身体/目标/调试平面 |
| ParticleSmoke | A:43 | 未移植可变缩放大烟子类 |
| ParticleTextsCustom | E:0,1 | 未实现自由文字/实体文字 |

共约 4243 行旧源码；不能把缺少实现的项说成已有完整原版替代。
此外 ParticleTeam（A:25；B:2，约 314 行）保留现有 LegacyTeamMarkerParticle，全模式机械一致性未验证，不计入上述 17 类。

其它 LAUNCH_SMOKE、HIT_EXPLOSION、AIR/MISSILE/AIRCRAFT/BOMB/TORPEDO、
FLARE_BURST、SEARCHLIGHT_MARK、BLACK_HOLE 及非文字战斗反馈仍保留已有实现/近似；
原版粒子分支的数量和运动并未在本次逐一认证。

## 证据解释
状态自检对正常/无效类型、无/移动宿主、小型/Boss 高度、UV、过期、文字阻尼作无头检查；
机械比较覆盖构造/tick/位置和贴图字节，API 桩不是 Minecraft 画面。
测试入口统一见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)；
本批次 HUD/粒子的实际渲染仍未视觉验证。
