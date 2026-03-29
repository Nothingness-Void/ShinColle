package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.team.TeamData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class DeskTerminalScreen extends AbstractContainerScreen<DeskTerminalMenu> {

    private static final int TEAM_BTN_X = 130;
    private static final int TEAM_BTN_Y = 10;
    private static final int TEAM_BTN_W = 40;
    private static final int TEAM_BTN_H = 14;
    private static final int FORM_BTN_X = 174;
    private static final int FORM_BTN_Y = 10;
    private static final int FORM_BTN_W = 40;
    private static final int FORM_BTN_H = 14;
    private static final int STOP_BTN_X = 130;
    private static final int STOP_BTN_Y = 28;
    private static final int STOP_BTN_W = 84;
    private static final int STOP_BTN_H = 14;

    private @Nullable EditBox textInput;
    private @Nullable EditBox relationInput;
    private @Nullable Button createButton;
    private @Nullable Button disbandButton;
    private @Nullable Button renameButton;
    private @Nullable Button allyAddButton;
    private @Nullable Button allyRemoveButton;
    private @Nullable Button banAddButton;
    private @Nullable Button banRemoveButton;
    private @Nullable Button targetAddButton;
    private @Nullable Button targetRemoveButton;

    public DeskTerminalScreen(DeskTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 228;
        this.imageHeight = 190;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.textInput = this.addRenderableWidget(new EditBox(this.font, left + 12, top + 62, 116, 14,
                Component.translatable("gui.shincolle.desk.input.primary")));
        this.textInput.setMaxLength(64);

        this.relationInput = this.addRenderableWidget(new EditBox(this.font, left + 132, top + 62, 84, 14,
                Component.translatable("gui.shincolle.desk.input.relation")));
        this.relationInput.setFilter(value -> value.length() <= 9 && value.chars().allMatch(Character::isDigit));

        this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.create"),
                        button -> this.createTeam())
                .bounds(left + 12, top + 80, 66, 16)
                .build());
        this.disbandButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.disband"),
                        button -> this.disbandTeam())
                .bounds(left + 82, top + 80, 66, 16)
                .build());
        this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.rename"),
                        button -> this.renameTeam())
                .bounds(left + 152, top + 80, 64, 16)
                .build());

        this.allyAddButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.ally_add"),
                        button -> this.changeAlly(true))
                .bounds(left + 12, top + 100, 50, 16)
                .build());
        this.allyRemoveButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.ally_remove"),
                        button -> this.changeAlly(false))
                .bounds(left + 66, top + 100, 50, 16)
                .build());
        this.banAddButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.ban_add"),
                        button -> this.changeBan(true))
                .bounds(left + 120, top + 100, 50, 16)
                .build());
        this.banRemoveButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.team.ban_remove"),
                        button -> this.changeBan(false))
                .bounds(left + 174, top + 100, 42, 16)
                .build());

        this.targetAddButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.target.add"),
                        button -> this.changeTargetClass(true))
                .bounds(left + 12, top + 100, 98, 16)
                .build());
        this.targetRemoveButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.shincolle.desk.target.remove"),
                        button -> this.changeTargetClass(false))
                .bounds(left + 116, top + 100, 100, 16)
                .build());

        this.refreshWidgets();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.refreshWidgets();
        if (this.menu.getSelectedFunction() == DeskReferenceMenu.BOOK_VARIANT && this.textInput != null && !this.textInput.isFocused()) {
            String ownTeamName = this.menu.getOwnTeamName();
            if (!ownTeamName.isBlank() && this.textInput.getValue().isBlank()) {
                this.textInput.setValue(ownTeamName);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 12, 12, 0xF3E8C8, false);
        guiGraphics.drawString(this.font, this.menu.getSelectedFunctionName(), 12, 28, 0x8FD5E8, false);
        guiGraphics.drawCenteredString(this.font, this.menu.getCurrentTeamLabel(), TEAM_BTN_X + TEAM_BTN_W / 2, TEAM_BTN_Y + 3, 0xF0F0E8);
        guiGraphics.drawCenteredString(this.font, this.menu.getCurrentFormationLabel(), FORM_BTN_X + FORM_BTN_W / 2, FORM_BTN_Y + 3, 0xF0F0E8);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.shincolle.ship_inventory.stop"), STOP_BTN_X + STOP_BTN_W / 2, STOP_BTN_Y + 3, 0xF0D8D8);

        String posText = "X: " + this.menu.getDeskPos().getX()
                + "  Y: " + this.menu.getDeskPos().getY()
                + "  Z: " + this.menu.getDeskPos().getZ();
        guiGraphics.drawString(this.font, posText, 12, 44, 0xC0C7CF, false);

        if (this.menu.getSelectedFunction() == DeskReferenceMenu.BOOK_VARIANT) {
            renderTeamPage(guiGraphics);
            return;
        }

        renderTargetPage(guiGraphics);
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
        guiGraphics.fill(left + 10, top + 54, left + this.imageWidth - 10, top + this.imageHeight - 10, 0x662D3F48);
        guiGraphics.fill(left + TEAM_BTN_X, top + TEAM_BTN_Y, left + TEAM_BTN_X + TEAM_BTN_W, top + TEAM_BTN_Y + TEAM_BTN_H, 0xAA36445A);
        guiGraphics.fill(left + FORM_BTN_X, top + FORM_BTN_Y, left + FORM_BTN_X + FORM_BTN_W, top + FORM_BTN_Y + FORM_BTN_H, 0xAA5A3C36);
        guiGraphics.fill(left + STOP_BTN_X, top + STOP_BTN_Y, left + STOP_BTN_X + STOP_BTN_W, top + STOP_BTN_Y + STOP_BTN_H, 0xAA5A2C2C);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.sendCommand(mouseX, mouseY, TEAM_BTN_X, TEAM_BTN_Y, TEAM_BTN_W, TEAM_BTN_H, GameplayCommandType.SET_CURRENT_TEAM, tag -> {
            tag.putInt(GameplayCommandHandler.TAG_TEAM_ID, (this.menu.getCurrentTeamId() + 1) % 9);
        })) {
            return true;
        }
        if (this.sendCommand(mouseX, mouseY, FORM_BTN_X, FORM_BTN_Y, FORM_BTN_W, FORM_BTN_H, GameplayCommandType.OPEN_FORMATION_SCREEN, tag -> {
        })) {
            return true;
        }
        if (this.sendCommand(mouseX, mouseY, STOP_BTN_X, STOP_BTN_Y, STOP_BTN_W, STOP_BTN_H, GameplayCommandType.STOP_COMMAND, tag -> {
            tag.putInt(GameplayCommandHandler.TAG_MODE, 2);
            tag.putInt(GameplayCommandHandler.TAG_SHIP_ID, -1);
        })) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderTeamPage(GuiGraphics guiGraphics) {
        boolean hasTeam = this.menu.hasOwnTeam();
        String ownLabel = hasTeam
                ? this.menu.getOwnTeamId() + " / " + this.menu.getOwnTeamName()
                : "-";
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.desk.team.status", hasTeam, this.menu.getTeamCooldown(), ownLabel),
                12, 122, 0xD8E6FF, false);

        int y = 134;
        List<TeamData> teams = this.menu.getKnownTeams();
        for (int i = 0; i < teams.size() && i < 4; i++) {
            TeamData team = teams.get(i);
            guiGraphics.drawString(this.font,
                    Component.literal("#" + team.getTeamId() + "  " + team.getTeamName()),
                    12, y, 0xEAD7A6, false);
            y += 11;
        }
    }

    private void renderTargetPage(GuiGraphics guiGraphics) {
        List<String> targets = this.menu.getTargetClasses();
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.desk.target.count", targets.size()),
                12, 122, 0xD8E6FF, false);

        int y = 134;
        for (int i = 0; i < targets.size() && i < 4; i++) {
            guiGraphics.drawString(this.font, Component.literal(targets.get(i)), 12, y, 0xEAD7A6, false);
            y += 11;
        }
    }

    private boolean sendCommand(double mouseX, double mouseY,
                                int x, int y, int w, int h,
                                GameplayCommandType commandType,
                                Consumer<CompoundTag> payloadBuilder) {
        int localX = (int) (mouseX - this.leftPos);
        int localY = (int) (mouseY - this.topPos);
        if (localX < x || localX > x + w || localY < y || localY > y + h) {
            return false;
        }

        this.sendGameplayPacket(commandType, payloadBuilder);
        return true;
    }

    private void sendGameplayPacket(GameplayCommandType commandType, Consumer<CompoundTag> payloadBuilder) {
        CompoundTag payload = new CompoundTag();
        payloadBuilder.accept(payload);
        ModNetwork.sendToServer(ServerboundGameplayCommandPacket.of(commandType, payload));
    }

    private void createTeam() {
        String name = this.textInput == null ? "" : this.textInput.getValue().trim();
        this.sendGameplayPacket(GameplayCommandType.DESK_CREATE_TEAM,
                payload -> payload.putString(GameplayCommandHandler.TAG_TEAM_NAME, name));
    }

    private void disbandTeam() {
        this.sendGameplayPacket(GameplayCommandType.DESK_DISBAND_TEAM, payload -> {
        });
    }

    private void renameTeam() {
        if (this.textInput == null) {
            return;
        }
        String name = this.textInput.getValue().trim();
        if (name.isBlank()) {
            return;
        }
        this.sendGameplayPacket(GameplayCommandType.DESK_RENAME_TEAM,
                payload -> payload.putString(GameplayCommandHandler.TAG_TEAM_NAME, name));
    }

    private void changeAlly(boolean add) {
        int teamId = parseRelationTeamId();
        if (teamId <= 0) {
            return;
        }
        this.sendGameplayPacket(add ? GameplayCommandType.DESK_ADD_ALLY : GameplayCommandType.DESK_REMOVE_ALLY,
                payload -> payload.putInt(GameplayCommandHandler.TAG_RELATION_TEAM_ID, teamId));
    }

    private void changeBan(boolean add) {
        int teamId = parseRelationTeamId();
        if (teamId <= 0) {
            return;
        }
        this.sendGameplayPacket(add ? GameplayCommandType.DESK_ADD_BANNED : GameplayCommandType.DESK_REMOVE_BANNED,
                payload -> payload.putInt(GameplayCommandHandler.TAG_RELATION_TEAM_ID, teamId));
    }

    private void changeTargetClass(boolean add) {
        if (this.textInput == null) {
            return;
        }
        String targetClass = this.textInput.getValue().trim();
        if (targetClass.isBlank()) {
            return;
        }
        this.sendGameplayPacket(add ? GameplayCommandType.TARGET_CLASS_ADD : GameplayCommandType.TARGET_CLASS_REMOVE,
                payload -> payload.putString(GameplayCommandHandler.TAG_TARGET_CLASS, targetClass));
    }

    private int parseRelationTeamId() {
        if (this.relationInput == null || this.relationInput.getValue().isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(this.relationInput.getValue());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void refreshWidgets() {
        boolean isBook = this.menu.getSelectedFunction() == DeskReferenceMenu.BOOK_VARIANT;
        if (this.relationInput != null) {
            this.relationInput.visible = isBook;
            this.relationInput.setEditable(isBook);
        }
        if (this.textInput != null) {
            this.textInput.visible = true;
            this.textInput.setEditable(true);
        }

        if (this.createButton != null) {
            this.createButton.visible = isBook;
            this.createButton.active = isBook;
        }
        if (this.disbandButton != null) {
            this.disbandButton.visible = isBook;
            this.disbandButton.active = isBook;
        }
        if (this.renameButton != null) {
            this.renameButton.visible = isBook;
            this.renameButton.active = isBook;
        }
        if (this.allyAddButton != null) {
            this.allyAddButton.visible = isBook;
            this.allyAddButton.active = isBook;
        }
        if (this.allyRemoveButton != null) {
            this.allyRemoveButton.visible = isBook;
            this.allyRemoveButton.active = isBook;
        }
        if (this.banAddButton != null) {
            this.banAddButton.visible = isBook;
            this.banAddButton.active = isBook;
        }
        if (this.banRemoveButton != null) {
            this.banRemoveButton.visible = isBook;
            this.banRemoveButton.active = isBook;
        }

        boolean isRadar = this.menu.getSelectedFunction() == DeskReferenceMenu.RADAR_VARIANT;
        if (this.targetAddButton != null) {
            this.targetAddButton.visible = isRadar;
            this.targetAddButton.active = isRadar;
        }
        if (this.targetRemoveButton != null) {
            this.targetRemoveButton.visible = isRadar;
            this.targetRemoveButton.active = isRadar;
        }
    }
}
