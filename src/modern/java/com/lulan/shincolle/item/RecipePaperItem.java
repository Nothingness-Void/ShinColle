package com.lulan.shincolle.item;

import com.lulan.shincolle.menu.RecipePaperMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class RecipePaperItem extends Item {

    public RecipePaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) -> new RecipePaperMenu(containerId, inventory, hand),
                            stack.getHoverName()),
                    buffer -> buffer.writeBoolean(true));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ListTag recipeEntries = getRecipeEntries(stack);

        if (recipeEntries.isEmpty()) {
            return;
        }

        ItemStack[] recipeStacks = new ItemStack[RecipePaperMenu.SAVED_SLOT_COUNT];

        for (int i = 0; i < recipeStacks.length; i++) {
            recipeStacks[i] = ItemStack.EMPTY;
        }

        for (int i = 0; i < recipeEntries.size(); i++) {
            CompoundTag itemTag = recipeEntries.getCompound(i);
            int slot = itemTag.getInt(RecipePaperMenu.SLOT_KEY);

            if (slot >= 0 && slot < recipeStacks.length) {
                recipeStacks[slot] = ItemStack.of(itemTag);
            }
        }

        ItemStack resultStack = recipeStacks[RecipePaperMenu.RESULT_STORAGE_SLOT];

        if (!resultStack.isEmpty()) {
            tooltip.add(Component.empty()
                    .append(Component.translatable("gui.shincolle.recipepaper.result").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" "))
                    .append(resultStack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
        }

        tooltip.add(Component.translatable("gui.shincolle.recipepaper.material").withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < RecipePaperMenu.CRAFT_SLOT_COUNT; i++) {
            ItemStack ingredient = recipeStacks[i];

            if (!ingredient.isEmpty()) {
                tooltip.add(Component.literal("  ")
                        .append(ingredient.getHoverName().copy().withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private static ListTag getRecipeEntries(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(RecipePaperMenu.RECIPE_TAG, Tag.TAG_LIST)) {
            return new ListTag();
        }

        return tag.getList(RecipePaperMenu.RECIPE_TAG, Tag.TAG_COMPOUND);
    }
}
