package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.menu.DeskTerminalMenu;
import com.lulan.shincolle.network.GameplayCommandHandler;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.team.TeamData;
import net.minecraft.client.gui.Font;
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
    private static final int TEAM_LIST_X = 12;
    private static final int TEAM_LIST_Y = 145;
    private static final int TEAM_COL_W = 101;
    private static final int TEAM_ROW_H = 10;
    private static final int TARGET_LIST_X = 12;
    private static final int TARGET_LIST_Y = 145;
    private static final int TARGET_COL_W = 101;
    private static final int TARGET_ROW_H = 10;
    private static final int LIST_ROWS = 4;

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
            this.renderTeamPage(guiGraphics);
            return;
        }

        this.renderTargetPage(guiGraphics);
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
        guiGraphics.fill(left + TEAM_LIST_X - 2, top + TEAM_LIST_Y - 2, left + this.imageWidth - 12, top + this.imageHeight - 12, 0x33202B32);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.sendCommand(mouseX, mouseY, TEAM_BTN_X, TEAM_BTN_Y, TEAM_BTN_W, TEAM_BTN_H, GameplayCommandType.SET_CURRENT_TEAM, tag ->
                tag.putInt(GameplayCommandHandler.TAG_TEAM_ID, (this.menu.getCurrentTeamId() + 1) % 9))) {
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

        if (this.menu.getSelectedFunction() == DeskReferenceMenu.BOOK_VARIANT && this.handleTeamListClick(mouseX, mouseY)) {
            return true;
        }
        if (this.menu.getSelectedFunction() == DeskReferenceMenu.RADAR_VARIANT && this.handleTargetListClick(mouseX, mouseY)) {
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
        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.team.allylist").append(": " + this.menu.getOwnAllyCount())
                        .append("    ")
                        .append(Component.translatable("gui.shincolle.team.banlist"))
                        .append(": " + this.menu.getOwnBannedCount()),
                12, 133, 0xB9CCD9, false);

        List<TeamData> teams = this.menu.getKnownTeams();
        int offset = this.menu.getTeamListOffset();
        for (int index = 0; index < LIST_ROWS * 2; index++) {
            int absoluteIndex = offset + index;
            if (absoluteIndex >= teams.size()) {
                break;
            }

            TeamData team = teams.get(absoluteIndex);
            int column = index / LIST_ROWS;
            int row = index % LIST_ROWS;
            int x = TEAM_LIST_X + column * TEAM_COL_W;
            int y = TEAM_LIST_Y + row * TEAM_ROW_H;
            int color = switch (this.menu.getTeamRelation(team.getTeamId())) {
                case OWN -> 0xFFE3C071;
                case ALLIED -> 0xFF8FD1AF;
                case HOSTILE -> 0xFFE48787;
                case NEUTRAL -> 0xFFD5D7DB;
            };
            String rowText = this.menu.getCompactTeamRow(team);
            guiGraphics.drawString(this.font,
                    this.truncate(rowText, TEAM_COL_W - 6),
                    x, y, color, false);
        }
    }

    private void renderTargetPage(GuiGraphics guiGraphics) {
        List<String> targets = this.menu.getTargetClasses();
        List<String> worldRules = this.menu.getWorldUnattackableClasses();

        guiGraphics.drawString(this.font,
                Component.translatable("gui.shincolle.desk.target.count", targets.size()),
                12, 122, 0xD8E6FF, false);
        guiGraphics.drawString(this.font,
                Component.translatable("item.shincolle.optool").append(" x" + worldRules.size()),
                126, 122, 0xD8E6FF, false);

        for (int row = 0; row < LIST_ROWS; row++) {
            if (row < targets.size()) {
                guiGraphics.drawString(this.font,
                        this.truncate(targets.get(row), TARGET_COL_W - 6),
                        TARGET_LIST_X, TARGET_LIST_Y + row * TARGET_ROW_H, 0xFFEAD7A6, false);
            }
            if (row < worldRules.size()) {
                guiGraphics.drawString(this.font,
                        this.truncate(worldRules.get(row), TARGET_COL_W - 6),
                        TARGET_LIST_X + TARGET_COL_W, TARGET_LIST_Y + row * TARGET_ROW_H, 0xFFB5C6E8, false);
            }
        }
    }

    private boolean handleTeamListClick(double mouseX, double mouseY) {
        int localX = (int) (mouseX - this.leftPos);
        int localY = (int) (mouseY - this.topPos);
        if (localY < TEAM_LIST_Y || localY >= TEAM_LIST_Y + LIST_ROWS * TEAM_ROW_H) {
            return false;
        }

        int relativeX = localX - TEAM_LIST_X;
        if (relativeX < 0 || relativeX >= TEAM_COL_W * 2) {
            return false;
        }

        int column = relativeX / TEAM_COL_W;
        int row = (localY - TEAM_LIST_Y) / TEAM_ROW_H;
        int index = column * LIST_ROWS + row + this.menu.getTeamListOffset();
        List<TeamData> teams = this.menu.getKnownTeams();
        if (index < 0 || index >= teams.size()) {
            return false;
        }

        TeamData team = teams.get(index);
        if (this.relationInput != null) {
            this.relationInput.setValue(Integer.toString(team.getTeamId()));
        }
        if (this.textInput != null && !team.getTeamName().isBlank()) {
            this.textInput.setValue(team.getTeamName());
        }
        return true;
    }

    private boolean handleTargetListClick(double mouseX, double mouseY) {
        int localX = (int) (mouseX - this.leftPos);
        int localY = (int) (mouseY - this.topPos);
        if (localY < TARGET_LIST_Y || localY >= TARGET_LIST_Y + LIST_ROWS * TARGET_ROW_H) {
            return false;
        }

        int relativeX = localX - TARGET_LIST_X;
        if (relativeX < 0 || relativeX >= TARGET_COL_W * 2) {
            return false;
        }

        int column = relativeX / TARGET_COL_W;
        int row = (localY - TARGET_LIST_Y) / TARGET_ROW_H;
        List<String> values = column == 0 ? this.menu.getTargetClasses() : this.menu.getWorldUnattackableClasses();
        if (row < 0 || row >= values.size()) {
            return false;
        }

        if (this.textInput != null) {
            this.textInput.setValue(values.get(row));
        }
        return true;
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
        int teamId = this.parseRelationTeamId();
        if (teamId <= 0) {
            return;
        }
        this.sendGameplayPacket(add ? GameplayCommandType.DESK_ADD_ALLY : GameplayCommandType.DESK_REMOVE_ALLY,
                payload -> payload.putInt(GameplayCommandHandler.TAG_RELATION_TEAM_ID, teamId));
    }

    private void changeBan(boolean add) {
        int teamId = this.parseRelationTeamId();
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

    private String truncate(String value, int width) {
        Font font = this.font;
        if (font.width(value) <= width) {
            return value;
        }

        String ellipsis = "...";
        return font.plainSubstrByWidth(value, Math.max(0, width - font.width(ellipsis))) + ellipsis;
    }
}
