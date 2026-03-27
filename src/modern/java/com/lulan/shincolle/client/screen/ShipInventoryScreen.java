package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ShipInventoryScreen extends AbstractContainerScreen<ShipInventoryMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_ship_inventory.png");
    private static final int PREVIEW_PANEL_X = 74;
    private static final int PREVIEW_PANEL_Y = 16;
    private static final int PREVIEW_PANEL_W = 61;
    private static final int PREVIEW_PANEL_H = 95;
    private static final int PREVIEW_CENTER_X = 104;
    private static final int PREVIEW_CENTER_Y = 103;
    private static final int MODE_BUTTON_X = 173;
    private static final int MODE_BUTTON_Y = 45;
    private static final int MODE_BUTTON_W = 69;
    private static final int MODE_BUTTON_H = 16;
    private static final int HP_BAR_X = 173;
    private static final int HP_BAR_Y = 91;
    private static final int HP_BAR_W = 69;
    private static final int HP_BAR_H = 8;
    private static final int TOP_PANEL_LEFT = 173;
    private static final int TOP_PANEL_RIGHT = 242;
    private static final int BOTTOM_PANEL_LEFT = 171;
    private static final int BOTTOM_PANEL_RIGHT = 246;

    public ShipInventoryScreen(ShipInventoryMenu menu, Inventory inventory, Component title) {
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
        this.renderExtraTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.renderPreview(guiGraphics, mouseX, mouseY);

        int modeColor = this.menu.getModeLabel().getString().equals(Component.translatable("gui.shincolle.entity.mode.standby").getString())
                ? 0xAA604040
                : 0xAA406040;
        guiGraphics.fill(this.leftPos + MODE_BUTTON_X, this.topPos + MODE_BUTTON_Y,
                this.leftPos + MODE_BUTTON_X + MODE_BUTTON_W, this.topPos + MODE_BUTTON_Y + MODE_BUTTON_H, modeColor);

        guiGraphics.fill(this.leftPos + HP_BAR_X, this.topPos + HP_BAR_Y,
                this.leftPos + HP_BAR_X + HP_BAR_W, this.topPos + HP_BAR_Y + HP_BAR_H, 0xAA1A1A1A);

        int filled = Math.round(this.menu.getHealthRatio() * (HP_BAR_W - 2));
        int barColor = this.menu.getHealthRatio() >= 0.5F ? 0xFF74C05E : this.menu.getHealthRatio() >= 0.25F ? 0xFFE6B450 : 0xFFD25555;
        guiGraphics.fill(this.leftPos + HP_BAR_X + 1, this.topPos + HP_BAR_Y + 1,
                this.leftPos + HP_BAR_X + 1 + filled, this.topPos + HP_BAR_Y + HP_BAR_H - 1, barColor);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.trimToWidth(this.title.getString(), 116), 8, 6, 0x303030, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.cargo"), 8, 120, 0x404040, false);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.shincolle.ship_inventory.equipment"), 164, 6, 0x404040);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.owner"), TOP_PANEL_LEFT, 18, 0xE1D7B8, false);
        this.drawRightAligned(guiGraphics, this.trimToWidth(this.menu.getOwnerLabel().getString(), 69), TOP_PANEL_RIGHT, 28, 0xF7F2EB);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.mode"), TOP_PANEL_LEFT, 37, 0xE1D7B8, false);
        guiGraphics.drawCenteredString(this.font, this.menu.getModeLabel(), MODE_BUTTON_X + (MODE_BUTTON_W / 2), MODE_BUTTON_Y + 4, 0xF7F2EB);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.role"), TOP_PANEL_LEFT, 65, 0xE1D7B8, false);
        this.drawRightAligned(guiGraphics, this.trimToWidth(this.menu.getRoleLabel().getString(), 69), TOP_PANEL_RIGHT, 75, 0xF0DCB2);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.health"), TOP_PANEL_LEFT, 86, 0xE1D7B8, false);
        this.drawRightAligned(guiGraphics, this.menu.getHealthText(), TOP_PANEL_RIGHT, 86, 0xF7F2EB);

        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.level"), String.valueOf(this.menu.getShipLevel()), 132);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.attack"), this.menu.getAttackText(), 141);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.speed"), this.menu.getSpeedText(), 150);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.range"), this.menu.getRangeText(), 159);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.morale"), this.menu.getMoraleText(), 168);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.state"), this.menu.getMoraleLabel().getString(), 177);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.marriage"), this.menu.getMarriageLabel().getString(), 186);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.modern"), String.valueOf(this.menu.getModernizationCount()), 195);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.rescue"), String.valueOf(this.menu.getRescueCount()), 204);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleRegionClick(mouseX, mouseY, MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_W, MODE_BUTTON_H, ShipInventoryMenu.BUTTON_TOGGLE_MODE)) {
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

    private void renderExtraTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;

        if (inside(localX, localY, MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_W, MODE_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.mode"),
                    this.menu.getModeLabel()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, HP_BAR_X, HP_BAR_Y, HP_BAR_W, HP_BAR_H)) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getHealthText()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, PREVIEW_PANEL_X, PREVIEW_PANEL_Y, PREVIEW_PANEL_W, PREVIEW_PANEL_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    this.menu.getRoleLabel(),
                    Component.literal(this.menu.getMoraleLabel().getString() + "  " + this.menu.getMoraleText())), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, 144, 18, 16, 108)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.equip")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, BOTTOM_PANEL_LEFT - 2, 130, 78, 78)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.slots"),
                    Component.literal((this.menu.getEquipmentUsedSlots() + this.menu.getCargoUsedSlots()) + " / " + LegacyShipEntity.SHIP_SLOT_COUNT)),
                    mouseX, mouseY);
        }
    }

    private void renderPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        LegacyShipEntity ship = this.menu.getShip();
        if (ship == null) {
            return;
        }

        guiGraphics.fill(this.leftPos + PREVIEW_PANEL_X + 2, this.topPos + PREVIEW_PANEL_Y + 2,
                this.leftPos + PREVIEW_PANEL_X + PREVIEW_PANEL_W - 2, this.topPos + PREVIEW_PANEL_Y + PREVIEW_PANEL_H - 2, 0xFF8E8E8E);

        int previewX = this.leftPos + PREVIEW_CENTER_X;
        int previewY = this.topPos + PREVIEW_CENTER_Y;
        float followYaw = (float) Mth.clamp((previewX - mouseX) * 0.18D, -16.0D, 16.0D);
        float followPitch = (float) Mth.clamp(((previewY - 36) - mouseY) * 0.08D, -6.0D, 6.0D);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, previewX, previewY, this.getPreviewScale(ship),
                followYaw, followPitch, ship);
    }

    private int getPreviewScale(LegacyShipEntity ship) {
        return switch (ship.getSpec().archetype()) {
            case DESTROYER, SUBMARINE -> 30;
            case CRUISER, TRANSPORT -> 28;
            case BATTLESHIP, CARRIER -> 26;
            case PRINCESS -> 23;
            case INSTALLATION -> 20;
        };
    }

    private void drawStatRow(GuiGraphics guiGraphics, Component label, String value, int y) {
        guiGraphics.drawString(this.font, label, BOTTOM_PANEL_LEFT, y, 0x5A5146, false);
        this.drawRightAligned(guiGraphics, this.trimToWidth(value, 52), BOTTOM_PANEL_RIGHT, y, 0xF2F2F2);
    }

    private void drawRightAligned(GuiGraphics guiGraphics, String text, int rightX, int y, int color) {
        guiGraphics.drawString(this.font, text, rightX - this.font.width(text), y, color, false);
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int targetWidth = Math.max(0, maxWidth - this.font.width(ellipsis));
        String trimmed = this.font.plainSubstrByWidth(text, targetWidth);
        return trimmed + ellipsis;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
