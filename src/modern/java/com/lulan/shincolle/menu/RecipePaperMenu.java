package com.lulan.shincolle.menu;

import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModMenus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class RecipePaperMenu extends AbstractContainerMenu {

    public static final String RECIPE_TAG = "Recipe";
    public static final String SLOT_KEY = "Slot";
    public static final int CRAFT_SLOT_COUNT = 9;
    public static final int RESULT_STORAGE_SLOT = 9;
    public static final int SAVED_SLOT_COUNT = 10;

    private static final int RESULT_SLOT_ID = 9;
    private static final int PLAYER_INVENTORY_START = 10;
    private static final int HOTBAR_START = 37;

    private final Player player;
    private final InteractionHand hostHand;
    private final int hostMenuSlot;
    private final TransientCraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftResult = new ResultContainer();

    public RecipePaperMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenus.RECIPE_PAPER.get(), containerId);
        this.player = playerInventory.player;
        this.hostHand = hand;
        this.hostMenuSlot = HOTBAR_START + playerInventory.selected;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlot(new Slot(this.craftMatrix, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }

        this.addSlot(new Slot(this.craftResult, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        this.loadRecipeFromStack(this.getHostStack());
        this.updateResult(this.player.level());
    }

    public static RecipePaperMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return new RecipePaperMenu(containerId, playerInventory, hand);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.craftMatrix) {
            this.updateResult(this.player.level());
        }

        super.slotsChanged(container);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == this.hostMenuSlot) {
            return;
        }

        if (slotId >= 0 && slotId < CRAFT_SLOT_COUNT) {
            Slot slot = this.slots.get(slotId);
            ItemStack carried = this.getCarried();

            if (carried.isEmpty() || button == 1) {
                slot.set(ItemStack.EMPTY);
            } else {
                ItemStack ghostStack = carried.copy();
                ghostStack.setCount(1);
                slot.set(ghostStack);
            }

            slot.setChanged();
            this.slotsChanged(this.craftMatrix);
            this.broadcastChanges();
            return;
        }

        if (slotId == RESULT_SLOT_ID) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            ItemStack hostStack = this.getHostStack();

            if (hostStack.is(ModItems.RECIPEPAPER.get())) {
                this.saveRecipeToStack(hostStack);
            }
        }
    }

    private ItemStack getHostStack() {
        return this.player.getItemInHand(this.hostHand);
    }

    private void loadRecipeFromStack(ItemStack hostStack) {
        if (hostStack.isEmpty()) {
            return;
        }

        CompoundTag tag = hostStack.getTag();

        if (tag == null || !tag.contains(RECIPE_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag recipeEntries = tag.getList(RECIPE_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < recipeEntries.size(); i++) {
            CompoundTag itemTag = recipeEntries.getCompound(i);
            int slot = itemTag.getInt(SLOT_KEY);

            if (slot >= 0 && slot < CRAFT_SLOT_COUNT) {
                this.craftMatrix.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    private void saveRecipeToStack(ItemStack hostStack) {
        CompoundTag tag = hostStack.getOrCreateTag();
        ListTag recipeEntries = new ListTag();

        for (int i = 0; i < CRAFT_SLOT_COUNT; i++) {
            ItemStack ingredient = this.craftMatrix.getItem(i);

            if (!ingredient.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt(SLOT_KEY, i);
                ingredient.save(itemTag);
                recipeEntries.add(itemTag);
            }
        }

        ItemStack resultStack = this.craftResult.getItem(0);

        if (!resultStack.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt(SLOT_KEY, RESULT_STORAGE_SLOT);
            resultStack.save(itemTag);
            recipeEntries.add(itemTag);
        }

        tag.put(RECIPE_TAG, recipeEntries);
    }

    private void updateResult(Level level) {
        ItemStack resultStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftMatrix, level);

        if (recipe.isPresent()) {
            resultStack = recipe.get().assemble(this.craftMatrix, level.registryAccess());
        }

        this.craftResult.setItem(0, resultStack);
    }
}
