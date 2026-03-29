package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class MorphInventoryScreen extends AbstractContainerScreen<MorphInventoryMenu> {

    private static final int TITLE_Y = 8;
    private static final int STATUS_X = 171;
    private static final int STATUS_Y = 18;
    private static final int PREV_X = 8;
    private static final int PREV_Y = 6;
    private static final int PREV_W = 12;
    private static final int PREV_H = 12;
    private static final int NEXT_X = 236;
    private static final int NEXT_Y = 6;
    private static final int NEXT_W = 12;
    private static final int NEXT_H = 12;
    private static final int TOGGLE_X = 170;
    private static final int TOGGLE_Y = 32;
    private static final int TOGGLE_W = 74;
    private static final int TOGGLE_H = 14;

    public MorphInventoryScreen(MorphInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 214;
        this.titleLabelY = 1000;
        this.inventoryLabelY = 1000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderHoverTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF181B22);
        guiGraphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3, 0xFF2A303A);
        guiGraphics.fill(left + 8, top + 18, left + 162, top + 124, 0x66434D5A);
        guiGraphics.fill(left + 168, top + 18, left + 248, top + 124, 0x664A2F38);
        guiGraphics.fill(left + 8, top + 128, left + 248, top + 208, 0x4439434F);
        guiGraphics.fill(left + PREV_X, top + PREV_Y, left + PREV_X + PREV_W, top + PREV_Y + PREV_H, 0xAA3A4652);
        guiGraphics.fill(left + NEXT_X, top + NEXT_Y, left + NEXT_X + NEXT_W, top + NEXT_Y + NEXT_H, 0xAA3A4652);
        guiGraphics.fill(left + TOGGLE_X, top + TOGGLE_Y, left + TOGGLE_X + TOGGLE_W, top + TOGGLE_Y + TOGGLE_H,
                this.menu.isActiveMorph() ? 0xAA3E6B3E : 0xAA6B3E3E);
        guiGraphics.fill(left + 143, top + 18, left + 161, top + 126, 0x55202020);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(this.font,
                Component.literal((this.menu.getProfileIndex() + 1) + " / " + Math.max(1, this.menu.getProfileCount())),
                128, TITLE_Y, 0xF2E8D0);
        guiGraphics.drawCenteredString(this.font, this.menu.getTitleLabel(), 128, 20, 0xF6F0E6);
        guiGraphics.drawString(this.font, "<", PREV_X + 3, PREV_Y + 2, 0xE6EDF3, false);
        guiGraphics.drawString(this.font, ">", NEXT_X + 3, NEXT_Y + 2, 0xE6EDF3, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.host"), STATUS_X, STATUS_Y, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, this.menu.getHostModeLabel(), STATUS_X, STATUS_Y + 10, 0xF1F1F1, false);
        guiGraphics.drawCenteredString(this.font, this.menu.getStatusLabel(), TOGGLE_X + (TOGGLE_W / 2), TOGGLE_Y + 3, 0xF7F2EB);

        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.level"), this.menu.getLevelText(), 55);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.attack"), this.menu.getAttackText(), 65);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.morph_inventory.attack_air"), this.menu.getAirAttackText(), 75);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.speed"), this.menu.getSpeedText(), 85);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.morph_inventory.move"), this.menu.getMoveText(), 95);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.range"), this.menu.getRangeText(), 105);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.resources"), 171, 55, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.ammo_light", this.menu.getAmmoLightText()), 171, 67, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.ammo_heavy", this.menu.getAmmoHeavyText()), 171, 79, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.grudge", this.menu.getGrudgeText()), 171, 91, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.morale"), 171, 103, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, this.menu.getMoraleText(), 171, 113, 0xF1F1F1, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.equipment"), 143, 6, 0xE4DED2, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.inventory"), 8, 122, 0xE4DED2, false);

        int infoY = 132;
        guiGraphics.drawString(this.font, this.menu.getMarriageLabel(), 8, infoY, 0xE8D1E6, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.modern"), 8, infoY + 10, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, Integer.toString(this.menu.getModernizationCount()), 98, infoY + 10, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, this.menu.getSensorBehaviorLabel(), 8, infoY + 24, 0xD2E7F6, false);
        guiGraphics.drawString(this.font, this.menu.getUtilityBehaviorLabel(), 8, infoY + 36, 0xEBD8BE, false);
        guiGraphics.drawString(this.font, this.menu.getRouteBehaviorLabel(), 8, infoY + 48, 0xD8E7C2, false);
        guiGraphics.drawString(this.font, this.menu.getTorpedoBehaviorLabel(), 8, infoY + 60, 0xF2C6C6, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clickCommand(mouseX, mouseY, PREV_X, PREV_Y, PREV_W, PREV_H, GameplayCommandType.MORPH_CYCLE_PROFILE_PREV)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, NEXT_X, NEXT_Y, NEXT_W, NEXT_H, GameplayCommandType.MORPH_CYCLE_PROFILE_NEXT)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H, GameplayCommandType.MORPH_TOGGLE_ACTIVE)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawStatRow(GuiGraphics guiGraphics, Component label, String value, int y) {
        guiGraphics.drawString(this.font, label, 10, y, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, value, 96, y, 0xF1F1F1, false);
    }

    private boolean clickCommand(double mouseX, double mouseY, int relX, int relY, int width, int height, GameplayCommandType commandType) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < relX || localX > relX + width || localY < relY || localY > relY + height) {
            return false;
        }

        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(commandType, new CompoundTag()));
        return true;
    }

    private void renderHoverTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;

        if (inside(localX, localY, TOGGLE_X, TOGGLE_Y, TOGGLE_W, TOGGLE_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.morph_inventory.tooltip.toggle"),
                    this.menu.getStatusLabel(),
                    this.menu.getHostModeLabel()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, 8, 156, 220, 42)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    this.menu.getSensorBehaviorLabel(),
                    this.menu.getUtilityBehaviorLabel(),
                    this.menu.getRouteBehaviorLabel(),
                    this.menu.getTorpedoBehaviorLabel()), mouseX, mouseY);
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
