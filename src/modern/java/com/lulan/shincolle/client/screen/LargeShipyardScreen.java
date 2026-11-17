package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.menu.LargeShipyardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LargeShipyardScreen extends AbstractContainerScreen<LargeShipyardMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_large_shipyard.png");
    private static final int SHIP_BUTTON_X = 157;
    private static final int EQUIP_BUTTON_X = 177;
    private static final int MODE_BUTTON_Y = 24;
    private static final int MODE_BUTTON_SIZE = 18;
    private int animationTick;

    public LargeShipyardScreen(LargeShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 208;
        this.imageHeight = 223;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.animationTick++;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String title = this.title.getString();
        guiGraphics.drawString(this.font, title, this.imageWidth / 2 - this.font.width(title) / 2, 6, 0x404040, false);

        String buildTime = this.menu.getBuildTimeString();
        guiGraphics.drawString(this.font, buildTime, 176 - this.font.width(buildTime) / 2, 77, 0x404040, false);

        for (int i = 0; i < 4; i++) {
            int buildAmt = this.menu.getBuildMaterialAmount(i);
            int stockAmt = this.menu.getMaterialAmount(i);
            this.drawCentered(guiGraphics, Integer.toString(buildAmt), 73, 20 + i * 19, materialColor(buildAmt));
            this.drawCentered(guiGraphics, Integer.toString(stockAmt), 125, 20 + i * 19, 0xE0C54E);
        }

        guiGraphics.drawString(this.font,
                Component.translatable(this.menu.isStructureComplete()
                        ? "gui.shincolle.shipyard.structure_ready"
                        : "gui.shincolle.shipyard.structure_missing"),
                103, 17, this.menu.isStructureComplete() ? 0x57C26B : 0xD15C5C, false);
        guiGraphics.drawString(this.font,
                Component.translatable(this.menu.isCoreLinked()
                        ? "gui.shincolle.shipyard.core_linked"
                        : "gui.shincolle.shipyard.core_missing"),
                103, 28, this.menu.isCoreLinked() ? 0x7CC6D8 : 0xB5B5B5, false);

        Component failure = this.menu.getFailureMessage();
        if (!failure.getString().isBlank()) {
            drawCentered(guiGraphics, failure, 105, 99, 0xFF2A73);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderHoverTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int fuelBar = this.menu.getPowerRemainingScaled(64);
        if (fuelBar > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 9, this.topPos + 83 - fuelBar, 208, 64 - fuelBar, 12, fuelBar);
        }

        renderModeOverlay(guiGraphics, this.menu.getBuildType(), this.animationTick / 6 % 6);

        int selectMat = this.menu.getSelectMat();
        if (selectMat >= 0 && selectMat <= 3) {
            // Material adjust buttons panel at (50, 8 + selectMat * 19)
            guiGraphics.blit(TEXTURE, this.leftPos + 50, this.topPos + 8 + selectMat * 19, 0, 223, 48, 30);
            // Material selection frame at (27, 14 + selectMat * 19)
            guiGraphics.blit(TEXTURE, this.leftPos + 27, this.topPos + 14 + selectMat * 19, 208, 64, 18, 18);
        }

        // Inventory mode overlay (23, 92) when in output mode
        if (this.menu.getInvMode() == 1) {
            guiGraphics.blit(TEXTURE, this.leftPos + 23, this.topPos + 92, 208, 82, 25, 20);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleModeClick(mouseX, mouseY, SHIP_BUTTON_X, LargeShipyardMenu.BUTTON_SHIP_MODE)) {
            return true;
        }

        if (this.handleModeClick(mouseX, mouseY, EQUIP_BUTTON_X, LargeShipyardMenu.BUTTON_EQUIP_MODE)) {
            return true;
        }

        if (!this.menu.canEdit()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        // Inventory mode button (23, 93) to (48, 112)
        if (inside((int) localX, (int) localY, 23, 93, 25, 20)) {
            this.sendButtonClick(LargeShipyardMenu.BUTTON_INVENTORY_MODE);
            return true;
        }

        // Material selection buttons (rows 0..3)
        for (int i = 0; i < 4; i++) {
            int rowY = 14 + i * 19;
            if (inside((int) localX, (int) localY, 27, rowY, 18, 18)
                    || inside((int) localX, (int) localY, 51, rowY + 5, 46, 8)) {
                this.sendButtonClick(LargeShipyardMenu.BUTTON_SELECT_MATERIAL_BASE + i);
                return true;
            }
        }

        // Material adjustment buttons (+1000..-1)
        int selectMat = this.menu.getSelectMat();
        if (selectMat >= 0 && selectMat <= 3) {
            int topRowY = 8 + selectMat * 19;
            int botRowY = 28 + selectMat * 19;

            if (localY >= topRowY && localY <= topRowY + 10) {
                if (localX >= 50 && localX < 62) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 0);
                    return true;
                } else if (localX >= 62 && localX < 74) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 1);
                    return true;
                } else if (localX >= 74 && localX < 86) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 2);
                    return true;
                } else if (localX >= 86 && localX < 98) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 3);
                    return true;
                }
            } else if (localY >= botRowY && localY <= botRowY + 10) {
                if (localX >= 50 && localX < 62) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 4);
                    return true;
                } else if (localX >= 62 && localX < 74) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 5);
                    return true;
                } else if (localX >= 74 && localX < 86) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 6);
                    return true;
                } else if (localX >= 86 && localX < 98) {
                    this.sendButtonClick(LargeShipyardMenu.BUTTON_ADJUST_MATERIAL_BASE + 7);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendButtonClick(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private boolean handleModeClick(double mouseX, double mouseY, int relX, int buttonId) {
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < relX || localX > relX + MODE_BUTTON_SIZE || localY < MODE_BUTTON_Y || localY > MODE_BUTTON_Y + MODE_BUTTON_SIZE) {
            return false;
        }

        this.sendButtonClick(buttonId);
        return true;
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;

        if (inside(localX, localY, 8, 19, 14, 65)) {
            guiGraphics.renderTooltip(this.font, Component.literal(Integer.toString(this.menu.getPowerRemained())), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, SHIP_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("gui.shincolle.shipyard.button.ship")
                            .append(Component.literal(" - "))
                            .append(this.menu.getBuildTypeLabel()),
                    mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, EQUIP_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            guiGraphics.renderTooltip(this.font,
                    Component.translatable("gui.shincolle.shipyard.button.equip")
                            .append(Component.literal(" - "))
                            .append(this.menu.getBuildTypeLabel()),
                    mouseX, mouseY);
        }
    }

    private void renderModeOverlay(GuiGraphics guiGraphics, int buildType, int animationFrame) {
        switch (buildType) {
            case ShipyardBuildTypes.SHIP -> guiGraphics.blit(TEXTURE, this.leftPos + SHIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 208, 64, 18, 18);
            case ShipyardBuildTypes.SHIP_LOOP ->
                    guiGraphics.blit(TEXTURE, this.leftPos + SHIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 208, 103 + animationFrame * 18, 18, 18);
            case ShipyardBuildTypes.EQUIP -> guiGraphics.blit(TEXTURE, this.leftPos + EQUIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 208, 64, 18, 18);
            case ShipyardBuildTypes.EQUIP_LOOP ->
                    guiGraphics.blit(TEXTURE, this.leftPos + EQUIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 208, 103 + animationFrame * 18, 18, 18);
            default -> {
            }
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        guiGraphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

    private void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        guiGraphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

    private static int materialColor(int amount) {
        if (amount < 100) {
            return 0xD15C5C;
        } else if (amount == 1000) {
            return 0xE0C54E;
        }
        return 0xF0F0F0;
    }
}
