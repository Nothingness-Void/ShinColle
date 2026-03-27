package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.WaypointTerminalMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class WaypointTerminalScreen extends AbstractContainerScreen<WaypointTerminalMenu> {

    private @Nullable Button stayButton;
    private @Nullable Button clearNextButton;
    private @Nullable Button clearLastButton;
    private @Nullable Button clearChestButton;

    public WaypointTerminalScreen(WaypointTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 228;
        this.imageHeight = 176;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos + 12;
        int top = this.topPos + 106;

        this.stayButton = this.addRenderableWidget(makeButton(left, top, 204, 20, WaypointTerminalMenu.BUTTON_CYCLE_STAY));
        this.clearLastButton = this.addRenderableWidget(makeButton(left, top + 24, 98, 20, WaypointTerminalMenu.BUTTON_CLEAR_LAST));
        this.clearNextButton = this.addRenderableWidget(makeButton(left + 106, top + 24, 110, 20, WaypointTerminalMenu.BUTTON_CLEAR_NEXT));
        this.clearChestButton = this.addRenderableWidget(makeButton(left, top + 48, 204, 20, WaypointTerminalMenu.BUTTON_CLEAR_CHEST));
        this.refreshButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.refreshButtons();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 12, 12, 0xF3E6D0, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.waypoint.owner", this.menu.getOwnerLabel()),
                12, 28, 0xF39C91, false);

        String posText = "X: " + this.menu.getWaypointPos().getX()
                + "  Y: " + this.menu.getWaypointPos().getY()
                + "  Z: " + this.menu.getWaypointPos().getZ();
        guiGraphics.drawString(this.font, posText, 12, 42, 0xD1C7C7, false);

        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.waypoint.current_stay", this.menu.getStayLabel()),
                12, 58, 0xFFD46E, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.crane.route.prev", formatPos(this.menu.getLastWaypoint(), ChatFormatting.LIGHT_PURPLE)),
                12, 74, 0xE2D7DC, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.crane.route.next", formatPos(this.menu.getNextWaypoint(), ChatFormatting.AQUA)),
                12, 84, 0xE2D7DC, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.crane.route.chest", formatPos(this.menu.getPairedChest(), ChatFormatting.GOLD)),
                12, 94, 0xE2D7DC, false);
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
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF170C10);
        guiGraphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3, 0xFF2A1118);
        guiGraphics.fill(left + 8, top + 8, left + this.imageWidth - 8, top + this.imageHeight - 8, 0xFF42161E);
        guiGraphics.fill(left + 10, top + 52, left + this.imageWidth - 10, top + 100, 0x55230D12);
        guiGraphics.fill(left + 10, top + 104, left + this.imageWidth - 10, top + this.imageHeight - 10, 0x66380F16);
    }

    private Button makeButton(int x, int y, int width, int height, int buttonId) {
        return Button.builder(Component.empty(), button -> this.sendMenuButton(buttonId))
                .bounds(x, y, width, height)
                .build();
    }

    private void sendMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private void refreshButtons() {
        if (this.stayButton == null) {
            return;
        }

        this.stayButton.setMessage(Component.translatable("gui.shincolle.waypoint.button.stay", this.menu.getStayLabel()));
        this.clearLastButton.setMessage(Component.translatable("gui.shincolle.waypoint.button.clear_last"));
        this.clearNextButton.setMessage(Component.translatable("gui.shincolle.waypoint.button.clear_next"));
        this.clearChestButton.setMessage(Component.translatable("gui.shincolle.waypoint.button.clear_chest"));

        boolean editable = this.menu.canEdit();
        this.stayButton.active = editable;
        this.clearLastButton.active = editable;
        this.clearNextButton.active = editable;
        this.clearChestButton.active = editable;
    }

    private static Component formatPos(@Nullable net.minecraft.core.BlockPos pos, ChatFormatting color) {
        if (pos == null) {
            return Component.translatable("gui.shincolle.waypoint.none").withStyle(ChatFormatting.DARK_GRAY);
        }

        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(color);
    }
}
