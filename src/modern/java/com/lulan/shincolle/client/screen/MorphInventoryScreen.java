package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.MorphInventoryMenu;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    private static final int AURA_LABEL_X = 171;
    private static final int AURA_LABEL_Y = 52;
    private static final int AURA_BUTTON_X = 218;
    private static final int AURA_BUTTON_Y = 50;
    private static final int AURA_BUTTON_W = 24;
    private static final int AURA_BUTTON_H = 12;
    private static final int HELD_LABEL_X = 171;
    private static final int HELD_LABEL_Y = 66;
    private static final int HELD_BUTTON_X = 218;
    private static final int HELD_BUTTON_Y = 64;
    private static final int HELD_BUTTON_W = 24;
    private static final int HELD_BUTTON_H = 12;
    private static final int RESOURCE_TITLE_X = 171;
    private static final int RESOURCE_TITLE_Y = 82;
    private static final int RESOURCE_ROW_X = 171;
    private static final int RESOURCE_LIGHT_Y = 94;
    private static final int RESOURCE_HEAVY_Y = 106;
    private static final int RESOURCE_GRUDGE_Y = 118;
    private static final int RESOURCE_BUTTON_X = 231;
    private static final int RESOURCE_BUTTON_W = 11;
    private static final int RESOURCE_BUTTON_H = 10;
    private static final int BOTTOM_LEFT_X = 8;
    private static final int BOTTOM_RIGHT_X = 136;
    private static final int BOTTOM_ROW_Y = 132;

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
        guiGraphics.fill(left + AURA_BUTTON_X, top + AURA_BUTTON_Y, left + AURA_BUTTON_X + AURA_BUTTON_W, top + AURA_BUTTON_Y + AURA_BUTTON_H,
                this.menu.isAuraEffectEnabled() ? 0xAA3E6B3E : 0xAA6B3E3E);
        guiGraphics.fill(left + HELD_BUTTON_X, top + HELD_BUTTON_Y, left + HELD_BUTTON_X + HELD_BUTTON_W, top + HELD_BUTTON_Y + HELD_BUTTON_H,
                this.menu.isShowHeldItemEnabled() ? 0xAA3E6B3E : 0xAA6B3E3E);
        this.renderResourceButton(guiGraphics, left, top, RESOURCE_LIGHT_Y, 0xAA3B536E);
        this.renderResourceButton(guiGraphics, left, top, RESOURCE_HEAVY_Y, 0xAA5B3F6E);
        this.renderResourceButton(guiGraphics, left, top, RESOURCE_GRUDGE_Y, 0xAA6E3B48);
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
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.auraeffect"), AURA_LABEL_X, AURA_LABEL_Y, 0xD7CBAE, false);
        guiGraphics.drawCenteredString(this.font, this.menu.getAuraStateLabel(),
                AURA_BUTTON_X + (AURA_BUTTON_W / 2), AURA_BUTTON_Y + 2, 0xF7F2EB);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.showhelditem"), HELD_LABEL_X, HELD_LABEL_Y, 0xD7CBAE, false);
        guiGraphics.drawCenteredString(this.font, this.menu.getShowHeldStateLabel(),
                HELD_BUTTON_X + (HELD_BUTTON_W / 2), HELD_BUTTON_Y + 2, 0xF7F2EB);

        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.level"), this.menu.getLevelText(), 55);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.attack"), this.menu.getAttackText(), 65);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.morph_inventory.attack_air"), this.menu.getAirAttackText(), 75);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.speed"), this.menu.getSpeedText(), 85);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.morph_inventory.move"), this.menu.getMoveText(), 95);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.range"), this.menu.getRangeText(), 105);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.resources"), RESOURCE_TITLE_X, RESOURCE_TITLE_Y, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.ammo_light", this.menu.getAmmoLightText()), RESOURCE_ROW_X, RESOURCE_LIGHT_Y, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.ammo_heavy", this.menu.getAmmoHeavyText()), RESOURCE_ROW_X, RESOURCE_HEAVY_Y, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.grudge", this.menu.getGrudgeText()), RESOURCE_ROW_X, RESOURCE_GRUDGE_Y, 0xF1F1F1, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.equipment"), 143, 6, 0xE4DED2, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.morph_inventory.inventory"), 8, 122, 0xE4DED2, false);

        guiGraphics.drawString(this.font, this.menu.getMarriageLabel(), BOTTOM_LEFT_X, BOTTOM_ROW_Y, 0xE8D1E6, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.modern"), BOTTOM_LEFT_X, BOTTOM_ROW_Y + 10, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, Integer.toString(this.menu.getModernizationCount()), 98, BOTTOM_ROW_Y + 10, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.exp"), BOTTOM_LEFT_X, BOTTOM_ROW_Y + 20, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, this.menu.getExperienceText(), 70, BOTTOM_ROW_Y + 20, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, this.menu.getSensorBehaviorLabel(), BOTTOM_LEFT_X, BOTTOM_ROW_Y + 34, 0xD2E7F6, false);
        guiGraphics.drawString(this.font, this.menu.getUtilityBehaviorLabel(), BOTTOM_LEFT_X, BOTTOM_ROW_Y + 46, 0xEBD8BE, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.morale"), BOTTOM_RIGHT_X, BOTTOM_ROW_Y, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, this.menu.getMoraleText(), BOTTOM_RIGHT_X, BOTTOM_ROW_Y + 10, 0xF1F1F1, false);
        guiGraphics.drawString(this.font, this.menu.getRouteBehaviorLabel(), BOTTOM_RIGHT_X, BOTTOM_ROW_Y + 34, 0xD8E7C2, false);
        guiGraphics.drawString(this.font, this.menu.getTorpedoBehaviorLabel(), BOTTOM_RIGHT_X, BOTTOM_ROW_Y + 46, 0xF2C6C6, false);
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
        if (this.clickCommand(mouseX, mouseY, AURA_BUTTON_X, AURA_BUTTON_Y, AURA_BUTTON_W, AURA_BUTTON_H, GameplayCommandType.MORPH_TOGGLE_AURA_EFFECT)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, HELD_BUTTON_X, HELD_BUTTON_Y, HELD_BUTTON_W, HELD_BUTTON_H, GameplayCommandType.MORPH_TOGGLE_SHOW_HELD)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, RESOURCE_BUTTON_X, RESOURCE_LIGHT_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H, GameplayCommandType.MORPH_ADD_LIGHT_AMMO)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, RESOURCE_BUTTON_X, RESOURCE_HEAVY_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H, GameplayCommandType.MORPH_ADD_HEAVY_AMMO)) {
            return true;
        }
        if (this.clickCommand(mouseX, mouseY, RESOURCE_BUTTON_X, RESOURCE_GRUDGE_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H, GameplayCommandType.MORPH_ADD_GRUDGE)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawStatRow(GuiGraphics guiGraphics, Component label, String value, int y) {
        guiGraphics.drawString(this.font, label, 10, y, 0xD7CBAE, false);
        guiGraphics.drawString(this.font, value, 96, y, 0xF1F1F1, false);
    }

    private void renderResourceButton(GuiGraphics guiGraphics, int left, int top, int rowY, int color) {
        guiGraphics.fill(left + RESOURCE_BUTTON_X, top + rowY - 1,
                left + RESOURCE_BUTTON_X + RESOURCE_BUTTON_W, top + rowY - 1 + RESOURCE_BUTTON_H, color);
        guiGraphics.drawCenteredString(this.font, Component.literal("+"),
                left + RESOURCE_BUTTON_X + (RESOURCE_BUTTON_W / 2), top + rowY, 0xF7F2EB);
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

        if (inside(localX, localY, AURA_BUTTON_X, AURA_BUTTON_Y, AURA_BUTTON_W, AURA_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.auraeffect"),
                    this.menu.getAuraStateLabel()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, HELD_BUTTON_X, HELD_BUTTON_Y, HELD_BUTTON_W, HELD_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.showhelditem"),
                    this.menu.getShowHeldStateLabel()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, RESOURCE_BUTTON_X, RESOURCE_LIGHT_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("item.shincolle.ammo"),
                    Component.translatable("item.shincolle.ammo1")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, RESOURCE_BUTTON_X, RESOURCE_HEAVY_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("item.shincolle.ammo2"),
                    Component.translatable("item.shincolle.ammo3")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, RESOURCE_BUTTON_X, RESOURCE_GRUDGE_Y - 1, RESOURCE_BUTTON_W, RESOURCE_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("item.shincolle.grudge"),
                    Component.translatable("block.shincolle.blockgrudge")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, 8, 166, 232, 30)) {
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
