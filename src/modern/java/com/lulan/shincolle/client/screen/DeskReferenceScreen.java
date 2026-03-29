package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.combat.WorldCombatRulesSavedData;
import com.lulan.shincolle.blockentity.CraneBlockEntity;
import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.blockentity.HeavyGrudgeBlockEntity;
import com.lulan.shincolle.blockentity.LargeShipyardBlockEntity;
import com.lulan.shincolle.blockentity.LegacyCoreBlockEntity;
import com.lulan.shincolle.blockentity.RouteNode;
import com.lulan.shincolle.blockentity.SmallShipyardBlockEntity;
import com.lulan.shincolle.blockentity.WaypointBlockEntity;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.menu.DeskReferenceMenu;
import com.lulan.shincolle.morph.MorphRuntimeState;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeskReferenceScreen extends AbstractContainerScreen<DeskReferenceMenu> {

    private static final int LEFT_PANEL_X = 10;
    private static final int LEFT_PANEL_Y = 30;
    private static final int LEFT_PANEL_W = 102;
    private static final int LEFT_PANEL_H = 66;
    private static final int RIGHT_PANEL_X = 116;
    private static final int RIGHT_PANEL_Y = 30;
    private static final int RIGHT_PANEL_W = 114;
    private static final int RIGHT_PANEL_H = 66;
    private static final int BOTTOM_PANEL_X = 10;
    private static final int BOTTOM_PANEL_Y = 100;
    private static final int BOTTOM_PANEL_W = 220;
    private static final int BOTTOM_PANEL_H = 84;
    private static final int TOP_BUTTON_Y = 8;
    private static final int BUTTON_W = 14;
    private static final int BUTTON_H = 14;
    private static final int PREV_BUTTON_X = 10;
    private static final int NEXT_BUTTON_X = 216;
    private static final int RADAR_MAP_X = BOTTOM_PANEL_X + 6;
    private static final int RADAR_MAP_Y = BOTTOM_PANEL_Y + 16;
    private static final int RADAR_MAP_W = 104;
    private static final int RADAR_MAP_H = 62;
    private static final int RADAR_LIST_X = BOTTOM_PANEL_X + 118;
    private static final int RADAR_LIST_Y = BOTTOM_PANEL_Y + 18;
    private static final int RADAR_LIST_ROW_H = 9;
    private static final int[] RADAR_ZOOMS = {256, 64, 16};
    private static final int BOOK_CHAPTER_COUNT = 5;

    private int bookChapter;
    private int bookPage;
    private int radarZoomIndex;
    private int selectedRadarIndex = -1;

    public DeskReferenceScreen(DeskReferenceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 240;
        this.imageHeight = 194;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 12, 12, 0xF3E8C8, false);
        if (this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT) {
            guiGraphics.drawCenteredString(this.font, Component.literal(this.getBookHeaderLabel()), this.imageWidth / 2, 22, 0x8FD5E8);
            this.renderBookContent(guiGraphics);
        } else {
            guiGraphics.drawCenteredString(this.font,
                    Component.literal("Radar Zoom " + RADAR_ZOOMS[this.radarZoomIndex] + "m"),
                    this.imageWidth / 2, 22, 0x8FD5E8);
            this.renderRadarContent(guiGraphics);
        }
        guiGraphics.drawString(this.font, "<", PREV_BUTTON_X + 4, TOP_BUTTON_Y + 2, 0xE6EDF3, false);
        guiGraphics.drawString(this.font, ">", NEXT_BUTTON_X + 4, TOP_BUTTON_Y + 2, 0xE6EDF3, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderHoverTooltips(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        int accent = this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT ? 0xFF6A5136 : 0xFF31515D;
        int accentSoft = this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT ? 0x885A422D : 0x88405A66;

        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171B22);
        guiGraphics.fill(left + 3, top + 3, left + this.imageWidth - 3, top + this.imageHeight - 3, 0xFF24303A);
        guiGraphics.fill(left + 8, top + 8, left + this.imageWidth - 8, top + this.imageHeight - 8, 0xFF10262B);
        guiGraphics.fill(left + 10, top + 28, left + this.imageWidth - 10, top + this.imageHeight - 10, 0x662D3F48);
        guiGraphics.fill(left + LEFT_PANEL_X, top + LEFT_PANEL_Y, left + LEFT_PANEL_X + LEFT_PANEL_W, top + LEFT_PANEL_Y + LEFT_PANEL_H, accentSoft);
        guiGraphics.fill(left + RIGHT_PANEL_X, top + RIGHT_PANEL_Y, left + RIGHT_PANEL_X + RIGHT_PANEL_W, top + RIGHT_PANEL_Y + RIGHT_PANEL_H, accentSoft);
        guiGraphics.fill(left + BOTTOM_PANEL_X, top + BOTTOM_PANEL_Y, left + BOTTOM_PANEL_X + BOTTOM_PANEL_W, top + BOTTOM_PANEL_Y + BOTTOM_PANEL_H, accentSoft);
        guiGraphics.fill(left + 10, top + 28, left + this.imageWidth - 10, top + 30, accent);
        guiGraphics.fill(left + LEFT_PANEL_X, top + LEFT_PANEL_Y, left + LEFT_PANEL_X + LEFT_PANEL_W, top + LEFT_PANEL_Y + 2, accent);
        guiGraphics.fill(left + RIGHT_PANEL_X, top + RIGHT_PANEL_Y, left + RIGHT_PANEL_X + RIGHT_PANEL_W, top + RIGHT_PANEL_Y + 2, accent);
        guiGraphics.fill(left + BOTTOM_PANEL_X, top + BOTTOM_PANEL_Y, left + BOTTOM_PANEL_X + BOTTOM_PANEL_W, top + BOTTOM_PANEL_Y + 2, accent);
        guiGraphics.fill(left + PREV_BUTTON_X, top + TOP_BUTTON_Y, left + PREV_BUTTON_X + BUTTON_W, top + TOP_BUTTON_Y + BUTTON_H, 0xAA31404A);
        guiGraphics.fill(left + NEXT_BUTTON_X, top + TOP_BUTTON_Y, left + NEXT_BUTTON_X + BUTTON_W, top + TOP_BUTTON_Y + BUTTON_H, 0xAA31404A);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int localX = (int) (mouseX - this.leftPos);
        int localY = (int) (mouseY - this.topPos);
        if (inside(localX, localY, PREV_BUTTON_X, TOP_BUTTON_Y, BUTTON_W, BUTTON_H)) {
            this.handlePrevButton();
            return true;
        }
        if (inside(localX, localY, NEXT_BUTTON_X, TOP_BUTTON_Y, BUTTON_W, BUTTON_H)) {
            this.handleNextButton();
            return true;
        }
        if (this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT && this.handleBookClick(localX, localY)) {
            return true;
        }
        if (this.menu.getVariant() == DeskReferenceMenu.RADAR_VARIANT && this.handleRadarClick(localX, localY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderBookContent(GuiGraphics guiGraphics) {
        @Nullable TeitokuData data = this.getClientTeitokuData();
        @Nullable Player player = this.minecraft != null ? this.minecraft.player : null;
        @Nullable TeamData ownTeam = data == null ? null : TeitokuHelper.getClientTeamData().get(data.getPlayerUid());
        BookPageData pageData = this.buildBookPageData(data, ownTeam, player);

        guiGraphics.drawString(this.font, Component.literal("Chapters"), LEFT_PANEL_X + 4, LEFT_PANEL_Y + 6, 0xF0E4C3, false);
        for (int chapter = 0; chapter < BOOK_CHAPTER_COUNT; chapter++) {
            int y = LEFT_PANEL_Y + 18 + chapter * 10;
            int color = chapter == this.bookChapter ? 0xFFF2E2B0 : 0xFFD8E6FF;
            guiGraphics.drawString(this.font, Component.literal((chapter + 1) + ". " + this.getBookChapterLabel(chapter)), LEFT_PANEL_X + 4, y, color, false);
        }
        guiGraphics.drawString(this.font, Component.literal("Use < > to turn pages"), LEFT_PANEL_X + 4, LEFT_PANEL_Y + 58, 0x908C83, false);

        guiGraphics.drawString(this.font, Component.literal(pageData.title()), RIGHT_PANEL_X + 4, RIGHT_PANEL_Y + 6, 0xF0E4C3, false);
        this.drawWrappedLines(guiGraphics, pageData.lines(), RIGHT_PANEL_X + 4, RIGHT_PANEL_Y + 18, RIGHT_PANEL_W - 8, 0xEAD7A6);

        guiGraphics.drawString(this.font, Component.literal("Notes"), BOTTOM_PANEL_X + 4, BOTTOM_PANEL_Y + 6, 0xF0E4C3, false);
        this.drawWrappedLines(guiGraphics, pageData.notes(), BOTTOM_PANEL_X + 4, BOTTOM_PANEL_Y + 18, BOTTOM_PANEL_W - 8, 0xD8E6FF);
    }

    private void renderRadarContent(GuiGraphics guiGraphics) {
        @Nullable TeitokuData data = this.getClientTeitokuData();
        @Nullable Player player = this.minecraft != null ? this.minecraft.player : null;
        @Nullable TeamData ownTeam = data == null ? null : TeitokuHelper.getClientTeamData().get(data.getPlayerUid());
        List<RadarContact> contacts = this.filterRadarContactsByZoom(this.collectRadarContacts(player, ownTeam, data));

        int owned = 0;
        int allied = 0;
        int hostile = 0;
        int neutral = 0;
        int items = 0;
        int facilities = 0;
        for (RadarContact contact : contacts) {
            switch (contact.relation()) {
                case OWN -> owned++;
                case ALLY -> allied++;
                case HOSTILE -> hostile++;
                case NEUTRAL -> neutral++;
                case ITEM -> items++;
            }
            if (contact.kind() == ContactKind.ROUTE || contact.kind() == ContactKind.FACILITY) {
                facilities++;
            }
        }
        this.selectedRadarIndex = Mth.clamp(this.selectedRadarIndex, contacts.isEmpty() ? -1 : 0, contacts.size() - 1);
        RadarContact selected = this.selectedRadarIndex >= 0 && this.selectedRadarIndex < contacts.size()
                ? contacts.get(this.selectedRadarIndex)
                : (!contacts.isEmpty() ? contacts.get(0) : null);

        guiGraphics.drawString(this.font, Component.literal("Sweep"), LEFT_PANEL_X + 4, LEFT_PANEL_Y + 6, 0xF0E4C3, false);
        int lineY = LEFT_PANEL_Y + 18;
        guiGraphics.drawString(this.font, Component.literal("Zoom: " + RADAR_ZOOMS[this.radarZoomIndex] + "m"), LEFT_PANEL_X + 4, lineY, 0xD8E6FF, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Contacts: " + contacts.size()), LEFT_PANEL_X + 4, lineY, 0xD8E6FF, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Own/Ally: " + owned + " / " + allied), LEFT_PANEL_X + 4, lineY, 0x8FE3B3, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Hostile: " + hostile), LEFT_PANEL_X + 4, lineY, 0xF28C8C, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Neutral/Item: " + neutral + " / " + items), LEFT_PANEL_X + 4, lineY, 0xD7D7D7, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Facilities: " + facilities), LEFT_PANEL_X + 4, lineY, 0xD8E6FF, false);
        lineY += 10;
        guiGraphics.drawString(this.font, Component.literal("Targets: " + (data != null ? data.getTargetClassCount() : 0)), LEFT_PANEL_X + 4, lineY, 0xD8E6FF, false);

        guiGraphics.drawString(this.font, Component.literal("Selected"), RIGHT_PANEL_X + 4, RIGHT_PANEL_Y + 6, 0xF0E4C3, false);
        List<String> worldBlocked = this.getWorldBlockedClasses();
        int targetY = RIGHT_PANEL_Y + 18;
        if (selected == null) {
            guiGraphics.drawString(this.font, Component.literal("No contact in this layer"), RIGHT_PANEL_X + 4, targetY, 0x908C83, false);
            targetY += 10;
        } else {
            guiGraphics.drawString(this.font, Component.literal(this.trimToWidth(selected.name(), 102)), RIGHT_PANEL_X + 4, targetY, selected.relation().color, false);
            targetY += 10;
            guiGraphics.drawString(this.font, Component.literal("Type: " + selected.kind().label), RIGHT_PANEL_X + 4, targetY, 0xEAD7A6, false);
            targetY += 10;
            guiGraphics.drawString(this.font, Component.literal("Relation: " + selected.relation().shortLabel), RIGHT_PANEL_X + 4, targetY, 0xEAD7A6, false);
            targetY += 10;
            guiGraphics.drawString(this.font, Component.literal("Pos: " + selected.x() + ", " + selected.z()), RIGHT_PANEL_X + 4, targetY, 0xEAD7A6, false);
            targetY += 10;
            guiGraphics.drawString(this.font, Component.literal("Height: " + selected.y() + "  Dist: " + selected.distance()), RIGHT_PANEL_X + 4, targetY, 0xEAD7A6, false);
            targetY += 10;
            guiGraphics.drawString(this.font, Component.literal(selected.healthText()), RIGHT_PANEL_X + 4, targetY, 0xEAD7A6, false);
            targetY += 10;
        }

        guiGraphics.drawString(this.font, Component.literal("World Shield: " + worldBlocked.size()), RIGHT_PANEL_X + 4, targetY, 0xF28C8C, false);
        targetY += 10;
        guiGraphics.drawString(this.font, Component.literal(worldBlocked.isEmpty()
                ? "Use OP Tool to edit"
                : this.trimToWidth(worldBlocked.get(0), 102)), RIGHT_PANEL_X + 4, targetY,
                worldBlocked.isEmpty() ? 0x908C83 : 0xF6C0C0, false);

        guiGraphics.drawString(this.font, Component.literal("Map & Contacts"), BOTTOM_PANEL_X + 4, BOTTOM_PANEL_Y + 6, 0xF0E4C3, false);
        this.renderRadarMap(guiGraphics, contacts, selected);
        this.renderRadarList(guiGraphics, contacts);
    }

    private List<RadarContact> collectRadarContacts(@Nullable Player player, @Nullable TeamData ownTeam, @Nullable TeitokuData data) {
        if (player == null) {
            return List.of();
        }
        List<RadarContact> contacts = new ArrayList<>();
        int selfTeamId = data != null ? data.getPlayerUid() : 0;
        double maxRadius = RADAR_ZOOMS[0];
        double maxRadiusSq = maxRadius * maxRadius;

        List<LegacyShipEntity> ships = player.level().getEntitiesOfClass(
                LegacyShipEntity.class,
                player.getBoundingBox().inflate(maxRadius),
                ship -> ship.isAlive() && ship.distanceToSqr(player) <= maxRadiusSq);

        for (LegacyShipEntity ship : ships) {
            Relation relation = classifyRelation(ship, selfTeamId, ownTeam, player);
            int distance = Mth.floor((float) ship.distanceTo(player));
            String healthText = Mth.floor(ship.getHealth()) + "/" + Mth.floor(ship.getMaxHealth());
            contacts.add(new RadarContact(
                    ship.getName().getString(),
                    distance,
                    healthText,
                    relation,
                    ContactKind.SHIP,
                    Mth.floor((float) ship.getX()),
                    Mth.floor((float) ship.getY()),
                    Mth.floor((float) ship.getZ()),
                    ship.getX() - player.getX(),
                    ship.getZ() - player.getZ()));
        }

        List<ItemEntity> items = player.level().getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(maxRadius),
                item -> item.isAlive() && item.distanceToSqr(player) <= maxRadiusSq);
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            contacts.add(new RadarContact(
                    stack.isEmpty() ? "Item" : stack.getHoverName().getString(),
                    Mth.floor((float) item.distanceTo(player)),
                    "Stack: " + Math.max(1, stack.getCount()),
                    Relation.ITEM,
                    ContactKind.ITEM,
                    Mth.floor((float) item.getX()),
                    Mth.floor((float) item.getY()),
                    Mth.floor((float) item.getZ()),
                    item.getX() - player.getX(),
                    item.getZ() - player.getZ()));
        }

        contacts.addAll(this.collectFacilityContacts(player));

        contacts.sort(Comparator.comparingInt(RadarContact::distance).thenComparing(RadarContact::name));
        return contacts;
    }

    private List<RadarContact> collectFacilityContacts(Player player) {
        if (this.minecraft == null || this.minecraft.getSingleplayerServer() == null) {
            return List.of();
        }

        ServerLevel serverLevel = this.minecraft.getSingleplayerServer().getLevel(player.level().dimension());
        if (serverLevel == null) {
            return List.of();
        }

        int radius = RADAR_ZOOMS[0];
        int radiusSq = radius * radius;
        BlockPos playerPos = player.blockPosition();
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int chunkRadius = Mth.ceil(radius / 16.0F) + 1;

        List<RadarContact> contacts = new ArrayList<>();
        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (pos.distSqr(playerPos) > radiusSq) {
                        continue;
                    }
                    RadarContact contact = this.createFacilityContact(serverLevel.getBlockEntity(pos), player);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
            }
        }
        return contacts;
    }

    private @Nullable RadarContact createFacilityContact(@Nullable BlockEntity blockEntity, Player player) {
        if (blockEntity == null) {
            return null;
        }

        ContactKind kind;
        String detail;
        if (blockEntity instanceof WaypointBlockEntity waypoint) {
            kind = ContactKind.ROUTE;
            detail = waypoint.getPairedChest() == null ? "Route: idle" : "Route: supply linked";
        } else if (blockEntity instanceof CraneBlockEntity crane) {
            kind = ContactKind.ROUTE;
            detail = crane.getPairedChest() == null ? "Crane: idle" : "Crane: linked";
        } else if (blockEntity instanceof LargeShipyardBlockEntity shipyard) {
            kind = ContactKind.FACILITY;
            detail = "Shipyard power " + shipyard.getPowerRemained();
        } else if (blockEntity instanceof HeavyGrudgeBlockEntity heavyGrudge) {
            kind = ContactKind.FACILITY;
            detail = heavyGrudge.isStructureComplete()
                    ? "Heavy Grudge: grand shipyard"
                    : "Heavy Grudge: core " + heavyGrudge.getRouteEnergyStored();
        } else if (blockEntity instanceof LegacyCoreBlockEntity core) {
            kind = ContactKind.FACILITY;
            detail = core.isVolCore() ? "VolCore charge " + core.getStoredCharge() : "Core charge " + core.getStoredCharge();
        } else if (blockEntity instanceof DeskBlockEntity) {
            kind = ContactKind.FACILITY;
            detail = "Desk terminal";
        } else if (blockEntity instanceof SmallShipyardBlockEntity) {
            kind = ContactKind.FACILITY;
            detail = "Small shipyard";
        } else if (blockEntity instanceof RouteNode routeNode) {
            kind = ContactKind.ROUTE;
            detail = routeNode.getRouteNodeName().getString();
        } else {
            return null;
        }

        BlockPos pos = blockEntity.getBlockPos();
        return new RadarContact(
                blockEntity.getBlockState().getBlock().getName().getString(),
                Mth.floor((float) player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) >= 0.0D
                        ? (float) Math.sqrt(player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D))
                        : 0.0F),
                detail,
                Relation.OWN,
                kind,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 0.5D - player.getX(),
                pos.getZ() + 0.5D - player.getZ());
    }

    private Relation classifyRelation(LegacyShipEntity ship, int selfTeamId, @Nullable TeamData ownTeam, Player player) {
        if (ship.isOwnedBy(player) || (selfTeamId > 0 && ship.getOwnerUid() == selfTeamId)) {
            return Relation.OWN;
        }
        if (ship.isHostileVariant()) {
            return Relation.HOSTILE;
        }
        if (ownTeam != null) {
            if (ownTeam.isBanned(ship.getOwnerUid())) {
                return Relation.HOSTILE;
            }
            if (ownTeam.isAlly(ship.getOwnerUid())) {
                return Relation.ALLY;
            }
        }
        return Relation.NEUTRAL;
    }

    private @Nullable TeitokuData getClientTeitokuData() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return null;
        }
        return TeitokuHelper.get(this.minecraft.player).resolve().orElse(null);
    }

    private String ringState(@Nullable TeitokuData data) {
        if (data == null || !data.hasRing()) {
            return "NONE";
        }
        return data.isRingActive() ? "ACTIVE" : "IDLE";
    }

    private String getCurrentTeamLabel(@Nullable TeitokuData data) {
        if (data == null) {
            return "-";
        }
        return "#" + (data.getCurrentTeamId() + 1);
    }

    private String getCurrentFormationShort(@Nullable TeitokuData data) {
        return data == null ? "-" : this.shortFormation(data.getCurrentFormationId());
    }

    private String shortFormation(int formationId) {
        return switch (formationId) {
            case 1 -> "Ahead";
            case 2 -> "Double";
            case 3 -> "Diamond";
            case 4 -> "Echelon";
            case 5 -> "Abreast";
            default -> "None";
        };
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int targetWidth = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, targetWidth) + ellipsis;
    }

    private void drawScaledString(GuiGraphics guiGraphics, String text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private void drawWrappedLines(GuiGraphics guiGraphics, List<String> lines, int x, int y, int width, int color) {
        int drawY = y;
        for (String line : lines) {
            for (var wrapped : this.font.split(Component.literal(line), width)) {
                guiGraphics.drawString(this.font, wrapped, x, drawY, color, false);
                drawY += 9;
            }
        }
    }

    private BookPageData buildBookPageData(@Nullable TeitokuData data, @Nullable TeamData ownTeam, @Nullable Player player) {
        String admiralName = player != null ? player.getName().getString() : "-";
        MorphRuntimeState morphState = data != null ? data.getMorphRuntimeState() : new MorphRuntimeState();
        return switch (this.bookChapter) {
            case 0 -> this.bookPage == 0
                    ? new BookPageData("Admiral Record",
                    List.of(
                            "Name: " + admiralName,
                            "UID: " + (data != null ? data.getPlayerUid() : 0),
                            "Ring: " + this.ringState(data),
                            "Marriage Count: " + (data != null ? data.getMarriageNum() : 0),
                            "Collected Ships: " + (data != null ? data.getCollectedShips().size() : 0),
                            "Unlocked Morphs: " + (data != null ? data.getMorphProfileCount() : 0)),
                    List.of(
                            "Front-page admiral record with current save progress.",
                            "Use later chapters for fleet, diplomacy, logistics, and morph runtime summaries."))
                    : new BookPageData("Status Ledger",
                    List.of(
                            "Current Team: " + this.getCurrentTeamLabel(data),
                            "Current Formation: " + this.getCurrentFormationShort(data),
                            "Team Cooldown: " + (data != null ? data.getTeamCooldown() : 0),
                            "Own Team: " + (data != null && data.hasTeam() ? "YES" : "NO"),
                            "Targets: " + (data != null ? data.getTargetClassCount() : 0),
                            "World Shield: " + this.getWorldBlockedClasses().size()),
                    List.of(
                            "This page mirrors the old desk summary ledger in a compact form.",
                            "The live terminal remains the editing surface for team operations."));
            case 1 -> this.bookPage == 0
                    ? new BookPageData("Fleet Sheet",
                    this.buildFleetBookLines(data),
                    List.of(
                            "Teams still preserve 9 fleets x 6 slots.",
                            "Formation bonuses only apply when a fleet fields more than 4 ships."))
                    : new BookPageData("Command Chain",
                    List.of(
                            "Pointer and formation UI now drive server-side command state.",
                            "Ships remember move, guard, stop, and route assignments.",
                            "Per-ship AI stores follow range, target, supply, and route stay toggles.",
                            "Target classes and diplomacy feed automatic engagement filtering."),
                    List.of(
                            "Use the ship GUI for direct control hints.",
                            "Use the terminal when you need to edit whole-fleet state."));
            case 2 -> this.bookPage == 0
                    ? new BookPageData("Diplomacy",
                    List.of(
                            "Own Team Name: " + this.getOwnTeamName(ownTeam),
                            "Allies: " + this.joinInts(ownTeam != null ? ownTeam.getAllies() : List.of()),
                            "Banned: " + this.joinInts(ownTeam != null ? ownTeam.getBanned() : List.of()),
                            "Targets: " + this.joinStrings(data != null ? data.getTargetClasses() : List.of(), 4)),
                    List.of(
                            "Ally add is unilateral, ally remove clears both sides.",
                            "Ban add is bilateral, ban remove is unilateral like the old rules."))
                    : new BookPageData("Shield Rules",
                    List.of(
                            "World Attack Shield Entries: " + this.getWorldBlockedClasses().size(),
                            "Preview: " + this.joinStrings(this.getWorldBlockedClasses(), 3),
                            "Editing Tool: OP Tool",
                            "Hotkeys: Numpad1 toggle, Numpad2 list"),
                    List.of(
                            "World shield rules take priority over normal target classes.",
                            "Use them to exempt friendly or protected entities globally."));
            case 3 -> new BookPageData("Route Logistics",
                    List.of(
                            "Waypoint supplies cargo from linked storage.",
                            "Crane moves item cargo with per-row load/unload filters.",
                            "Liquid mode transfers between tanks or ship tank items.",
                            "Energy mode transfers native ShinColle charge or shipyard power.",
                            "Transport tier affects budget, cadence, and ship energy capacity."),
                    List.of(
                            "Target Wrench can now pair route nodes with containers, tanks, cores, or shipyards.",
                            "Energy mode 1 loads to ship, mode 2 unloads to facility."));
            case 4 -> new BookPageData("Morph Runtime",
                    List.of(
                            "Selected Class: " + morphState.getSelectedClassId(),
                            "State: " + (morphState.isActive() ? "ACTIVE" : "IDLE"),
                            "Host Mode: " + morphState.getHostMode().name(),
                            "Atk Cooldown: " + morphState.getAttackCooldownsCopy()[0],
                            "Special Cooldown: " + morphState.getSpecialCooldown(),
                            "Profiles: " + (data != null ? data.getMorphProfileCount() : 0)),
                    List.of(
                            "Target Wrench right click opens morph inventory.",
                            "Left click your own ship unlocks or selects its morph.",
                            "Tenryuu and Tatsuta currently carry the restored special skills."));
            default -> new BookPageData("Appendix", List.of("No page."), List.of("-"));
        };
    }

    private List<String> buildFleetBookLines(@Nullable TeitokuData data) {
        if (data == null) {
            return List.of("No admiral data synced.");
        }
        List<String> lines = new ArrayList<>();
        for (int team = 0; team < TeitokuData.TEAM_COUNT; team++) {
            String teamName = data.getTeamName(team);
            String displayName = teamName == null || teamName.isBlank() ? "Fleet" : teamName;
            lines.add("#" + (team + 1) + " [" + data.countShipsInTeam(team) + "] "
                    + this.shortFormation(data.getFormationId(team)) + " " + displayName);
        }
        return lines;
    }

    private List<RadarContact> filterRadarContactsByZoom(List<RadarContact> contacts) {
        int radius = RADAR_ZOOMS[this.radarZoomIndex];
        double radiusSq = radius * (double) radius;
        return contacts.stream()
                .filter(contact -> contact.relX() * contact.relX() + contact.relZ() * contact.relZ() <= radiusSq)
                .toList();
    }

    private void renderRadarMap(GuiGraphics guiGraphics, List<RadarContact> contacts, @Nullable RadarContact selected) {
        int mapLeft = RADAR_MAP_X;
        int mapTop = RADAR_MAP_Y;
        int mapRight = RADAR_MAP_X + RADAR_MAP_W;
        int mapBottom = RADAR_MAP_Y + RADAR_MAP_H;
        guiGraphics.fill(mapLeft, mapTop, mapRight, mapBottom, 0x77202832);
        guiGraphics.fill(mapLeft + RADAR_MAP_W / 2, mapTop + 2, mapLeft + RADAR_MAP_W / 2 + 1, mapBottom - 2, 0x334FB0C0);
        guiGraphics.fill(mapLeft + 2, mapTop + RADAR_MAP_H / 2, mapRight - 2, mapTop + RADAR_MAP_H / 2 + 1, 0x334FB0C0);
        guiGraphics.fill(mapLeft + RADAR_MAP_W / 2 - 1, mapTop + RADAR_MAP_H / 2 - 1,
                mapLeft + RADAR_MAP_W / 2 + 2, mapTop + RADAR_MAP_H / 2 + 2, 0xFFEFE7B0);

        double radius = RADAR_ZOOMS[this.radarZoomIndex];
        for (int index = 0; index < contacts.size(); index++) {
            RadarContact contact = contacts.get(index);
            int dotX = mapLeft + RADAR_MAP_W / 2 + Mth.floor((float) ((contact.relX() / radius) * ((RADAR_MAP_W / 2) - 4)));
            int dotY = mapTop + RADAR_MAP_H / 2 + Mth.floor((float) ((contact.relZ() / radius) * ((RADAR_MAP_H / 2) - 4)));
            int size = contact == selected ? 3 : 2;
            guiGraphics.fill(dotX - size, dotY - size, dotX + size + 1, dotY + size + 1, contact.relation().color);
            if (index == this.selectedRadarIndex) {
                guiGraphics.fill(dotX - size - 1, dotY - size - 1, dotX + size + 2, dotY - size, 0xFFF2E2B0);
                guiGraphics.fill(dotX - size - 1, dotY + size + 1, dotX + size + 2, dotY + size + 2, 0xFFF2E2B0);
            }
        }
    }

    private void renderRadarList(GuiGraphics guiGraphics, List<RadarContact> contacts) {
        if (contacts.isEmpty()) {
            guiGraphics.drawString(this.font, Component.literal("No contacts in this zoom layer"), RADAR_LIST_X, RADAR_LIST_Y, 0x908C83, false);
            return;
        }
        for (int index = 0; index < Math.min(6, contacts.size()); index++) {
            RadarContact contact = contacts.get(index);
            int rowY = RADAR_LIST_Y + index * RADAR_LIST_ROW_H;
            if (index == this.selectedRadarIndex) {
                guiGraphics.fill(RADAR_LIST_X - 2, rowY - 1, BOTTOM_PANEL_X + BOTTOM_PANEL_W - 6, rowY + 8, 0x445B4F38);
            }
            this.drawScaledString(guiGraphics,
                    "[" + contact.relation().shortLabel + "] " + this.trimToWidth(contact.name(), 50) + " " + contact.distance() + "m",
                    RADAR_LIST_X, rowY, contact.relation().color, 0.75F);
        }
        if (contacts.size() > 6) {
            guiGraphics.drawString(this.font, Component.literal("+" + (contacts.size() - 6) + " more"), RADAR_LIST_X, RADAR_LIST_Y + 56, 0x908C83, false);
        }
    }

    private boolean handleBookClick(int localX, int localY) {
        for (int chapter = 0; chapter < BOOK_CHAPTER_COUNT; chapter++) {
            int rowY = LEFT_PANEL_Y + 16 + chapter * 10;
            if (inside(localX, localY, LEFT_PANEL_X + 2, rowY, LEFT_PANEL_W - 4, 10)) {
                this.bookChapter = chapter;
                this.bookPage = 0;
                return true;
            }
        }
        return false;
    }

    private boolean handleRadarClick(int localX, int localY) {
        @Nullable TeitokuData data = this.getClientTeitokuData();
        @Nullable Player player = this.minecraft != null ? this.minecraft.player : null;
        @Nullable TeamData ownTeam = data == null ? null : TeitokuHelper.getClientTeamData().get(data.getPlayerUid());
        List<RadarContact> contacts = this.filterRadarContactsByZoom(this.collectRadarContacts(player, ownTeam, data));
        if (inside(localX, localY, RADAR_MAP_X, RADAR_MAP_Y, RADAR_MAP_W, RADAR_MAP_H)) {
            int hit = this.findRadarContactAtMap(localX, localY, contacts);
            if (hit >= 0) {
                this.selectedRadarIndex = hit;
                return true;
            }
        }
        for (int index = 0; index < Math.min(6, contacts.size()); index++) {
            int rowY = RADAR_LIST_Y + index * RADAR_LIST_ROW_H;
            if (inside(localX, localY, RADAR_LIST_X - 2, rowY - 1, 102, 9)) {
                this.selectedRadarIndex = index;
                return true;
            }
        }
        return false;
    }

    private int findRadarContactAtMap(int localX, int localY, List<RadarContact> contacts) {
        double radius = RADAR_ZOOMS[this.radarZoomIndex];
        int bestIndex = -1;
        double bestDistance = 25.0D;
        for (int index = 0; index < contacts.size(); index++) {
            RadarContact contact = contacts.get(index);
            int dotX = RADAR_MAP_X + RADAR_MAP_W / 2 + Mth.floor((float) ((contact.relX() / radius) * ((RADAR_MAP_W / 2) - 4)));
            int dotY = RADAR_MAP_Y + RADAR_MAP_H / 2 + Mth.floor((float) ((contact.relZ() / radius) * ((RADAR_MAP_H / 2) - 4)));
            double distance = (localX - dotX) * (double) (localX - dotX) + (localY - dotY) * (double) (localY - dotY);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = index;
            }
        }
        return bestIndex;
    }

    private void handlePrevButton() {
        if (this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT) {
            if (this.bookPage > 0) {
                this.bookPage--;
            } else {
                this.bookChapter = (this.bookChapter + BOOK_CHAPTER_COUNT - 1) % BOOK_CHAPTER_COUNT;
                this.bookPage = this.getBookPageCount(this.bookChapter) - 1;
            }
        } else {
            this.radarZoomIndex = (this.radarZoomIndex + 1) % RADAR_ZOOMS.length;
            this.selectedRadarIndex = -1;
        }
    }

    private void handleNextButton() {
        if (this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT) {
            this.bookPage++;
            if (this.bookPage >= this.getBookPageCount(this.bookChapter)) {
                this.bookChapter = (this.bookChapter + 1) % BOOK_CHAPTER_COUNT;
                this.bookPage = 0;
            }
        } else {
            this.radarZoomIndex = (this.radarZoomIndex + RADAR_ZOOMS.length - 1) % RADAR_ZOOMS.length;
            this.selectedRadarIndex = -1;
        }
    }

    private void renderHoverTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int localX = mouseX - this.leftPos;
        int localY = mouseY - this.topPos;
        if (inside(localX, localY, PREV_BUTTON_X, TOP_BUTTON_Y, BUTTON_W, BUTTON_H)) {
            guiGraphics.renderTooltip(this.font,
                    this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT ? Component.literal("Previous page") : Component.literal("Wider zoom layer"),
                    mouseX, mouseY);
            return;
        }
        if (inside(localX, localY, NEXT_BUTTON_X, TOP_BUTTON_Y, BUTTON_W, BUTTON_H)) {
            guiGraphics.renderTooltip(this.font,
                    this.menu.getVariant() == DeskReferenceMenu.BOOK_VARIANT ? Component.literal("Next page") : Component.literal("Closer zoom layer"),
                    mouseX, mouseY);
        }
    }

    private String getBookHeaderLabel() {
        return "Logbook  " + (this.bookChapter + 1) + "." + (this.bookPage + 1) + "  " + this.getBookChapterLabel(this.bookChapter);
    }

    private String getBookChapterLabel(int chapter) {
        return switch (chapter) {
            case 0 -> "Admiralty";
            case 1 -> "Fleet";
            case 2 -> "Diplomacy";
            case 3 -> "Logistics";
            case 4 -> "Morph";
            default -> "Appendix";
        };
    }

    private int getBookPageCount(int chapter) {
        return switch (chapter) {
            case 0, 1, 2 -> 2;
            default -> 1;
        };
    }

    private String joinStrings(List<String> values, int limit) {
        if (values == null || values.isEmpty()) {
            return "-";
        }
        return values.stream().limit(limit).reduce((a, b) -> a + ", " + b).orElse("-");
    }

    private String joinInts(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "-";
        }
        return values.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("-");
    }

    private String getOwnTeamName(@Nullable TeamData ownTeam) {
        if (ownTeam == null || ownTeam.getTeamName() == null || ownTeam.getTeamName().isBlank()) {
            return "-";
        }
        return ownTeam.getTeamName();
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private List<String> getWorldBlockedClasses() {
        if (this.minecraft == null || this.minecraft.getSingleplayerServer() == null || this.minecraft.player == null) {
            return List.of();
        }

        var serverLevel = this.minecraft.getSingleplayerServer().getLevel(this.minecraft.player.level().dimension());
        if (serverLevel == null) {
            return List.of();
        }

        return List.copyOf(WorldCombatRulesSavedData.get(serverLevel).getAllUnattackableClasses());
    }

    private record BookPageData(String title, List<String> lines, List<String> notes) {
    }

    private record RadarContact(String name, int distance, String healthText, Relation relation,
                                ContactKind kind, int x, int y, int z, double relX, double relZ) {
    }

    private enum ContactKind {
        SHIP("Ship"),
        ITEM("Item"),
        ROUTE("Route"),
        FACILITY("Facility");

        private final String label;

        ContactKind(String label) {
            this.label = label;
        }
    }

    private enum Relation {
        OWN("OWN", 0xFF8FE3B3),
        ALLY("ALLY", 0xFF8FD5E8),
        HOSTILE("HOST", 0xFFF28C8C),
        NEUTRAL("NEUT", 0xFFD7D7D7),
        ITEM("ITEM", 0xFFE1D1B4);

        private final String shortLabel;
        private final int color;

        Relation(String shortLabel, int color) {
            this.shortLabel = shortLabel;
            this.color = color;
        }
    }
}
