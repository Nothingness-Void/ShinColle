package com.lulan.shincolle.client.screen;

import com.lulan.shincolle.blockentity.DeskBlockEntity;
import com.lulan.shincolle.client.model.legacy.compat.BasicEntityShip;
import com.lulan.shincolle.client.model.legacy.compat.ID;
import com.lulan.shincolle.entity.mount.LegacyShipMountEntity;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.menu.DeskMenu;
import com.lulan.shincolle.menu.LegacyDeskBookData;
import com.lulan.shincolle.menu.LegacyShipInventoryCatalog;
import com.lulan.shincolle.network.GameplayCommandType;
import com.lulan.shincolle.network.ModNetwork;
import com.lulan.shincolle.network.ServerboundGameplayCommandPacket;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

abstract class DeskScreenSupport extends AbstractContainerScreen<DeskMenu> {
    protected final Player player;
    protected final DeskCapability capa = new DeskCapability();
    protected GuiGraphics graphics;
    protected int guiLeft, guiTop;
    protected final LegacyFont fontRendererObj = new LegacyFont();
    protected final LegacyGl GlStateManager = new LegacyGl();
    private ResourceLocation boundTexture;
    protected static final int[] POW2 = java.util.stream.IntStream.range(0, 32).map(i -> 1 << i).toArray();

    protected DeskScreenSupport(DeskMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.player = inventory.player;
    }

    protected static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("shincolle", "textures/gui/" + name + ".png");
    }

    protected void bindTexture(ResourceLocation texture) { this.boundTexture = texture; }

    protected void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
        this.graphics.blit(this.boundTexture, x, y, u, v, w, h);
    }

    protected void drawHoveringText(List<String> lines, int x, int y, LegacyFont ignored) {
        if (!lines.isEmpty()) this.graphics.renderComponentTooltip(this.font,
                lines.stream().map(Component::literal).map(c -> (Component)c).toList(), x, y);
    }

    protected void renderToolTip(ItemStack item, int x, int y) {
        this.graphics.renderTooltip(this.font, item, x, y);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}

    protected void requestRadar() { send(GameplayCommandType.REQUEST_DESK_RADAR, "", 0); }

    protected void send(GameplayCommandType command, String key, Object value) {
        CompoundTag payload = new CompoundTag();
        if (!key.isEmpty()) {
            if (value instanceof Number number) payload.putInt(key, number.intValue());
            else payload.putString(key, String.valueOf(value));
        }
        ModNetwork.sendToServer(new ServerboundGameplayCommandPacket(command, payload));
    }

    protected void syncDesk(int function, int zoom, int chapter, int page) {
        CompoundTag tag = new CompoundTag();
        var pos = this.menu.getDeskPos();
        tag.putInt("X", pos.getX()); tag.putInt("Y", pos.getY()); tag.putInt("Z", pos.getZ());
        tag.putInt("Mode", function); tag.putInt("RadarZoom", zoom);
        tag.putInt("BookChapter", chapter); tag.putInt("BookPage", page);
        if (this.player.level().getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            desk.syncFunc(function, zoom, chapter, page);
        }
        ModNetwork.sendToServer(new ServerboundGameplayCommandPacket(GameplayCommandType.SYNC_DESK_FUNC, tag));
    }

    protected double[] itemCoordinates() {
        return TeitokuHelper.getClientDeskRadarContacts().stream().filter(c -> c.kind().equals("ITEM"))
                .flatMapToDouble(c -> java.util.stream.DoubleStream.of(c.x(), c.y(), c.z())).toArray();
    }

    protected Preview preview(int chapter, int page) {
        List<Integer> entries = chapter == 4 ? LegacyDeskBookData.ShipBookList
                : chapter == 5 ? LegacyDeskBookData.EnemyBookList : List.of();
        if (page <= 0 || page > entries.size()) return null;
        int shipClass = entries.get(page - 1);
        boolean collected = TeitokuHelper.get(this.player).map(d -> d.getCollectedShips().contains(shipClass)).orElse(false);
        if (!collected) return null;
        var spec = ShipEntitySpecs.findByLegacyClassId(shipClass);
        if (spec == null) return null;
        var ship = ModEntityTypes.LEGACY_SHIP.get().create(this.player.level());
        if (ship == null) return null;
        ship.applySpawnSpec(spec, null);
        ship.setGrudge(Math.max(1, ship.getGrudge()));
        return new Preview(ship);
    }

    protected Entity createTarget(String name) {
        ResourceLocation key = ResourceLocation.tryParse(name);
        if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            return BuiltInRegistries.ENTITY_TYPE.get(key).create(this.player.level());
        }
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            Entity entity;
            try { entity = type.create(this.player.level()); }
            catch (RuntimeException exception) { continue; }
            if (entity == null) continue;
            String simple = entity.getClass().getSimpleName();
            if (name.equals(simple) || name.equals(entity.getClass().getName()) || name.equals("Entity" + simple)) return entity;
        }
        return null;
    }

    protected void renderModel(Entity entity, float x, float y, float scale, float pitch, float yaw,
                               float partialTick, PreviewMount mount) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var pose = this.graphics.pose();
        this.graphics.flush();
        pose.pushPose();
        pose.translate(x, y, 50F);
        pose.scale(-scale, scale, scale);
        pose.mulPose(Axis.ZP.rotationDegrees(180F));
        Lighting.setupForEntityInInventory();
        if (entity instanceof LegacyShipEntity) pose.translate(0, 0.7F, 0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.translate(0, com.lulan.shincolle.entity.LegacyRiding.offset(entity) - (entity instanceof LegacyShipEntity ? 0.7F : 0), 0);
        Quaternionf camera = new Quaternionf(dispatcher.cameraOrientation());
        dispatcher.overrideCameraOrientation(Axis.YP.rotationDegrees(180F));
        dispatcher.setRenderShadow(false);
        try {
            if (mount != null) {
                var profile = LegacyShipBehaviorCatalog.shipMountProfileFor(mount.host.ship.getSpec());
                pose.pushPose();
                pose.translate(profile.hostX(), profile.hostY() + com.lulan.shincolle.entity.LegacyRiding.offset(entity), profile.hostZ());
                renderEntity(entity, partialTick);
                pose.popPose();
                renderEntity(mount.entity, partialTick);
            } else renderEntity(entity, partialTick);
            this.graphics.flush();
        } finally {
            dispatcher.setRenderShadow(true);
            dispatcher.overrideCameraOrientation(camera);
            pose.popPose();
            Lighting.setupFor3DItems();
        }
    }

    private void renderEntity(Entity entity, float partialTick) {
        RenderSystem.runAsFancy(() -> Minecraft.getInstance().getEntityRenderDispatcher().render(
                entity, 0D, 0D, 0D, 0F, partialTick, this.graphics.pose(), this.graphics.bufferSource(), LightTexture.FULL_BRIGHT));
    }

    protected static int moraleLevel(int morale) {
        return morale > 5100 ? 0 : morale > 3900 ? 1 : morale > 2100 ? 2 : morale > 900 ? 3 : 4;
    }

    protected final class LegacyFont {
        int getStringWidth(String text) { return font.width(text == null ? "" : text); }
        void drawString(String text, int x, int y, int color) {
            if (text != null) graphics.drawString(font, text, x, y, color, false);
        }
        void drawStringWithShadow(String text, int x, int y, int color) {
            if (text != null) graphics.drawString(font, text, x, y, color, true);
        }
        void drawSplitString(String text, int x, int y, int width, int color) {
            if (text != null) graphics.drawWordWrap(font, Component.literal(text), x, y, width, color);
        }
        int splitStringWidth(String text, int width) { return font.wordWrapHeight(text, width); }
    }

    protected final class LegacyGl {
        void pushMatrix() { graphics.pose().pushPose(); }
        void popMatrix() { graphics.pose().popPose(); }
        void scale(float x, float y, float z) { graphics.pose().scale(x, y, z); }
        void color(float r, float g, float b, float a) { graphics.setColor(r, g, b, a); }
        void enableBlend() { graphics.flush(); RenderSystem.enableBlend(); }
        void disableBlend() { graphics.flush(); RenderSystem.disableBlend(); }
        void blendFunc(int source, int destination) { RenderSystem.defaultBlendFunc(); }
    }

    protected final class DeskEditBox extends EditBox {
        DeskEditBox(int x, int y, int width, int height) {
            super(DeskScreenSupport.this.font, x, y, width, height, Component.empty());
            setTextColor(-1); setTextColorUneditable(-1); setMaxLength(64); setFocused(false); visible = false;
        }
        String getText() { return getValue(); }
    }

    protected final class DeskCapability {
        public boolean hasTeam() { return menu.hasOwnTeam(); }
        public int getPlayerUID() { return menu.getOwnTeamId(); }
        public int getPlayerTeamCooldown() { return menu.getTeamCooldown(); }
        public int getPlayerTeamCooldownInSec() { return (int)(menu.getTeamCooldown() * 0.05F); }
        public List<TeamData> getPlayerTeamDataList() { return menu.getKnownTeams(); }
        public Map<Integer, TeamData> getPlayerTeamDataMap() { return TeitokuHelper.getClientTeamData(); }
        public TeamData getPlayerTeamData(int id) { return getPlayerTeamDataMap().get(id); }
        public List<Integer> getPlayerTeamAllyList() {
            TeamData own = getPlayerTeamData(getPlayerUID()); return own == null ? List.of() : own.getAllies();
        }
        public List<Integer> getPlayerTeamBannedList() {
            TeamData own = getPlayerTeamData(getPlayerUID()); return own == null ? List.of() : own.getBanned();
        }
        public boolean isTeamAlly(int id) { return getPlayerTeamAllyList().contains(id); }
        public boolean isTeamBanned(int id) { return getPlayerTeamBannedList().contains(id); }
        public List<Integer> getShipEIDList() {
            List<Integer> ships = new ArrayList<>();
            var level = Minecraft.getInstance().level;
            if (level != null) for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof LegacyShipEntity) ships.add(entity.getId());
            }
            return ships;
        }
    }

    protected static final class RadarEntity {
        public Entity ship;
        public String name;
        public double pixelx, pixely, pixelz;
        public int posX, posY, posZ;
        RadarEntity(Entity ship) { this.ship = ship; this.name = ship.getName().getString(); }
        RadarEntity(double x, double y, double z, int posX, int posY, int posZ) {
            pixelx = x; pixely = y; pixelz = z; this.posX = posX; this.posY = posY; this.posZ = posZ;
        }
    }

    protected static final class Preview extends BasicEntityShip {
        Preview(LegacyShipEntity ship) { super(ship); }
        public int shipType() { return LegacyShipInventoryCatalog.forShip(ship.getSpec()).shipType(); }
        public int modelButtons() { return LegacyShipInventoryCatalog.forShip(ship.getSpec()).modelButtons(); }
        public void setSitting(boolean sitting) { ship.setOrderedToSit(sitting); }
        public void setSprinting(boolean sprinting) { ship.setSprinting(sprinting); }
        public void setSneaking(boolean sneaking) { ship.setShiftKeyDown(sneaking); }
        @Override public void setStateFlag(int id, boolean value) {
            if (id == ID.F.NoFuel) ship.setGrudge(value ? 0 : Math.max(1, ship.getGrudge()));
            else super.setStateFlag(id, value);
        }
        public boolean hasShipMounts() { return LegacyShipBehaviorCatalog.shipMountProfileFor(ship.getSpec()) != null; }
        public PreviewMount mount() {
            ship.stopRiding();
            if ((getStateEmotion(ID.S.State) & 1) == 0 || getStateFlag(ID.F.NoFuel)) return null;
            LegacyShipMountEntity mount = LegacyShipMountEntity.create(ship.level(), ship);
            if (mount == null) return null;
            ship.startRiding(mount, true);
            return new PreviewMount(this, mount);
        }
        public void advancePreview() {
            ship.tickCount++;
            if (getAttackTick() > 0) setAttackTick(getAttackTick() - 1);
            if (isSprinting()) {
                double oldX = ship.getX(), oldZ = ship.getZ();
                ship.travel(new net.minecraft.world.phys.Vec3(1F, 0F, 0F));
                float distance = (float)Math.sqrt(Math.pow(ship.getX()-oldX, 2) + Math.pow(ship.getZ()-oldZ, 2));
                ship.walkAnimation.update(Math.min(1F, distance * 4F), 0.4F);
            } else ship.walkAnimation.setSpeed(0F);
        }
    }

    protected static final class PreviewMount {
        final Preview host;
        final LegacyShipMountEntity entity;
        private int attackTick;
        private final int[] emotion = new int[8];
        private final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        PreviewMount(Preview host, LegacyShipMountEntity entity) { this.host = host; this.entity = entity; }
        public void setSprinting(boolean sprinting) { entity.setSprinting(sprinting); }
        public void setAttackTick(int ticks) { this.attackTick = ticks; }
        public void setStateEmotion(int id, int value, boolean sync) { this.emotion[id] = value; }
        public net.minecraft.util.RandomSource getRNG() { return this.random; }
    }

    protected static final class CalcHelper {
        public static List<String> stringConvNewlineToList(String text) {
            return Arrays.asList(text.split("<BR>|<BR/>|<br>|<br/>"));
        }
    }
}
