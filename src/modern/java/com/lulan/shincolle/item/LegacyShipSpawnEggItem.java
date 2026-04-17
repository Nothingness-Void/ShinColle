package com.lulan.shincolle.item;

import com.lulan.shincolle.advancement.ModCriteriaTriggers;
import com.lulan.shincolle.crafting.LegacyShipConstructionHelper;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.sound.ShipSoundType;
import com.lulan.shincolle.sound.ShinColleSoundHelper;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class LegacyShipSpawnEggItem extends Item {

    private static final String VARIANT_EGG_META_TAG = "VariantEggMeta";
    private static final String LEGACY_CLASS_ID_TAG = "ShipClassId";
    private static final String RECOVERED_SHIP_TAG = "RecoveredShip";

    public LegacyShipSpawnEggItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createRecoveredShipStack(LegacyShipEntity ship) {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath("shincolle", "shipegg" + ship.getVariantEggMeta());
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            item = BuiltInRegistries.ITEM.get(itemId);
        }
        if (!(item instanceof LegacyShipSpawnEggItem)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);
        CompoundTag stackTag = stack.getOrCreateTag();
        stackTag.putInt(VARIANT_EGG_META_TAG, ship.getVariantEggMeta());

        CompoundTag shipTag = new CompoundTag();
        ship.addAdditionalSaveData(shipTag);
        shipTag.remove("Health");
        shipTag.remove("HurtTime");
        shipTag.remove("DeathTime");
        shipTag.remove("NoAI");
        stackTag.put(RECOVERED_SHIP_TAG, shipTag);
        return stack;
    }

    public static boolean isRecoveredShipStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(RECOVERED_SHIP_TAG, Tag.TAG_COMPOUND);
    }

    public static @Nullable CompoundTag getRecoveredShipData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(RECOVERED_SHIP_TAG, Tag.TAG_COMPOUND)
                ? tag.getCompound(RECOVERED_SHIP_TAG).copy()
                : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos spawnPos = getSpawnPos(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        return this.spawnShip(context.getLevel(), player, context.getHand(), context.getItemInHand(), spawnPos);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos clickedPos = hitResult.getBlockPos();

        if (level.getFluidState(clickedPos).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }

        InteractionResult result = this.spawnShip(level, player, hand, stack, clickedPos);
        return new InteractionResultHolder<>(result, stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String itemPath = BuiltInRegistries.ITEM.getKey(this).getPath();
        tooltip.add(Component.translatable("gui.shincolle.spawn_egg.use").withStyle(ChatFormatting.GRAY));

        if (isRecoveredShipStack(stack)) {
            tooltip.add(Component.translatable("gui.shincolle.spawn_egg.recovered").withStyle(ChatFormatting.GOLD));
        }

        if (LegacyShipConstructionHelper.hasConstructionRecipe(stack)) {
            int[] materials = LegacyShipConstructionHelper.readMaterialAmounts(stack);
            tooltip.add(Component.literal(materials[0] + " ").append(Component.translatable("item.shincolle.grudge")).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal(materials[1] + " ").append(Component.translatable("item.shincolle.abyssmetal")).withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(materials[2] + " ").append(Component.translatable("item.shincolle.ammo")).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal(materials[3] + " ").append(Component.translatable("item.shincolle.abyssmetal1")).withStyle(ChatFormatting.AQUA));
            return;
        }

        if ("smallegg".equals(itemPath)) {
            tooltip.add(Component.translatable("gui.shincolle.spawn_egg.pool_primary").withStyle(ChatFormatting.DARK_AQUA));
            return;
        }

        if ("largeegg".equals(itemPath)) {
            tooltip.add(Component.translatable("gui.shincolle.spawn_egg.pool_advanced").withStyle(ChatFormatting.DARK_AQUA));
            return;
        }

        ShipEntitySpec spec = this.resolveSpec(stack, itemPath, level);
        tooltip.add(Component.translatable(spec.hostile()
                ? "gui.shincolle.spawn_egg.hostile"
                : "gui.shincolle.spawn_egg.friendly").withStyle(spec.hostile() ? ChatFormatting.RED : ChatFormatting.AQUA));
    }

    private InteractionResult spawnShip(Level level, Player player, InteractionHand hand, ItemStack stack, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ShipEntitySpec spec = this.resolveSpec(stack, BuiltInRegistries.ITEM.getKey(this).getPath(), level);
        LegacyShipEntity ship = ModEntityTypes.LEGACY_SHIP.get().create(serverLevel);

        if (ship == null) {
            return InteractionResult.FAIL;
        }

        CompoundTag recoveredShipData = getRecoveredShipData(stack);

        ship.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, Mth.wrapDegrees(player.getYRot()), 0.0F);
        ship.setVariantEggMeta(spec.eggMeta());
        ship.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null, null);
        if (recoveredShipData != null) {
            ship.readAdditionalSaveData(recoveredShipData);
            ship.restoreRecoveredDeployment(player);
        } else {
            ship.applySpawnSpec(spec, player);
        }
        ship.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, Mth.wrapDegrees(player.getYRot()), 0.0F);
        ship.setYHeadRot(player.getYRot());
        ship.yBodyRot = player.getYRot();

        if (!serverLevel.noCollision(ship, ship.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        serverLevel.addFreshEntity(ship);

        if (!spec.hostile() && player instanceof ServerPlayer serverPlayer) {
            TeitokuHelper.addCollectedShip(serverPlayer, spec.legacyClassId());
            ModCriteriaTriggers.FRIENDLY_SHIP_DEPLOYED.trigger(serverPlayer);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        ShinColleSoundHelper.playShipVoice(level, player, spec.hostile() ? ShipSoundType.KNOCKBACK : ShipSoundType.PICKITEM,
                0.7F, ShinColleSoundHelper.variedPitch(player, 1.0F, 0.08F));
        player.displayClientMessage(Component.translatable("chat.shincolle.spawn_egg.deployed",
                ship.getName().copy().withStyle(spec.hostile() ? ChatFormatting.RED : ChatFormatting.AQUA)), true);
        player.swing(hand, true);
        return InteractionResult.CONSUME;
    }

    private ShipEntitySpec resolveSpec(ItemStack stack, String itemPath, @Nullable Level level) {
        CompoundTag tag = stack.getTag();
        RandomSource random = level != null ? level.getRandom() : RandomSource.create();

        if (tag != null) {
            if (tag.contains(RECOVERED_SHIP_TAG, Tag.TAG_COMPOUND)) {
                CompoundTag shipTag = tag.getCompound(RECOVERED_SHIP_TAG);
                ShipEntitySpec spec = ShipEntitySpecs.findByEggMeta(shipTag.getInt(VARIANT_EGG_META_TAG));
                if (spec != null) {
                    return spec.hostile() ? ShipEntitySpecs.friendlyCounterpart(spec) : spec;
                }
            }

            if (tag.contains(VARIANT_EGG_META_TAG)) {
                ShipEntitySpec spec = ShipEntitySpecs.findByEggMeta(tag.getInt(VARIANT_EGG_META_TAG));
                if (spec != null) {
                    return spec;
                }
            }

            if (tag.contains(LEGACY_CLASS_ID_TAG)) {
                ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(tag.getInt(LEGACY_CLASS_ID_TAG));
                if (spec != null) {
                    return spec.hostile() ? ShipEntitySpecs.friendlyCounterpart(spec) : spec;
                }
            }

            ShipEntitySpec spec = LegacyShipConstructionHelper.resolveConstructionEgg(stack, itemPath, random);
            if (spec != null) {
                return spec;
            }
        }

        return ShipEntitySpecs.resolveEggItem(itemPath, random);
    }

    private static BlockPos getSpawnPos(Level level, BlockPos clickedPos, Direction face) {
        BlockState clickedState = level.getBlockState(clickedPos);
        return clickedState.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(face);
    }
}
