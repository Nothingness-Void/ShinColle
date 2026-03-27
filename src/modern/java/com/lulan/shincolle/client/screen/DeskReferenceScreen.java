package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.DeskReferenceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class DeskReferenceScreen extends AbstractContainerScreen<DeskReferenceMenu> {

    public DeskReferenceScreen(DeskReferenceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 152;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 12, 12, 0xF3E8C8, false);

        int lineY = 34;
        for (Component line : this.getBodyLines()) {
            guiGraphics.drawWordWrap(this.font, line, 12, lineY, this.imageWidth - 24, 0xD6CFBF);
            lineY += 22;
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
        int left = this.leftPos;
        int top = this.topPos;
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171B22);
        guiGraphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3, 0xFF24303A);
        guiGraphics.fill(left + 8, top + 8, left + this.imageWidth - 8, top + this.imageHeight - 8, 0xFF10262B);
        guiGraphics.fill(left + 10, top + 28, left + this.imageWidth - 10, top + this.imageHeight - 10, 0x662D3F48);
    }

    private List<Component> getBodyLines() {
        if (this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT) {
            return List.of(
                    Component.translatable("gui.shincolle.desk.book.line1"),
                    Component.translatable("gui.shincolle.desk.book.line2"),
                    Component.translatable("gui.shincolle.desk.book.line3"),
                    Component.translatable("gui.shincolle.desk.book.line4"));
        }

        return List.of(
                Component.translatable("gui.shincolle.desk.radar.line1"),
                Component.translatable("gui.shincolle.desk.radar.line2"),
                Component.translatable("gui.shincolle.desk.radar.line3"),
                Component.translatable("gui.shincolle.desk.radar.line4"));
    }
}
