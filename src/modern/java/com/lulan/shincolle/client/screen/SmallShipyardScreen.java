package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.crafting.ShipyardBuildTypes;
import com.lulan.shincolle.menu.SmallShipyardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SmallShipyardScreen extends AbstractContainerScreen<SmallShipyardMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_small_shipyard.png");
    private static final int SHIP_BUTTON_X = 123;
    private static final int EQUIP_BUTTON_X = 143;
    private static final int MODE_BUTTON_Y = 17;
    private static final int MODE_BUTTON_SIZE = 18;
    private int animationTick;

    public SmallShipyardScreen(SmallShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 164;
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
        guiGraphics.drawString(this.font, buildTime, 71 - this.font.width(buildTime) / 2, 51, 0x404040, false);

        Component failure = this.menu.getFailureMessage();
        if (!failure.getString().isBlank()) {
            drawCenteredText(guiGraphics, failure, 80, 67, 0xFF2A73);
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

        int fuelBar = this.menu.getPowerRemainingScaled(31);
        if (fuelBar > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 10, this.topPos + 48 - fuelBar, 176, 47 - fuelBar, 12, fuelBar);
        }

        renderModeOverlay(guiGraphics, this.menu.getBuildType(), this.animationTick / 6 % 6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleModeClick(mouseX, mouseY, SHIP_BUTTON_X, SmallShipyardMenu.BUTTON_SHIP_MODE)) {
            return true;
        }

        if (this.handleModeClick(mouseX, mouseY, EQUIP_BUTTON_X, SmallShipyardMenu.BUTTON_EQUIP_MODE)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
            return true;
        }

        return false;
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;

        if (inside(localX, localY, 9, 17, 14, 32)) {
            guiGraphics.renderTooltip(this.font, Component.literal(Integer.toString(this.menu.getPowerRemained())), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, SHIP_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            renderComponentTooltip(guiGraphics, List.of(
                    Component.translatable("gui.shincolle.shipyard.button.ship"),
                    shipModeTooltip()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, EQUIP_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            renderComponentTooltip(guiGraphics, List.of(
                    Component.translatable("gui.shincolle.shipyard.button.equip"),
                    equipModeTooltip()), mouseX, mouseY);
        }
    }

    private Component shipModeTooltip() {
        int buildType = this.menu.getBuildType();
        if (ShipyardBuildTypes.isShipMode(buildType)) {
            return Component.translatable("gui.shincolle.shipyard.mode.current", this.menu.getBuildTypeLabel());
        }

        return Component.translatable("gui.shincolle.shipyard.mode.current", Component.translatable("gui.shincolle.shipyard.mode.none"));
    }

    private Component equipModeTooltip() {
        int buildType = this.menu.getBuildType();
        if (ShipyardBuildTypes.isEquipMode(buildType)) {
            return Component.translatable("gui.shincolle.shipyard.mode.current", this.menu.getBuildTypeLabel());
        }

        return Component.translatable("gui.shincolle.shipyard.mode.current", Component.translatable("gui.shincolle.shipyard.mode.none"));
    }

    private void renderModeOverlay(GuiGraphics guiGraphics, int buildType, int animationFrame) {
        switch (buildType) {
            case ShipyardBuildTypes.SHIP -> guiGraphics.blit(TEXTURE, this.leftPos + SHIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 176, 47, 18, 18);
            case ShipyardBuildTypes.SHIP_LOOP -> guiGraphics.blit(TEXTURE, this.leftPos + SHIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 176, 65 + animationFrame * 18, 18, 18);
            case ShipyardBuildTypes.EQUIP -> guiGraphics.blit(TEXTURE, this.leftPos + EQUIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 176, 47, 18, 18);
            case ShipyardBuildTypes.EQUIP_LOOP -> guiGraphics.blit(TEXTURE, this.leftPos + EQUIP_BUTTON_X, this.topPos + MODE_BUTTON_Y, 176, 65 + animationFrame * 18, 18, 18);
            default -> {
            }
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawCenteredText(GuiGraphics guiGraphics, Component component, int centerX, int y, int color) {
        guiGraphics.drawString(this.font, component, centerX - this.font.width(component) / 2, y, color, false);
    }

    private void renderComponentTooltip(GuiGraphics guiGraphics, List<Component> lines, int mouseX, int mouseY) {
        List<FormattedCharSequence> formatted = lines.stream()
                .map(Component::getVisualOrderText)
                .toList();
        guiGraphics.renderTooltip(this.font, formatted, mouseX, mouseY);
    }
}
