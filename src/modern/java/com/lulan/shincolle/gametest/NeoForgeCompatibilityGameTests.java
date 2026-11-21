package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.event.LegacyLivingHurtEvent;
import com.lulan.shincolle.entity.other.RecoveredShipItemEntity;
import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.item.LegacyShipSpawnEggItem;
import com.lulan.shincolle.menu.RecipePaperMenu;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeoForgeCompatibilityGameTests {
    @GameTest(template = "gameplayparitygametests.empty")
    public static void recoveredDropImportsLegacyEggAndNestedShipInventory(GameTestHelper helper) {
        RecoveredShipItemEntity original = ModEntityTypes.RECOVERED_SHIP_ITEM.get().create(helper.getLevel());
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        saved.putString("id", "shincolle:recovered_ship_item");
        CompoundTag ship = new CompoundTag();
        ship.putInt("ShipLevel", 42);
        var owner = java.util.UUID.randomUUID();
        ship.putUUID("OwnerUuid", owner);
        CompoundTag swordData = new CompoundTag();
        swordData.putInt("Damage", 7);
        CompoundTag sword = oldStack("minecraft:diamond_sword", swordData);
        sword.putInt("Slot", 4);
        ListTag inventory = new ListTag();
        inventory.add(sword);
        ship.put("ShipInventory", inventory);
        CompoundTag eggData = new CompoundTag();
        eggData.put("RecoveredShip", ship);
        saved.put("Item", oldStack("shincolle:shipegg60", eggData));
        ListTag entities = new ListTag();
        entities.add(saved);
        CompoundTag chunk = new CompoundTag();
        chunk.put("Entities", entities);
        CompoundTag upgraded = net.minecraft.util.datafix.DataFixTypes.ENTITY_CHUNK.updateToCurrentVersion(
                net.minecraft.util.datafix.DataFixers.getDataFixer(), chunk, 3465);
        var loaded = EntityType.loadEntityRecursive(upgraded.getList("Entities", Tag.TAG_COMPOUND).getCompound(0),
                helper.getLevel(), entity -> entity);
        helper.assertTrue(loaded instanceof RecoveredShipItemEntity, "the world-upgrade path retains the custom drop type");
        ItemStack egg = ((RecoveredShipItemEntity) loaded).getItem();
        helper.assertTrue(egg.getCount() == 1 && LegacyShipSpawnEggItem.isRecoveredShipStack(egg)
                && LegacyShipSpawnEggItem.getRecoveredOwnerUuid(egg).orElseThrow().equals(owner),
                "old recovery eggs keep their stack and ship ownership after entity-chunk DFU and Entity.load");
        CompoundTag recovered = LegacyShipSpawnEggItem.getRecoveredShipData(egg);
        CompoundTag nested = recovered.getList("ShipInventory", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(recovered.getInt("ShipLevel") == 42 && nested.getInt("Slot") == 4
                && ItemStack.parseOptional(helper.getLevel().registryAccess(), nested).getDamageValue() == 7,
                "nested ship equipment is converted before the custom item entity parses its egg");
        RecoveredShipItemEntity modern = ModEntityTypes.RECOVERED_SHIP_ITEM.get().create(helper.getLevel());
        modern.load(loaded.saveWithoutId(new CompoundTag()));
        helper.assertTrue(ItemStack.isSameItemSameComponents(egg, modern.getItem()), "already-modern drop data is unchanged");
        for (CompoundTag invalid : java.util.List.of(new CompoundTag(), oldStack("shincolle:missing_item", new CompoundTag()))) {
            RecoveredShipItemEntity empty = ModEntityTypes.RECOVERED_SHIP_ITEM.get().create(helper.getLevel());
            CompoundTag invalidSave = saved.copy();
            invalidSave.put("Item", invalid);
            empty.load(invalidSave);
            helper.assertTrue(empty.getItem().isEmpty(), "empty and unknown drop items retain vanilla's empty fallback");
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void legacyNestedItemsAndFluidUpgradeWithoutRefilling(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        CompoundTag fluid = new CompoundTag();
        fluid.putString("FluidName", "minecraft:water");
        fluid.putInt("Amount", 1500);
        CompoundTag fluidData = new CompoundTag();
        fluidData.putInt("Marker", 99);
        fluid.put("Tag", fluidData);
        CompoundTag tankData = new CompoundTag();
        tankData.put("Fluid", fluid);
        tankData.putInt("FleetMarker", 42);
        ItemStack tank = LegacyItemData.parse(registries, oldStack(BuiltInRegistries.ITEM.getKey(ModItems.SHIPTANK.get()).toString(), tankData));
        var handler = FluidUtil.getFluidHandler(tank).orElseThrow();
        helper.assertTrue(handler.getFluidInTank(0).getFluid() == Fluids.WATER
                && handler.getFluidInTank(0).getAmount() == 1500
                && handler.getFluidInTank(0).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getInt("Marker") == 99
                && LegacyItemData.copyTag(tank).getInt("FleetMarker") == 42,
                "old tank fluid and unrelated custom NBT survive conversion");
        handler.drain(1500, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(FluidUtil.getFluidHandler(handler.getContainer()).orElseThrow().getFluidInTank(0).isEmpty(),
                "a consumed legacy Fluid tag must not refill a drained component-backed tank");

        CompoundTag swordData = new CompoundTag();
        swordData.putInt("Damage", 7);
        ListTag enchantments = new ListTag();
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", "minecraft:sharpness");
        enchantment.putShort("lvl", (short) 2);
        enchantments.add(enchantment);
        swordData.put("Enchantments", enchantments);
        CompoundTag swordTag = oldStack("minecraft:diamond_sword", swordData);
        swordTag.putInt("Slot", 4);
        ListTag recipe = new ListTag();
        recipe.add(swordTag);
        CompoundTag paperData = new CompoundTag();
        paperData.put(RecipePaperMenu.RECIPE_TAG, recipe);
        ItemStack paper = LegacyItemData.parse(registries,
                oldStack(BuiltInRegistries.ITEM.getKey(ModItems.RECIPEPAPER.get()).toString(), paperData));
        CompoundTag nested = LegacyItemData.copyTag(paper).getList(RecipePaperMenu.RECIPE_TAG, Tag.TAG_COMPOUND).getCompound(0);
        ItemStack sword = LegacyItemData.parse(registries, nested);
        helper.assertTrue(sword.getDamageValue() == 7 && nested.getInt("Slot") == 4
                && EnchantmentHelper.getItemEnchantmentLevel(registries.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS), sword) == 2,
                "nested legacy stacks retain damage, enchantments, and recipe slot metadata");
        ItemStack undamaged = sword.copy();
        undamaged.setDamageValue(0);
        helper.assertTrue(com.lulan.shincolle.crafting.LegacyItemMatchHelper.matchesTargetItem(sword, undamaged, false, false, true)
                && !com.lulan.shincolle.crafting.LegacyItemMatchHelper.matchesTargetItem(sword, new ItemStack(Items.DIAMOND_SWORD), false, false, true),
                "legacy NBT matching includes migrated enchantments but keeps damage independent of the NBT toggle");
        helper.assertTrue(!com.lulan.shincolle.crafting.LegacyItemMatchHelper.matchesTargetItem(
                new ItemStack(Items.BEEF), new ItemStack(Items.COOKED_BEEF), false, true, false),
                "new food classification tags must not turn furnace inputs into legacy matching outputs");
        ItemStack rerolled = new ItemStack(Items.DIAMOND_SWORD);
        var curse = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE);
        rerolled.enchant(curse, 1);
        com.lulan.shincolle.crafting.LegacyEquipmentBuildHelper.applyRandomEnchant(rerolled, 0, 1,
                net.minecraft.util.RandomSource.create(2L), registries);
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(curse, rerolled) == 0
                && !rerolled.getEnchantments().isEmpty(), "a successful legacy enchant roll replaces rather than merges previous enchantments");
        CompoundTag empty = new CompoundTag();
        LegacyItemData.save(ItemStack.EMPTY, registries, empty);
        helper.assertTrue(LegacyItemData.parse(registries, empty).isEmpty(), "empty stacks serialize and reload without an exception");
        helper.assertTrue(LegacyItemData.parse(registries, new CompoundTag()).isEmpty()
                && LegacyItemData.parse(registries, oldStack("shincolle:missing_item", new CompoundTag())).isEmpty(),
                "empty and unknown legacy stacks retain vanilla empty-stack fallback");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void legacyMachineFluidsAndWaypointPositions(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        CompoundTag oldFluid = new CompoundTag();
        oldFluid.putString("FluidName", "minecraft:water");
        oldFluid.putInt("Amount", 1000);
        CompoundTag fluidData = new CompoundTag();
        fluidData.putInt("Marker", 81);
        oldFluid.put("Tag", fluidData);
        CompoundTag upgraded = com.lulan.shincolle.item.LegacyFluidData.upgradeTank(registries, oldFluid.copy());
        var tank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(4000);
        tank.readFromNBT(registries, upgraded);
        helper.assertTrue(tank.getFluidAmount() == 1000 && tank.getFluid().getFluid() == Fluids.WATER
                && tank.getFluid().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getInt("Marker") == 81,
                "legacy machine tank NBT retains fluid, amount, and fluid custom data");
        upgraded.putString("FluidName", "minecraft:lava");
        tank.readFromNBT(registries, com.lulan.shincolle.item.LegacyFluidData.upgradeTank(registries, upgraded));
        helper.assertTrue(tank.getFluid().getFluid() == Fluids.WATER, "modern machine fluids take precedence over stale old fields");
        helper.assertTrue(com.lulan.shincolle.item.LegacyFluidData.read(new CompoundTag()).isEmpty(), "absent old fluid stays empty");
        oldFluid.putString("FluidName", "shincolle:missing_fluid");
        helper.assertTrue(com.lulan.shincolle.item.LegacyFluidData.read(oldFluid).isEmpty(), "unknown old fluid cannot create a replacement fluid");

        var position = new net.minecraft.core.BlockPos(3, 2, 4);
        CompoundTag oldPosition = new CompoundTag();
        oldPosition.putInt("X", position.getX());
        oldPosition.putInt("Y", position.getY());
        oldPosition.putInt("Z", position.getZ());
        CompoundTag saved = new CompoundTag();
        saved.put("NextWaypoint", oldPosition);
        saved.put("PairedChest", oldPosition.copy());
        var waypoint = new com.lulan.shincolle.blockentity.WaypointBlockEntity(net.minecraft.core.BlockPos.ZERO,
                com.lulan.shincolle.registry.ModBlocks.BLOCK_WAYPOINT.get().defaultBlockState());
        waypoint.loadWithComponents(saved, registries);
        helper.assertTrue(position.equals(waypoint.getNextWaypoint()) && position.equals(waypoint.getPairedChest()),
                "1.20.1 compound positions retain route and container links");
        var crane = new com.lulan.shincolle.blockentity.CraneBlockEntity(net.minecraft.core.BlockPos.ZERO,
                com.lulan.shincolle.registry.ModBlocks.BLOCK_CRANE.get().defaultBlockState());
        crane.loadWithComponents(saved, registries);
        helper.assertTrue(position.equals(crane.getPairedChest()), "crane accepts old compound positions too");
        waypoint.loadWithComponents(new CompoundTag(), registries);
        helper.assertTrue(waypoint.getNextWaypoint() == null && waypoint.getPairedChest() == null, "missing positions stay unset");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void hurtHookRunsAfterShieldBlocking(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(0.0D, 2.0D, 0.0D);
        player.setYRot(0.0F);
        attacker.setPos(0.0D, 2.0D, 2.0D);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
        for (int tick = 0; tick < 6; tick++) {
            player.tick();
        }
        helper.assertTrue(player.isBlocking(), "shield timing probe requires an actively blocking player");
        DamageProbe probe = new DamageProbe(player);
        NeoForge.EVENT_BUS.register(probe);
        try {
            float health = player.getHealth();
            player.hurt(player.damageSources().playerAttack(attacker), 10.0F);
            helper.assertTrue(probe.count == 1 && probe.amount == 0.0F && player.getHealth() == health,
                    "the legacy stage must observe post-shield damage, not the incoming ten damage");
        } finally {
            NeoForge.EVENT_BUS.unregister(probe);
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void forgePlayerCapabilityImportsOnceAndModernAttachmentWins(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TeitokuData original = new TeitokuData().withRegistries(player.registryAccess());
        original.setPlayerUid(4321);
        original.setMarriageNum(7);
        CompoundTag wrapped = new CompoundTag();
        wrapped.put("TeitokuExtProps", original.saveToTag(new CompoundTag()));
        CompoundTag caps = new CompoundTag();
        caps.put("shincolle:teitoku", wrapped);
        CompoundTag saved = player.saveWithoutId(new CompoundTag());
        saved.remove("neoforge:attachments");
        saved.put("ForgeCaps", caps);
        player.load(saved);
        TeitokuData imported = TeitokuHelper.get(player).orElseThrow();
        helper.assertTrue(imported.getPlayerUid() == 4321 && imported.getMarriageNum() == 7,
                "Entity.load imports the old outer ForgeCaps and inner capability wrapper");
        imported.setPlayerUid(8765);
        CompoundTag modern = player.saveWithoutId(new CompoundTag());
        modern.put("ForgeCaps", caps);
        player.load(modern);
        helper.assertTrue(TeitokuHelper.get(player).orElseThrow().getPlayerUid() == 8765,
                "modern attachments take precedence over a stale ForgeCaps copy");
        Player empty = helper.makeMockPlayer(GameType.SURVIVAL);
        empty.load(empty.saveWithoutId(new CompoundTag()));
        helper.assertTrue(TeitokuHelper.get(empty).orElseThrow().getPlayerUid() == new TeitokuData().getPlayerUid(),
                "players without old capability data retain empty defaults");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void hurtHookMutatesBeforeArmorForBothVanillaImplementations(GameTestHelper helper) {
        LivingEntity[] entities = {helper.makeMockPlayer(GameType.SURVIVAL), EntityType.ZOMBIE.create(helper.getLevel())};
        for (LivingEntity entity : entities) {
            helper.assertTrue(entity != null, "damage probe entity must be creatable");
            entity.getAttribute(Attributes.ARMOR).setBaseValue(20.0D);
            entity.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0.0D);
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
            entity.setHealth(100.0F);
            DamageProbe probe = new DamageProbe(entity);
            NeoForge.EVENT_BUS.register(probe);
            try {
                var source = entity.damageSources().playerAttack(helper.makeMockPlayer(GameType.SURVIVAL));
                helper.assertTrue(!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR),
                        "the pre-armor probe requires an armor-sensitive damage source");
                float expected = CombatRules.getDamageAfterAbsorb(entity, 20.0F, source, 20.0F, 0.0F);
                entity.hurt(source, 40.0F);
                helper.assertTrue(probe.count == 1 && probe.amount == 40.0F
                        && Math.abs(entity.getHealth() - (100.0F - expected)) < 0.0001F,
                        "real hurt dispatch must apply the legacy mutation before armor, once per entity: "
                                + entity.getType() + " events=" + probe.count + " amount=" + probe.amount
                                + " health=" + entity.getHealth() + " expected=" + (100.0F - expected));
                entity.invulnerableTime = 0;
                float health = entity.getHealth();
                probe.cancel = true;
                entity.hurt(source, 40.0F);
                helper.assertTrue(entity.getHealth() == health && probe.count == 2,
                        "canceling the pre-armor stage prevents health loss");
                entity.invulnerableTime = 0;
                probe.cancel = false;
                probe.scale = 0.0F;
                entity.hurt(source, 40.0F);
                helper.assertTrue(entity.getHealth() == health && probe.count == 3,
                        "zero pre-armor damage prevents health loss");
            } finally {
                NeoForge.EVENT_BUS.unregister(probe);
            }
        }
        helper.succeed();
    }

    private static CompoundTag oldStack(String id, CompoundTag data) {
        CompoundTag stack = new CompoundTag();
        stack.putString("id", id);
        stack.putByte("Count", (byte) 1);
        stack.put("tag", data);
        return stack;
    }

    public static final class DamageProbe {
        private final LivingEntity target;
        private int count;
        private float amount;
        private float scale = 0.5F;
        private boolean cancel;

        private DamageProbe(LivingEntity target) {
            this.target = target;
        }

        @SubscribeEvent
        public void onHurt(LegacyLivingHurtEvent event) {
            if (event.getEntity() == this.target) {
                this.count++;
                this.amount = event.getAmount();
                event.setCanceled(this.cancel);
                event.setAmount(event.getAmount() * this.scale);
            }
        }
    }
}
