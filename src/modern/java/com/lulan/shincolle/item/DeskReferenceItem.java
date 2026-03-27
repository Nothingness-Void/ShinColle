package com.lulan.shincolle.item;

import com.lulan.shincolle.menu.DeskReferenceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class DeskReferenceItem extends Item {

    private final int variant;
    private final String titleKey;

    public DeskReferenceItem(Properties properties, int variant, String titleKey) {
        super(properties);
        this.variant = variant;
        this.titleKey = titleKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider(
                            (containerId, inventory, ignoredPlayer) -> new DeskReferenceMenu(containerId, inventory, this.variant),
                            Component.translatable(this.titleKey)),
                    buffer -> buffer.writeVarInt(this.variant));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
