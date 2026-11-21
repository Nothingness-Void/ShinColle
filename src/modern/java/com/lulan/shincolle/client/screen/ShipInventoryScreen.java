package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.menu.ShipInventoryMenu;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.network.ShipGuiButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

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
    private static final int TEAM_BUTTON_X = 173;
    private static final int TEAM_BUTTON_Y = 64;
    private static final int TEAM_BUTTON_W = 20;
    private static final int TEAM_BUTTON_H = 14;
    private static final int FORM_BUTTON_X = 195;
    private static final int FORM_BUTTON_Y = 64;
    private static final int FORM_BUTTON_W = 47;
    private static final int FORM_BUTTON_H = 14;
    private static final int STOP_BUTTON_X = 173;
    private static final int STOP_BUTTON_Y = 120;
    private static final int STOP_BUTTON_W = 69;
    private static final int STOP_BUTTON_H = 10;
    private static final int HP_BAR_X = 173;
    private static final int HP_BAR_Y = 96;
    private static final int HP_BAR_W = 69;
    private static final int HP_BAR_H = 8;
    private static final int BEHAVIOR_AREA_X = 173;
    private static final int BEHAVIOR_AREA_Y = 105;
    private static final int BEHAVIOR_AREA_W = 69;
    private static final int BEHAVIOR_AREA_H = 13;
    private static final int TOP_PANEL_LEFT = 173;
    private static final int TOP_PANEL_RIGHT = 242;
    private static final int BOTTOM_PANEL_LEFT = 171;
    private static final int BOTTOM_PANEL_RIGHT = 246;
    private static final int MARRIAGE_ROW_Y = 186;
    private static final int MARRIAGE_ROW_H = 9;

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
        guiGraphics.fill(this.leftPos + TEAM_BUTTON_X, this.topPos + TEAM_BUTTON_Y,
                this.leftPos + TEAM_BUTTON_X + TEAM_BUTTON_W, this.topPos + TEAM_BUTTON_Y + TEAM_BUTTON_H, 0xAA36445A);
        guiGraphics.fill(this.leftPos + FORM_BUTTON_X, this.topPos + FORM_BUTTON_Y,
                this.leftPos + FORM_BUTTON_X + FORM_BUTTON_W, this.topPos + FORM_BUTTON_Y + FORM_BUTTON_H, 0xAA5A3C36);
        guiGraphics.fill(this.leftPos + STOP_BUTTON_X, this.topPos + STOP_BUTTON_Y,
                this.leftPos + STOP_BUTTON_X + STOP_BUTTON_W, this.topPos + STOP_BUTTON_Y + STOP_BUTTON_H, 0xAA5A2C2C);

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
        guiGraphics.drawCenteredString(this.font, this.menu.getCurrentTeamShortLabel(), TEAM_BUTTON_X + (TEAM_BUTTON_W / 2), TEAM_BUTTON_Y + 3, 0xF0EAE0);
        guiGraphics.drawCenteredString(this.font, this.trimToWidth(this.menu.getCurrentFormationShortLabel().getString(), FORM_BUTTON_W - 4),
                FORM_BUTTON_X + (FORM_BUTTON_W / 2), FORM_BUTTON_Y + 3, 0xF0EAE0);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.shincolle.ship_inventory.stop"),
                STOP_BUTTON_X + (STOP_BUTTON_W / 2), STOP_BUTTON_Y + 2, 0xF0D8D8);

        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.role"), TOP_PANEL_LEFT, 80, 0xE1D7B8, false);
        this.drawRightAligned(guiGraphics, this.trimToWidth(this.menu.getRoleLabel().getString(), 69), TOP_PANEL_RIGHT, 80, 0xF0DCB2);
        guiGraphics.drawString(this.font, Component.translatable("gui.shincolle.ship_inventory.health"), TOP_PANEL_LEFT, 88, 0xE1D7B8, false);
        this.drawRightAligned(guiGraphics, this.menu.getHealthText(), TOP_PANEL_RIGHT, 88, 0xF7F2EB);
        this.renderAiControlArea(guiGraphics);

        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.level"), String.valueOf(this.menu.getShipLevel()), 132);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.attack"), this.menu.getAttackText(), 141);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.speed"), this.menu.getSpeedText(), 150);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.range"), this.menu.getRangeText(), 159);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.morale"), this.menu.getMoraleText(), 168);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.state"), this.menu.getMoraleLabel().getString(), 177);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.marriage"),
                this.menu.getMarriageLabel().getString() + " " + onOff(this.menu.isRingEffectEnabled()), MARRIAGE_ROW_Y);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.modern"),
                this.menu.getModernizationCount() + "  R" + this.menu.getRescueCount() + "  K" + this.menu.getShipKills(), 195);
        this.drawStatRow(guiGraphics, Component.translatable("gui.shincolle.ship_inventory.supply"), this.menu.getSupplyTierText(), 204);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.handleRegionClick(mouseX, mouseY, MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_W, MODE_BUTTON_H, ShipInventoryMenu.BUTTON_TOGGLE_MODE)) {
            return true;
        }
        if (this.sendGameplayCommandOnClick(mouseX, mouseY, TEAM_BUTTON_X, TEAM_BUTTON_Y, TEAM_BUTTON_W, TEAM_BUTTON_H,
                GameplayCommandType.SET_CURRENT_TEAM, tag -> tag.putInt(GameplayCommandHandler.TAG_TEAM_ID, (this.menu.getCurrentTeamId() + 1) % 9))) {
            return true;
        }
        if (this.sendGameplayCommandOnClick(mouseX, mouseY, FORM_BUTTON_X, FORM_BUTTON_Y, FORM_BUTTON_W, FORM_BUTTON_H,
                GameplayCommandType.OPEN_FORMATION_SCREEN, tag -> {
                })) {
            return true;
        }
        if (this.cycleWaypointStayOnClick(mouseX, mouseY, button)) {
            return true;
        }
        if (this.sendShipCommandOnClick(mouseX, mouseY, STOP_BUTTON_X, STOP_BUTTON_Y, STOP_BUTTON_W, STOP_BUTTON_H,
                ServerboundShipCommandPacket.stop(2, this.menu.getShipId(), this.resolveShipUid()))) {
            return true;
        }
        if (this.sendShipCommandOnClick(mouseX, mouseY, BOTTOM_PANEL_LEFT - 2, MARRIAGE_ROW_Y - 1, 78, MARRIAGE_ROW_H,
                ServerboundShipCommandPacket.toggleRingEffect(this.menu.getShipId(), this.resolveShipUid()))) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleRegionClick(double mouseX, double mouseY, int relX, int relY, int width, int height, int buttonId) {
        return this.sendShipCommandOnClick(mouseX, mouseY, relX, relY, width, height, switch (buttonId) {
            case ShipInventoryMenu.BUTTON_TOGGLE_MODE ->
                    ServerboundShipCommandPacket.toggleSit(0, this.menu.getShipId(), this.resolveShipUid());
            default -> null;
        });
    }

    private boolean cycleWaypointStayOnClick(double mouseX, double mouseY, int button) {
        if (!this.menu.canEdit() || (button != 0 && button != 1)) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < BEHAVIOR_AREA_X || localX > BEHAVIOR_AREA_X + BEHAVIOR_AREA_W
                || localY < BEHAVIOR_AREA_Y || localY > BEHAVIOR_AREA_Y + BEHAVIOR_AREA_H) {
            return false;
        }

        if (Screen.hasShiftDown()) {
            this.adjustCombatRationLevel(button == 1 ? -1 : 1);
            return true;
        }

        this.adjustWaypointStay(button == 1 ? -1 : 1);
        return true;
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
        if (inside(localX, localY, TEAM_BUTTON_X, TEAM_BUTTON_Y, TEAM_BUTTON_W, TEAM_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.team"),
                    this.menu.getCurrentTeamLabel()), mouseX, mouseY);
            return;
        }
        if (inside(localX, localY, FORM_BUTTON_X, FORM_BUTTON_Y, FORM_BUTTON_W, FORM_BUTTON_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.formation"),
                    this.menu.getCurrentFormationLabel()), mouseX, mouseY);
            return;
        }
        if (inside(localX, localY, STOP_BUTTON_X, STOP_BUTTON_Y, STOP_BUTTON_W, STOP_BUTTON_H)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.shincolle.ship_inventory.tooltip.stop"), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, HP_BAR_X, HP_BAR_Y, HP_BAR_W, HP_BAR_H)) {
            guiGraphics.renderTooltip(this.font, Component.literal(this.menu.getHealthText()), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, TOP_PANEL_LEFT, BEHAVIOR_AREA_Y, BEHAVIOR_AREA_W, BEHAVIOR_AREA_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.literal("Z/X/C/B/N: Melee AmmoL AmmoH AirL AirH"),
                     Component.literal("T: PassiveAI  Y: PVP  U: OnSight  I: AA  G: ASM"),
                     Component.literal("P: PickItem  O: Aura  H: ShowHeld"),
                     Component.literal("J/K: FollowMin  Shift+J/K: FollowMax"),
                     Component.literal("[ ]: WpStay  Shift+[ ]: FleeHP " + this.menu.getFleeHp()),
                     Component.literal("PgUp/PgDn: Page " + (this.menu.getInventoryPage() + 1)
                             + " / " + (Math.min(this.menu.getInventoryPageSize(), 2) + 1)),
                     Component.literal("L: TimeKeeper  " + onOff(this.menu.isTimeKeeper())
                             + "  V: AutoPump " + onOff(this.menu.isAutoPump())),
                     Component.literal("AutoCR: " + this.menu.getCombatRationLevel() + " (Shift-click +/-)"),
                     Component.literal("Waypoint stay: " + this.menu.getWaypointStayText()),
                    this.menu.getSensorBehaviorLabel(),
                    this.menu.getUtilityBehaviorLabel(),
                    this.menu.getRouteBehaviorLabel(),
                    this.menu.getTorpedoBehaviorLabel(),
                    this.menu.getMarriageBonusLabel()),
                    mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, PREVIEW_PANEL_X, PREVIEW_PANEL_Y, PREVIEW_PANEL_W, PREVIEW_PANEL_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    this.menu.getRoleLabel(),
                    Component.literal(this.menu.getMoraleLabel().getString() + "  " + this.menu.getMoraleText()),
                    Component.translatable("gui.shincolle.exp").append(Component.literal(" " + this.menu.getExperienceText())),
                    Component.literal("Fuel " + this.menu.getFuelText()),
                    Component.literal("Ammo L " + this.menu.getLightAmmoText() + "  H " + this.menu.getHeavyAmmoText()),
                    Component.literal("Grudge " + this.menu.getGrudgeText()),
                    Component.literal("Food " + this.menu.getFoodSaturationText())), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, 144, 18, 16, 108)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.equip")), mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, BOTTOM_PANEL_LEFT - 2, MARRIAGE_ROW_Y - 1, 78, MARRIAGE_ROW_H)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    this.menu.getMarriageLabel(),
                    Component.translatable("gui.shincolle.auraeffect"),
                    this.menu.getRingEffectLabel(),
                    Component.translatable("gui.shincolle.ship_inventory.tooltip.ring_effect")),
                    mouseX, mouseY);
            return;
        }

        if (inside(localX, localY, BOTTOM_PANEL_LEFT - 2, 130, 78, 78)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("gui.shincolle.ship_inventory.slots"),
                    Component.literal((this.menu.getEquipmentUsedSlots() + this.menu.getCargoUsedSlots()) + " / " + this.menu.getInventorySizePaged())),
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

    private boolean sendGameplayCommandOnClick(double mouseX, double mouseY,
                                               int relX, int relY, int width, int height,
                                               GameplayCommandType commandType,
                                               java.util.function.Consumer<CompoundTag> payloadBuilder) {
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < relX || localX > relX + width || localY < relY || localY > relY + height) {
            return false;
        }

        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(commandType, payload));
        return true;
    }

    private boolean sendShipCommandOnClick(double mouseX, double mouseY,
                                           int relX, int relY, int width, int height,
                                           ServerboundShipCommandPacket packet) {
        if (packet == null) {
            return false;
        }
        if (!this.menu.canEdit()) {
            return false;
        }

        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;
        if (localX < relX || localX > relX + width || localY < relY || localY > relY + height) {
            return false;
        }

        ModNetwork.sendToServer(packet);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.menu.canEdit()) {
            LegacyShipEntity ship = this.menu.getShip();
            boolean shift = Screen.hasShiftDown();
            int flags = this.menu.getAiFlags();
            switch (keyCode) {
                case GLFW.GLFW_KEY_Z -> this.sendToggle(ShipGuiButton.MELEE, ship != null && ship.useAttackMelee());
                case GLFW.GLFW_KEY_X -> this.sendToggle(ShipGuiButton.AMMO_LIGHT, ship != null && ship.useAttackLight());
                case GLFW.GLFW_KEY_C -> this.sendToggle(ShipGuiButton.AMMO_HEAVY, ship != null && ship.useAttackHeavy());
                case GLFW.GLFW_KEY_B -> this.sendToggle(ShipGuiButton.AIR_LIGHT, ship != null && ship.useAttackAirLight());
                case GLFW.GLFW_KEY_N -> this.sendToggle(ShipGuiButton.AIR_HEAVY, ship != null && ship.useAttackAirHeavy());
                case GLFW.GLFW_KEY_T -> this.sendToggle(ShipGuiButton.TAR_AI,
                        (flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) == 0);
                case GLFW.GLFW_KEY_Y -> this.sendToggle(ShipGuiButton.PVP_AI, ship != null && ship.isAiPvpFirst());
                case GLFW.GLFW_KEY_U -> this.sendToggle(ShipGuiButton.ON_SIGHT_AI, ship != null && ship.isAiOnSightChase());
                case GLFW.GLFW_KEY_I -> this.sendToggle(ShipGuiButton.AA_AI, ship != null && ship.isAiAntiAir());
                case GLFW.GLFW_KEY_G -> this.sendToggle(ShipGuiButton.ASM_AI, ship != null && ship.isAiAntiSub());
                case GLFW.GLFW_KEY_P -> this.sendToggle(ShipGuiButton.PICKITEM_AI,
                        (flags & GameplayCommandHandler.AI_FLAG_PICK_ITEM) != 0);
                case GLFW.GLFW_KEY_H -> this.sendToggle(ShipGuiButton.SHOW_HELD, ship != null && ship.canShowHeldItem());
                case GLFW.GLFW_KEY_O -> this.toggleRingEffect();
                case GLFW.GLFW_KEY_J -> this.adjustFollow(shift, -1);
                case GLFW.GLFW_KEY_K -> this.adjustFollow(shift, 1);
                case GLFW.GLFW_KEY_LEFT_BRACKET -> {
                    if (shift) {
                        this.adjustFleeHp(-1);
                    } else {
                        this.adjustWaypointStay(-1);
                    }
                }
                case GLFW.GLFW_KEY_RIGHT_BRACKET -> {
                    if (shift) {
                        this.adjustFleeHp(1);
                    } else {
                        this.adjustWaypointStay(1);
                    }
                }
                case GLFW.GLFW_KEY_L -> this.toggleTimeKeeper();
                case GLFW.GLFW_KEY_V -> this.toggleAutoPump();
                case GLFW.GLFW_KEY_PAGE_UP -> this.changeInventoryPage(this.menu.getInventoryPage() - 1);
                case GLFW.GLFW_KEY_PAGE_DOWN -> this.changeInventoryPage(this.menu.getInventoryPage() + 1);
                default -> {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderAiControlArea(GuiGraphics guiGraphics) {
        int flags = this.menu.getAiFlags();
        boolean passive = (flags & GameplayCommandHandler.AI_FLAG_AUTO_TARGET) == 0;
        boolean pickItem = (flags & GameplayCommandHandler.AI_FLAG_PICK_ITEM) != 0;
        LegacyShipEntity ship = this.menu.getShip();
        boolean pvp = ship != null && ship.isAiPvpFirst();

        this.drawScaledString(guiGraphics,
                "Pa:" + onOff(passive) + " P:" + onOff(pvp)
                        + " I:" + onOff(pickItem) + " W:" + onOff(this.menu.isRingEffectEnabled()),
                TOP_PANEL_LEFT, 106, 0xD0D7DC, 0.75F);
        this.drawScaledString(guiGraphics,
                "F" + this.menu.getFollowMin() + "-" + this.menu.getFollowMax()
                        + " Pg" + (this.menu.getInventoryPage() + 1) + " E" + this.menu.getRouteEnergyText(),
                TOP_PANEL_LEFT, 112, 0xD0D7DC, 0.75F);
    }

    private void sendGuiButton(int button, int value) {
        ModNetwork.sendToServer(ServerboundShipCommandPacket.guiButton(
                this.menu.getShipId(), this.resolveShipUid(), button, value));
    }

    /** Legacy GuiShipInventory sends getInverseInt(currentSwitch). */
    private void sendToggle(int button, boolean current) {
        this.sendGuiButton(button, current ? 0 : 1);
    }

    /** Legacy follow sliders: FollowMin 1..31, FollowMax 2..32. */
    private void adjustFollow(boolean max, int delta) {
        if (max) {
            this.sendGuiButton(ShipGuiButton.FOLLOW_MAX, Mth.clamp(this.menu.getFollowMax() + delta, 2, 32));
        } else {
            this.sendGuiButton(ShipGuiButton.FOLLOW_MIN, Mth.clamp(this.menu.getFollowMin() + delta, 1, 31));
        }
    }

    /** Legacy flee-HP slider range 0..100. */
    private void adjustFleeHp(int delta) {
        this.sendGuiButton(ShipGuiButton.FLEE_HP, Mth.clamp(this.menu.getFleeHp() + delta, 0, 100));
    }

    /** Legacy GuiShipInventory only sends page 1/2 when DrumState unlocks it. */
    private void changeInventoryPage(int page) {
        if (page >= 0 && page <= Math.min(this.menu.getInventoryPageSize(), 2)) {
            this.sendGuiButton(ShipGuiButton.INV_PAGE, page);
        }
    }

    private void adjustWaypointStay(int delta) {
        int mode = this.menu.getWaypointStayMode();
        int next = Math.floorMod(mode + delta, LegacyShipEntity.MAX_WP_STAY_MODE + 1);
        ModNetwork.sendToServer(ServerboundShipCommandPacket.setWaypointStay(this.menu.getShipId(), this.resolveShipUid(), next));
    }

    private void toggleRingEffect() {
        ModNetwork.sendToServer(ServerboundShipCommandPacket.toggleRingEffect(this.menu.getShipId(), this.resolveShipUid()));
    }

    private void toggleTimeKeeper() {
        ModNetwork.sendToServer(ServerboundShipCommandPacket.setTimeKeeper(
                this.menu.getShipId(), this.resolveShipUid(), !this.menu.isTimeKeeper()));
    }

    private void toggleAutoPump() {
        ModNetwork.sendToServer(ServerboundShipCommandPacket.setAutoPump(
                this.menu.getShipId(), this.resolveShipUid(), !this.menu.isAutoPump()));
    }

    private void adjustCombatRationLevel(int delta) {
        int next = Mth.clamp(this.menu.getCombatRationLevel() + delta, 1, 4);
        ModNetwork.sendToServer(ServerboundShipCommandPacket.setCombatRationLevel(
                this.menu.getShipId(), this.resolveShipUid(), next));
    }

    private int resolveShipUid() {
        return this.menu.getShipUid();
    }

    private static String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private void drawScaledString(GuiGraphics guiGraphics, String text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }
}
