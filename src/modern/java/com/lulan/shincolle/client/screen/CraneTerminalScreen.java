package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class CraneTerminalScreen extends AbstractContainerScreen<CraneTerminalMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_crane.png");
    private static final int MODE_X = 22;
    private static final int MODE_Y = 5;
    private static final int MODE_W = 69;
    private static final int MODE_H = 15;
    private static final int LOAD_X = 7;
    private static final int LOAD_Y = 52;
    private static final int TOGGLE_W = 11;
    private static final int TOGGLE_H = 11;
    private static final int UNLOAD_Y = 83;
    private static final int REDSTONE_X = 65;
    private static final int REDSTONE_Y = 22;
    private static final int LIQUID_X = 23;
    private static final int LIQUID_Y = 36;
    private static final int ENERGY_X = 39;
    private static final int ENERGY_Y = 36;

    public CraneTerminalScreen(CraneTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 201;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(this.font, this.menu.getWaitModeLabel(), 57, 9, 0xFFF0A8);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.crane.filter.load"), 21, 54, 0xFF827D);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.crane.filter.unload"), 21, 85, 0x404040);

        guiGraphics.drawString(this.font, this.menu.getOwnerLabel(), 80, 24, 0xF2F2F2, false);
        guiGraphics.drawString(this.font, shortPos("F", this.menu.getLastWaypoint(), ChatFormatting.LIGHT_PURPLE), 80, 36, 0xD0C8D8, false);
        guiGraphics.drawString(this.font, shortPos("T", this.menu.getNextWaypoint(), ChatFormatting.AQUA), 80, 48, 0xC8DFE1, false);
        guiGraphics.drawString(this.font, shortPos("C", this.menu.getPairedChest(), ChatFormatting.GOLD), 80, 60, 0xE1D1B4, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderHoverTooltip(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (!this.menu.isLoadEnabled()) {
            guiGraphics.fill(this.leftPos + 8, this.topPos + 65, this.leftPos + 168, this.topPos + 81, 0x66300000);
        }

        if (!this.menu.isUnloadEnabled()) {
            guiGraphics.fill(this.leftPos + 8, this.topPos + 96, this.leftPos + 168, this.topPos + 112, 0x66000030);
        }

        drawToggleLamp(guiGraphics, REDSTONE_X, REDSTONE_Y, colorForMode(this.menu.getRedstoneModeLabel()));
        drawToggleLamp(guiGraphics, LIQUID_X, LIQUID_Y, colorForMode(this.menu.getLiquidModeLabel()));
        drawToggleLamp(guiGraphics, ENERGY_X, ENERGY_Y, colorForMode(this.menu.getEnergyModeLabel()));

        for (int slot = 0; slot < 18; slot++) {
            if (!this.menu.isFilterInverted(slot)) {
                continue;
            }

            int x = slot < 9 ? 8 + slot * 18 : 8 + (slot - 9) * 18;
            int y = slot < 9 ? 65 : 96;
            guiGraphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 16, this.topPos + y + 16, 0x44AA2200);
            guiGraphics.drawString(this.font, "!", x + 5, y + 4, 0xFFE38C7A, false);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int mouseButton) {
        return super.hasClickedOutside(mouseX, mouseY, leftPos, topPos, mouseButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleRegionClick(mouseX, mouseY, MODE_X, MODE_Y, MODE_W, MODE_H, CraneTerminalMenu.BUTTON_WAIT_MODE)) {
            return true;
        }

        if (this.handleRegionClick(mouseX, mouseY, LOAD_X, LOAD_Y, TOGGLE_W, TOGGLE_H, CraneTerminalMenu.BUTTON_TOGGLE_LOAD)) {
            return true;
        }

        if (this.handleRegionClick(mouseX, mouseY, LOAD_X, UNLOAD_Y, TOGGLE_W, TOGGLE_H, CraneTerminalMenu.BUTTON_TOGGLE_UNLOAD)) {
            return true;
        }

        if (this.handleRegionClick(mouseX, mouseY, REDSTONE_X, REDSTONE_Y, TOGGLE_W, TOGGLE_H, CraneTerminalMenu.BUTTON_REDSTONE_MODE)) {
            return true;
        }

        if (this.handleRegionClick(mouseX, mouseY, LIQUID_X, LIQUID_Y, 13, 13, CraneTerminalMenu.BUTTON_LIQUID_MODE)) {
            return true;
        }

        if (this.handleRegionClick(mouseX, mouseY, ENERGY_X, ENERGY_Y, 13, 13, CraneTerminalMenu.BUTTON_ENERGY_MODE)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleRegionClick(double mouseX, double mouseY, int relX, int relY, int width, int height, int buttonId) {
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < relX || localX > relX + width || localY < relY || localY > relY + height) {
            return false;
        }

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
            return true;
        }

        return false;
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;

        if (inside(localX, localY, MODE_X, MODE_Y, MODE_W, MODE_H)) {
            renderComponentTooltip(guiGraphics, List.of(
                    this.menu.getWaitModeLabel(),
                    Component.translatable("gui.shincolle.crane.note.wait")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, LOAD_X, LOAD_Y, TOGGLE_W, TOGGLE_H)) {
            renderComponentTooltip(guiGraphics, List.of(
                    Component.translatable("gui.shincolle.crane.button.load", this.menu.getLoadStateLabel()),
                    Component.translatable("gui.shincolle.crane.note.filter")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, LOAD_X, UNLOAD_Y, TOGGLE_W, TOGGLE_H)) {
            renderComponentTooltip(guiGraphics, List.of(
                    Component.translatable("gui.shincolle.crane.button.unload", this.menu.getUnloadStateLabel()),
                    Component.translatable("gui.shincolle.crane.note.filter")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, REDSTONE_X, REDSTONE_Y, TOGGLE_W, TOGGLE_H)) {
            guiGraphics.renderTooltip(this.font, this.menu.getRedstoneModeLabel(), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, LIQUID_X, LIQUID_Y, 13, 13)) {
            guiGraphics.renderTooltip(this.font, this.menu.getLiquidModeLabel(), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, ENERGY_X, ENERGY_Y, 13, 13)) {
            guiGraphics.renderTooltip(this.font, this.menu.getEnergyModeLabel(), mouseX, mouseY);
            return;
        }

        for (int slot = 0; slot < 18; slot++) {
            int x = slot < 9 ? 8 + slot * 18 : 8 + (slot - 9) * 18;
            int y = slot < 9 ? 65 : 96;
            if (!inside(localX, localY, x, y, 16, 16)) {
                continue;
            }

            ItemStack stack = this.menu.getFilterStack(slot);
            if (stack.isEmpty()) {
                renderComponentTooltip(guiGraphics, List.of(
                        Component.translatable("gui.shincolle.crane.note.filter"),
                        Component.translatable("gui.shincolle.crane.note.filter_inverse")), mouseX, mouseY);
            } else if (this.menu.isFilterInverted(slot)) {
                renderComponentTooltip(guiGraphics, List.of(
                        stack.getHoverName(),
                        Component.translatable("gui.shincolle.crane.note.filter_inverse")), mouseX, mouseY);
            }

            return;
        }
    }

    private void renderComponentTooltip(GuiGraphics guiGraphics, List<Component> lines, int mouseX, int mouseY) {
        List<FormattedCharSequence> formatted = lines.stream()
                .map(Component::getVisualOrderText)
                .toList();
        guiGraphics.renderTooltip(this.font, formatted, mouseX, mouseY);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawToggleLamp(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 11, this.topPos + y + 11, 0xAA111111);
        guiGraphics.fill(this.leftPos + x + 2, this.topPos + y + 2, this.leftPos + x + 9, this.topPos + y + 9, color);
    }

    private static int colorForMode(Component label) {
        String text = label.getString().toLowerCase();
        if (text.contains("disabled") || text.contains("no ")) {
            return 0xFF555555;
        }

        if (text.contains("pulse") || text.contains("unload")) {
            return 0xFFE58B3C;
        }

        return 0xFF7FDB77;
    }

    private static Component shortPos(String prefix, @Nullable net.minecraft.core.BlockPos pos, ChatFormatting color) {
        if (pos == null) {
            return Component.literal(prefix + ": -").withStyle(ChatFormatting.DARK_GRAY);
        }

        return Component.literal(prefix + ": " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(color);
    }
}
