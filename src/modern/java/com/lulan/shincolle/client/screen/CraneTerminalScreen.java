package com.lulan.shincolle.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.menu.CraneTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CraneTerminalScreen extends AbstractContainerScreen<CraneTerminalMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShinColle.MOD_ID, "textures/gui/gui_crane.png");
    private static final int[][] BUTTONS = {
            {7, 6, 20, 19, CraneTerminalMenu.BUTTON_POWER},
            {23, 6, 90, 19, CraneTerminalMenu.BUTTON_WAIT_MODE},
            {22, 21, 35, 34, CraneTerminalMenu.BUTTON_TOGGLE_META},
            {36, 21, 49, 34, CraneTerminalMenu.BUTTON_TOGGLE_OREDICT},
            {7, 52, 80, 63, CraneTerminalMenu.BUTTON_TOGGLE_LOAD},
            {7, 83, 80, 94, CraneTerminalMenu.BUTTON_TOGGLE_UNLOAD},
            {50, 21, 63, 34, CraneTerminalMenu.BUTTON_TOGGLE_NBT},
            {64, 21, 77, 34, CraneTerminalMenu.BUTTON_REDSTONE_MODE},
            {23, 36, 36, 49, CraneTerminalMenu.BUTTON_LIQUID_MODE},
            {39, 36, 52, 49, CraneTerminalMenu.BUTTON_ENERGY_MODE}
    };

    public CraneTerminalScreen(CraneTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 201;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.menu.getWaitModeLabel(), 57, 9, 0xFFFF00);
        graphics.drawString(this.font, Component.translatable("gui.shincolle.crane.filter.load"), 21, 54, 0xFF3333, false);
        graphics.drawString(this.font, Component.translatable("gui.shincolle.crane.filter.unload"), 21, 85, 0x000000, false);
        var ship = this.menu.getShip();
        if (ship != null) {
            String time = CraneTerminalMenu.formatCraneTime(this.menu.getCraneTime());
            graphics.drawString(this.font, time, 133 - this.font.width(time) / 2, 10, 0x303030, false);
            Component name = ship.getCustomName();
            if (name == null || name.getString().isEmpty()) {
                name = ship.getSpec().displayName();
            }
            graphics.drawString(this.font, name, 80, 24, 0xFFFFFF, true);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderHoverTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isActive()) {
            this.blitButton(graphics, 7, 6, 176, 0, 13, 13);
        }
        if (this.menu.isCheckMetadata()) {
            this.blitButton(graphics, 23, 22, 176, 13, 11, 11);
        }
        if (this.menu.isCheckOredict()) {
            this.blitButton(graphics, 37, 22, 176, 24, 11, 11);
        }
        if (this.menu.isCheckNbt()) {
            this.blitButton(graphics, 51, 22, 176, 46, 11, 11);
        }
        if (!this.menu.isLoadEnabled()) {
            this.blitButton(graphics, 7, 52, 176, 35, 11, 11);
            this.blitButton(graphics, 8, 65, 0, 201, 160, 16);
        }
        if (!this.menu.isUnloadEnabled()) {
            this.blitButton(graphics, 7, 83, 176, 35, 11, 11);
            this.blitButton(graphics, 8, 96, 0, 201, 160, 16);
        }
        int redstone = this.menu.getCrane() == null ? 0 : this.menu.getCrane().getRedstoneMode();
        if (redstone == 1 || redstone == 2) {
            this.blitButton(graphics, 65, 22, 176, redstone == 1 ? 57 : 68, 11, 11);
        }
        int liquid = this.menu.getCrane() == null ? 0 : this.menu.getCrane().getLiquidMode();
        this.blitButton(graphics, 23, 36, liquid == 1 ? 176 : liquid == 2 ? 189 : 202, 101, 13, 13);
        for (int slot = 0; slot < 18; slot++) {
            if (this.menu.isFilterInverted(slot)) {
                this.blitButton(graphics, 7 + (slot % 9) * 18, slot < 9 ? 64 : 95, 0, 217, 18, 18);
            }
        }
        RenderSystem.disableBlend();
    }

    private void blitButton(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(TEXTURE, this.leftPos + x, this.topPos + y, u, v, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) (mouseX - this.leftPos);
        int y = (int) (mouseY - this.topPos);
        for (int[] region : BUTTONS) {
            if (x >= region[0] && x <= region[2] && y >= region[1] && y <= region[3]) {
                if (this.menu.canEdit() && this.minecraft != null && this.minecraft.gameMode != null) {
                    int id = region[4];
                    if (id == CraneTerminalMenu.BUTTON_WAIT_MODE && button != 0) {
                        id = CraneTerminalMenu.BUTTON_WAIT_MODE_DECR;
                    }
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
                    return true;
                }
                break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;
        Component label = null;
        if (y > 21 && y < 34) {
            if (x > 22 && x < 35) {
                label = Component.translatable("gui.shincolle.crane.usemeta");
            } else if (x > 36 && x < 49) {
                label = Component.translatable("gui.shincolle.crane.useoredict");
            } else if (x > 50 && x < 63) {
                label = Component.translatable("gui.shincolle.crane.usenbt");
            } else if (x > 64 && x < 77) {
                label = this.menu.getRedstoneModeLabel();
            }
        } else if (y > 35 && y < 50 && x > 22 && x < 37) {
            label = this.menu.getLiquidModeLabel();
        }
        if (label != null) {
            graphics.renderTooltip(this.font, label, mouseX, mouseY + 10);
        }
        if (x > 22 && x < 91 && y > 5 && y < 20) {
            int mode = this.menu.getCrane() == null ? 0 : this.menu.getCrane().getWaitMode();
            String key = switch (mode) {
                case 0 -> "nowait";
                case 1 -> "untilfull";
                case 2 -> "untilempty";
                case 3 -> "excess";
                case 4 -> "remain";
                default -> null;
            };
            if (key != null) {
                List<Component> lines = mode == 0
                        ? List.of(Component.translatable("gui.shincolle.crane." + key + "1"))
                        : List.of(Component.translatable("gui.shincolle.crane." + key + "1"),
                                Component.translatable("gui.shincolle.crane." + key + "2"));
                graphics.renderTooltip(this.font, lines.stream().map(Component::getVisualOrderText).toList(), this.leftPos - 50, this.topPos + 37);
            }
        }
    }
}
