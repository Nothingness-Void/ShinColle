# 半重构源码的0aad501对照

这些是第1—5步已执行的只读对照，第6步不重新取代码或替换实现。完整命令形式为 `git show 0aad501:src/main/java/com/lulan/shincolle/<path>`；行号指该历史内容。

| 文件 | 行/符号 | 对照原因与用途 | 原始记录 |
| --- | --- | --- | --- |
| entity/BasicEntityShip.java | 189-199 | 当前状态表半重构，确认FollowMin=3、FollowMax=12、FleeHP=35，与现有ShipStateHandler:62-64交叉核对 | [step1](step1.md) |
| reference/ID.java | 494-496 | 确认旧minor状态表10/11/12索引 | [step1](step1.md) |
| utility/ParticleHelper.java | 360-363 | 确认旧粒子25实际是ParticleTeam箭头；据此明确现代替代而非声称视觉等价 | [step2](step2.md) |
| entity/BasicEntityShip.java | 2113-2127 | 当前onLivingUpdate半重构，确认每16tick派发updateWaypointMove及其位于Guarding AI外 | [step3](step3.md) |
| entity/BasicEntityShip.java | 857-865：getMoveSpeed/getJumpSpeed；1863-1865：moveEntityWithHeading；2056-2067：更新顺序；3243-3245：canFly | 当前普通舰travel已转super、移动接口半重构缺失方法；确认原始移动速度、jumpSpeed=1、委托EntityHelper、super前更新导航及友方漂浮药水canFly。敌对canFly=false另由当前BasicEntityShipHostile核对 | [step5说明](step5/README.md)、[step5过程](step5/STATUS.md) |

未以历史版本绕过未实现项目：飞机/坐骑、debug路径粒子仍明确记录为未接入。
