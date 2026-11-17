package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.LegacyCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LegacyCoreScreen extends AbstractContainerScreen<LegacyCoreMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_vol_core.png");
    private static final int POWER_BUTTON_X = 7;
    private static final int POWER_BUTTON_Y = 6;
    private static final int POWER_BUTTON_SIZE = 13;

    public LegacyCoreScreen(LegacyCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String titleStr = this.title.getString();
        guiGraphics.drawString(this.font, titleStr, this.imageWidth / 2 - this.font.width(titleStr) / 2, 6, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.getMode() == 1) {
            guiGraphics.blit(TEXTURE, this.leftPos + POWER_BUTTON_X, this.topPos + POWER_BUTTON_Y, 12, 166, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE);
        }

        int chargeBar = this.menu.getChargeScaled(31);
        if (chargeBar > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 38, this.topPos + 59 - chargeBar, 0, 197 - chargeBar, 12, chargeBar);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handlePowerButton(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handlePowerButton(double mouseX, double mouseY) {
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < POWER_BUTTON_X || localX > POWER_BUTTON_X + POWER_BUTTON_SIZE || localY < POWER_BUTTON_Y || localY > POWER_BUTTON_Y + POWER_BUTTON_SIZE) {
            return false;
        }

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, LegacyCoreMenu.BUTTON_CYCLE_MODE);
            return true;
        }

        return false;
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;
        if (localX >= 36 && localX <= 52 && localY >= 27 && localY <= 61) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getCharge() + " / " + this.menu.getMaxCharge()), mouseX, mouseY);
            return;
        }

        if (localX >= POWER_BUTTON_X && localX <= POWER_BUTTON_X + POWER_BUTTON_SIZE
                && localY >= POWER_BUTTON_Y && localY <= POWER_BUTTON_Y + POWER_BUTTON_SIZE) {
            guiGraphics.renderTooltip(this.font, this.menu.getModeLabel(), mouseX, mouseY);
        }
    }
}
