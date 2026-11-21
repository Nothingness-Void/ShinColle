# 历史与 API 查证

## 1. calcShipAttributes：0aad501 是必要的半重构澄清

查证命令：`git show 0aad501:src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`。
历史实例方法在1115–1119附近明确区分“给舰娘自身应用属性”与“apply attrs to player”：

```java
if (this.morphHost != null)
{
    this.morphHost.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
        .setBaseValue(20D + this.shipAttrs.getAttrsBuffed(ID.Attrs.HP) * ConfigHandler.morphHPRatio);
}
```

当前只读旧源码该段有静态方法混用 this 的半重构痕迹；不能据它给玩家发明更多属性。历史只明确同步 MAX_HEALTH，没有玩家移动/攻击/追踪/击退 modifier 的依据。舰娘自身的属性设置不等于玩家 host 的属性同步。原生 Morph 技术替代仍保留。

## 2. BlockHelper：0aad501 也有不匹配的 instanceof，但 material-liquid 语义明确

`git show 0aad501:src/main/java/com/lulan/shincolle/utility/BlockHelper.java`，160–167：

```java
public static boolean checkBlockIsLiquid(IBlockState state)
{
    if (state != null && (state instanceof IFluidBlock || state instanceof BlockLiquid || state.getMaterial().isLiquid()))
    {
        return true;
    }
    return false;
}
```

历史同样存在 IBlockState 与方块类型 instanceof 混用，不能照抄为现代API。现代 `TeitokuEvents.isInLegacyLiquid` 以脚部方块 `BlockState.liquid()` / `LiquidBlock` 承载 material-liquid 语义；不是自行加入水下、眼部或所有含水方块判定。雾的 EXP density → near/far 仅为 API 近似，未视觉验证。

## 3. decrSupplies 的8是类型，不是消耗数量

`src/main/.../intermod/MetamorphHelper.java` 771–815，历史 `0aad501` 同一路径803–809也一致：

```java
case 8: //use 1 repair goddess
    itemType = new ItemStack(ModItems.RepairGoddess,1);
break;
// ...
ItemStack itemGet = InventoryHelper.getAndRemoveItem(inv, itemType, 1, true, false, false, null);
```

受伤 helper 的 `decrSupplies(player.inventory, 8)` 因此消费1个 Repair Goddess。随后是满血、玩家120t hurt resistance、舰娘120t ImmuneTime、type13女神粒子、事件伤害归零；不是消费8份通用补给。现代保留对应数值与资源条件。

## 4. Forge 47.4.18 不提供用户举例的 TicketController API

项目 `gradle.properties`：Minecraft 1.20.1、Forge 47.4.18。对本机实际 universal jar 执行 JDK17 `jar tf`，匹配 `TicketController|RegisterTicketControllersEvent|ForgeChunkManager` 的类仅为：

```text
net/minecraftforge/common/world/ForgeChunkManager$LoadingValidationCallback.class
net/minecraftforge/common/world/ForgeChunkManager$TicketHelper.class
net/minecraftforge/common/world/ForgeChunkManager$TicketOwner.class
net/minecraftforge/common/world/ForgeChunkManager$TicketTracker.class
net/minecraftforge/common/world/ForgeChunkManager.class
```

JDK17 javap 输出（省略其它重载）：

```text
public static void setForcedChunkLoadingCallback(java.lang.String, net.minecraftforge.common.world.ForgeChunkManager$LoadingValidationCallback);
public static boolean forceChunk(net.minecraft.server.level.ServerLevel, java.lang.String, java.util.UUID, int, int, boolean, boolean);
```

检查的 jar 位于 Gradle `forge_gradle/maven_downloader/net/minecraftforge/forge/1.20.1-47.4.18/forge-1.20.1-47.4.18-universal.jar`，不是按其它 Forge/NeoForge 版本猜接口。现代实现使用本 mod ID + 舰娘 UUID + ticking=true；通过 `setForcedChunkLoadingCallback` 与 `TicketHelper.removeAllTickets` 清除此 mod 保存的旧实体/方块票据，不清其它 mod 的票据。事件用 EnteringSection.didChunkChange 适配旧 entering-chunk。

validation callback 已注册并通过编译/服务器启动，**没有真实保存后重启实验**，不能称为真实旧票清理端到端已验证。

## 5. 其它关键只读依据

- `ServerProxy.updatePlayerID/updateShipID`：初始100，现存UID超过/等于next时 +100000；舰娘保护也见同文件650–690。`EventHandler` 的玩家 <100 检查决定无效边界。
- `CapaTeitoku.addShipEntity` 1114–1189、`getShipTeamIDArray` 1628起：当前队伍开关、saveId覆盖以及跨队伍同舰。全库 saveId 引用仅初始化与 addShipEntity，没有NBT/同步写入。
- `C2SGUIPackets.AddTeam` 376–405 与 `PointerItem` 226–278：网络slot0、普通重复点击聚焦、潜行移除。`TeamHelper.updateTeamList` 221–255仅刷新当前队伍，未载入但有效SID保留。
- `ShipSkillHandler` 41–53、505–527仅普通坐骑、首个舰娘乘客、Morph三类；owner检查按 TeamHelper 的有效UID且相等。
- `BasicEntityShip` 自动坐骑受 !isMorph 限制。未找到 Morph 专属 summon action，也未找到空弹药/怨念自动 demorph；未以猜测增加机制。
