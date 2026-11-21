# 决策与边界

- IntHashMap适配FastUtil Int2ObjectOpenHashMap；ChunkCache适配PathNavigationRegion；旧Material液体适配FluidState，门的铁/木分类使用DoorBlock.type().canOpenByHand；碰撞外形使用VoxelShape.bounds，空形状沿用旧默认整格高度。
- 队形移动速度沿用现代已有resolveEffectiveMoveSpeed（队伍minMOV+formation MOV并限幅），MoveHelper读同一MOVEMENT_SPEED属性，不重复叠加阵型。
- 旧canFly：友方漂浮药水、敌对false。恢复液体/空中无重力与各自0.8/0.91阻力；不借用vanilla水/空导航。旧额外travel调用及其与super.aiStep的先后顺序保留。
- 保留旧代码中未使用的getPathableYPos和原直线捷径公式，未借移植修复其旧疑点。
- Rensouhou/RensouhouMob旧helper60度，现代为PathfinderMob，已一并接入。
- 最终逐点速度审计：EntityAIShipRangeAttack:189为1D，Rensouhou与Mob变体:98/95安装同一RangeAttack，因此移除现代RangeAttack追击的额外装备速度乘数，Rensouhou追击由属性值参数改为1D。PickItem安装参数1D，Flee:107为1.2D，CombatRation:183为0.75D，均保留对应旧值。
- 旧BasicEntityMount及其舰装子类、Airplane/Zero(36度)、Takoyaki/TMob(30度)、FloatingFort(45度)也使用旧导航。现代LegacyMountEntity、LegacyShipAircraftEntity是直接Entity，缺少Mob跳跃/移动控制与LivingEntity更新阶段；接入需重做其独立移动生命周期，本步未扩大。LegacyShipMountEntity只迁移其host舰船停止调用。
- 现代没有debugMode配置和旧路径点列表粒子包；未新增自创调试开关/路径粒子协议。阵型粒子沿用既有降级，不在本步改变。
- 客户端运动观感、多人实体追踪和粒子视觉未验证。
