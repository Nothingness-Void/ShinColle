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
    private static final int MODE_BUTTON_X = 143;
    private static final int MODE_BUTTON_Y = 17;
    private static final int MODE_BUTTON_SIZE = 18;

    public LegacyCoreScreen(LegacyCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 174;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 10, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.menu.getOwnerLabel(), 79, 18, 0xE7E7E7, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.legacy_core.status.mode", this.menu.getModeLabel()), 79, 33, 0xD6D6D6, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.legacy_core.status.charge",
                        Component.literal(this.menu.getCharge() + " / " + this.menu.getMaxCharge())),
                79, 48, 0xD6D6D6, false);

        if (this.menu.isVolCore()) {
            Component aura = this.menu.hasNearbyFluid()
                    ? Component.translatable("gui.shincolle.volcore.aura.heal")
                    : Component.translatable("gui.shincolle.volcore.aura.burn");
            guiGraphics.drawString(this.font, aura, 79, 63, this.menu.hasNearbyFluid() ? 0x6BD2A0 : 0xE28B67, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.shipyard.structure_missing"), 79, 63, 0xC9A66B, false);
        }
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

        int chargeBar = this.menu.getChargeScaled(54);
        if (chargeBar > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 57, this.topPos + 75 - chargeBar, 176, 54 - chargeBar, 10, chargeBar);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleModeButton(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleModeButton(double mouseX, double mouseY) {
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < MODE_BUTTON_X || localX > MODE_BUTTON_X + MODE_BUTTON_SIZE || localY < MODE_BUTTON_Y || localY > MODE_BUTTON_Y + MODE_BUTTON_SIZE) {
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
        if (localX >= 56 && localX <= 67 && localY >= 21 && localY <= 75) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getCharge() + " / " + this.menu.getMaxCharge()), mouseX, mouseY);
            return;
        }

        if (localX >= MODE_BUTTON_X && localX <= MODE_BUTTON_X + MODE_BUTTON_SIZE
                && localY >= MODE_BUTTON_Y && localY <= MODE_BUTTON_Y + MODE_BUTTON_SIZE) {
            guiGraphics.renderTooltip(this.font, this.menu.getModeLabel(), mouseX, mouseY);
        }
    }
}
