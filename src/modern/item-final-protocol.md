# 最后一批物品复核

范围仅为回收蛋免伤防火、船蛋水面编辑权限和碰撞、ShipTank 机器传输实际目标、TargetWrench 选择生命周期、OPTool 操作与排除类、MarriageRing 激活键。

旧版入口：BasicEntityShip、item/BasicEntityItem、ShipSpawnEgg、ShipTank、TargetWrench、OPTool、TargetHelper、MarriageRing。现代入口：对应 Item、LegacyShipEntity、GameplayCommandHandler、MorphClientEvents、ModEntityTypes 和实体渲染注册。只修改 src/modern；旧版只读。

验收：保护蛋拒绝火焰及伤害并持久化；水面部署遵守 mayInteract/mayUseItemAt；碰撞不额外拒绝部署；机器 capability 存在时复刻旧版实际从 stack 获取目标；扳手选择为注册 Item 内存共享而非 stack NBT；OPTool 仅键盘操作且排除舰娘/召唤/坐骑；婚戒使用 isActive。新增/修改 GameTest 后 compileJava，再 runGameTestServer 写 /home/ubuntu/gt-final.log。已知基线两失败不修。非目标：变身战斗航空库存生命周期、其他模块审计。
