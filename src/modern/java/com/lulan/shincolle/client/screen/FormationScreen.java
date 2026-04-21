package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.FormationMenu;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.teitoku.TeitokuData;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FormationScreen extends AbstractContainerScreen<FormationMenu> {

    private static final int SLOT_AREA_X = 12;
    private static final int SLOT_AREA_Y = 60;
    private static final int SLOT_AREA_W = 232;
    private static final int SLOT_LINE_H = 14;

    private EditBox teamNameInput;
    private Button prevTeamButton;
    private Button nextTeamButton;
    private Button cycleFormationButton;
    private Button clearTeamButton;
    private Button swapUpButton;
    private Button swapDownButton;
    private Button renameTeamButton;
    private Button openShipButton;

    private int selectedSlot;
    private int lastClickedSlot = -1;
    private long lastClickedAt;

    public FormationScreen(FormationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 192;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.teamNameInput = this.addRenderableWidget(new EditBox(this.font, left + 12, top + 34, 128, 16,
                Component.translatable("gui.shincolle.formation.team_name")));
        this.teamNameInput.setMaxLength(32);

        this.prevTeamButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.switchTeam(-1))
                .bounds(left + 148, top + 34, 20, 16)
                .build());
        this.nextTeamButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.switchTeam(1))
                .bounds(left + 170, top + 34, 20, 16)
                .build());
        this.cycleFormationButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.cycle"),
                        button -> this.sendCommand(GameplayCommandType.CYCLE_FORMATION, payload -> {
                        }))
                .bounds(left + 194, top + 34, 50, 16)
                .build());

        this.clearTeamButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.clear"),
                        button -> this.sendCommand(GameplayCommandType.CLEAR_CURRENT_TEAM, payload -> {
                        }))
                .bounds(left + 12, top + 152, 56, 18)
                .build());
        this.swapUpButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.swap_up"),
                        button -> this.swapSlot(true))
                .bounds(left + 72, top + 152, 46, 18)
                .build());
        this.swapDownButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.swap_down"),
                        button -> this.swapSlot(false))
                .bounds(left + 122, top + 152, 46, 18)
                .build());
        this.renameTeamButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.rename"),
                        button -> this.renameOwnTeam())
                .bounds(left + 172, top + 152, 34, 18)
                .build());
        this.openShipButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.formation.open_ship"),
                        button -> this.openSelectedShip())
                .bounds(left + 210, top + 152, 34, 18)
                .build());

        this.selectedSlot = 0;
        this.refreshTextField();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.refreshTextField();
        this.openShipButton.active = this.menu.getShipUid(this.selectedSlot) > 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int localX = (int) (mouseX - this.leftPos);
        int localY = (int) (mouseY - this.topPos);
        if (localX >= SLOT_AREA_X && localX < SLOT_AREA_X + SLOT_AREA_W
                && localY >= SLOT_AREA_Y && localY < SLOT_AREA_Y + TeitokuData.TEAM_SIZE * SLOT_LINE_H) {
            int slot = (localY - SLOT_AREA_Y) / SLOT_LINE_H;
            this.onSlotClicked(slot);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 12, 10, 0xE7E2D8, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.ship_inventory.team", this.menu.getCurrentTeamId() + 1),
                12, 22, 0xF2D78E, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.formation.format" + this.menu.getCurrentFormationId()),
                90, 22, 0xA0D9F0, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.formation.ship_count", this.menu.countCurrentTeamShips(), 6),
                190, 22, 0xD2D2D2, false);

        for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
            int y = SLOT_AREA_Y + slot * SLOT_LINE_H + 3;
            int color = this.selectedSlot == slot ? 0xFFF3C96A : this.menu.isSlotSelected(slot) ? 0xFF9AD0A1 : 0xFFD5D7DB;
            guiGraphics.drawString(this.font, this.menu.getSlotLabel(slot), SLOT_AREA_X + 4, y, color, false);
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
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF141B23);
        guiGraphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3, 0xFF273646);
        guiGraphics.fill(left + 8, top + 8, left + this.imageWidth - 8, top + this.imageHeight - 8, 0xFF1A2835);
        guiGraphics.fill(left + SLOT_AREA_X, top + SLOT_AREA_Y,
                left + SLOT_AREA_X + SLOT_AREA_W, top + SLOT_AREA_Y + TeitokuData.TEAM_SIZE * SLOT_LINE_H, 0x55394A57);

        for (int slot = 0; slot < TeitokuData.TEAM_SIZE; slot++) {
            int rowTop = top + SLOT_AREA_Y + slot * SLOT_LINE_H;
            int baseColor = slot == this.selectedSlot ? 0x4477502A : this.menu.isSlotSelected(slot) ? 0x3343753E : 0x22343D47;
            guiGraphics.fill(left + SLOT_AREA_X + 1, rowTop + 1, left + SLOT_AREA_X + SLOT_AREA_W - 1, rowTop + SLOT_LINE_H - 1, baseColor);
        }
    }

    private void onSlotClicked(int slot) {
        this.selectedSlot = slot;
        long now = Util.getMillis();
        boolean doubleClick = slot == this.lastClickedSlot && now - this.lastClickedAt < 250L;
        this.lastClickedSlot = slot;
        this.lastClickedAt = now;

        if (doubleClick) {
            this.openSelectedShip();
            return;
        }

        boolean nextSelected = !this.menu.isSlotSelected(slot);
        this.sendCommand(GameplayCommandType.SET_SLOT_SELECTION, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_SLOT, slot);
            payload.putBoolean(GameplayCommandHandler.TAG_SELECTED, nextSelected);
        });
    }

    private void switchTeam(int delta) {
        int teamId = this.menu.getCurrentTeamId();
        int next = (teamId + delta + TeitokuData.TEAM_COUNT) % TeitokuData.TEAM_COUNT;
        this.sendCommand(GameplayCommandType.SET_CURRENT_TEAM, payload -> payload.putInt(GameplayCommandHandler.TAG_TEAM_ID, next));
    }

    private void swapSlot(boolean up) {
        int to = up ? (this.selectedSlot + TeitokuData.TEAM_SIZE - 1) % TeitokuData.TEAM_SIZE
                : (this.selectedSlot + 1) % TeitokuData.TEAM_SIZE;
        int from = this.selectedSlot;
        this.sendCommand(GameplayCommandType.SWAP_TEAM_SLOT, payload -> {
            payload.putInt(GameplayCommandHandler.TAG_SLOT_FROM, from);
            payload.putInt(GameplayCommandHandler.TAG_SLOT_TO, to);
        });
        this.selectedSlot = to;
    }

    private void renameOwnTeam() {
        String name = this.teamNameInput.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        this.sendCommand(GameplayCommandType.DESK_RENAME_TEAM, payload -> payload.putString(GameplayCommandHandler.TAG_TEAM_NAME, name));
    }

    private void openSelectedShip() {
        int shipUid = this.menu.getShipUid(this.selectedSlot);
        if (shipUid <= 0) {
            return;
        }

        this.sendCommand(GameplayCommandType.OPEN_SHIP_INVENTORY, payload -> payload.putInt(GameplayCommandHandler.TAG_SHIP_UID, shipUid));
    }

    private void refreshTextField() {
        if (this.teamNameInput == null || this.teamNameInput.isFocused()) {
            return;
        }
        String current = this.menu.getCurrentTeamName();
        if (!current.equals(this.teamNameInput.getValue())) {
            this.teamNameInput.setValue(current);
        }
    }

    private void sendCommand(GameplayCommandType type, java.util.function.Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(type, payload));
    }
}
