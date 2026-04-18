package com.lulan.shincolle.menu;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.LegacyShipStatTables;
import com.lulan.shincolle.entity.ship.ShipEquipmentBehaviorState;
import com.lulan.shincolle.entity.ship.LegacyShipStats;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.ShipEquipmentProfile;
import com.lulan.shincolle.item.equipment.LegacyEquipmentItem;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModMenus;
import com.lulan.shincolle.network.ShipCommandService;
import com.lulan.shincolle.network.ServerboundShipCommandPacket;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.TeitokuData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ShipInventoryMenu extends AbstractContainerMenu {

    public static final int BUTTON_TOGGLE_MODE = 0;
    private static final int DEFAULT_AI_FOLLOW_RANGE = 14;
    private static final String HEALTH_TAG = "Health";
    private static final String ORDERED_TO_SIT_TAG = "OrderedToSit";
    private static final String SHIP_LEVEL_TAG = "ShipLevel";
    private static final String SHIP_MORALE_TAG = "ShipMorale";
    private static final String SHIP_MARRIED_TAG = "ShipMarried";
    private static final String MODERN_HEALTH_TAG = "ModernHealth";
    private static final String MODERN_ATTACK_TAG = "ModernAttack";
    private static final String MODERN_SPEED_TAG = "ModernSpeed";
    private static final String MODERN_RANGE_TAG = "ModernRange";
    private static final String ROUTE_ENERGY_TAG = "RouteEnergy";
    private static final String AI_AUTO_TARGET_TAG = "AiAutoTarget";
    private static final String AI_ALLOW_PVP_TAG = "AiAllowPvp";
    private static final String AI_AUTO_SUPPLY_TAG = "AiAutoSupply";
    private static final String AI_FOLLOW_RANGE_TAG = "AiFollowRange";
    private static final String AI_ROUTE_STAY_TAG = "AiRouteStay";

    private static final int EQUIPMENT_SLOT_START = 0;
    private static final int CARGO_SLOT_START = EQUIPMENT_SLOT_START + LegacyShipEntity.EQUIPMENT_SLOT_COUNT;
    private static final int PLAYER_SLOT_START = LegacyShipEntity.SHIP_SLOT_COUNT;
    private static final int HOTBAR_SLOT_START = PLAYER_SLOT_START + 27;
    private static final int SLOT_END = HOTBAR_SLOT_START + 9;

    private final Inventory playerInventory;
    private final int shipId;
    private final int shipUid;
    private final Container shipInventory;

    private @Nullable LegacyShipEntity ship;

    public ShipInventoryMenu(int containerId, Inventory playerInventory, int shipId) {
        this(containerId, playerInventory, shipId, resolveInitialShipUid(playerInventory, shipId));
    }

    public ShipInventoryMenu(int containerId, Inventory playerInventory, int shipId, int shipUid) {
        super(ModMenus.SHIP_INVENTORY.get(), containerId);
        this.playerInventory = playerInventory;
        this.shipId = shipId;
        this.shipUid = shipUid > 0 ? shipUid : resolveInitialShipUid(playerInventory, shipId);
        this.ship = resolveShip(playerInventory, shipId, this.shipUid);
        this.shipInventory = this.ship != null ? this.ship.getShipInventory() : new SimpleContainer(LegacyShipEntity.SHIP_SLOT_COUNT);

        checkContainerSize(this.shipInventory, LegacyShipEntity.SHIP_SLOT_COUNT);
        this.shipInventory.startOpen(playerInventory.player);

        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            this.addSlot(new ShipEquipmentSlot(this.shipInventory, slot, 144, 18 + slot * 18, this::isValidShipEquipment));
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = CARGO_SLOT_START + row * 3 + col;
                this.addSlot(new Slot(this.shipInventory, slot, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 132 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 190));
        }
    }

    public static ShipInventoryMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        return new ShipInventoryMenu(containerId, inventory, buffer.readVarInt(), buffer.readVarInt());
    }

    public @Nullable LegacyShipEntity getShip() {
        if (this.ship == null || !this.ship.isAlive()) {
            this.ship = resolveShip(this.playerInventory, this.shipId, this.shipUid);
        }

        return this.ship;
    }

    public int getShipId() {
        return this.shipId;
    }

    public int getShipUid() {
        LegacyShipEntity ship = this.getShip();
        return ship != null && ship.getShipUid() > 0 ? ship.getShipUid() : this.shipUid;
    }

    public boolean canEdit() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.isOwnedBy(this.playerInventory.player) && !ship.isHostileVariant();
        }

        ShipWorldCacheEntry cached = this.getShipCacheEntry();
        ShipEntitySpec spec = this.resolveCachedSpec(cached);
        return cached != null && spec != null && !spec.hostile() && !cached.dead() && this.isOwnedByViewer(cached);
    }

    public boolean isValidShipEquipment(ItemStack stack) {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.canEquip(stack);
        }

        ShipEntitySpec spec = this.resolveCachedSpec(this.getShipCacheEntry());
        if (spec == null) {
            return stack.getItem() instanceof LegacyEquipmentItem;
        }

        return ShipEquipmentProfile.canEquip(spec.archetype(), stack);
    }

    public Component getOwnerLabel() {
        LegacyShipEntity ship = this.getShip();
        String ownerName;
        if (ship != null) {
            ownerName = ship.getOwnerName();
        } else {
            ShipWorldCacheEntry cached = this.getShipCacheEntry();
            ownerName = cached != null ? cached.ownerName() : "";
        }

        if (ownerName.isBlank()) {
            return Component.translatable("gui.shincolle.waypoint.owner.unassigned");
        }

        return Component.literal(ownerName);
    }

    public Component getModeLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.getEscortModeLabel();
        }

        CompoundTag tag = this.getCachedEntityTag();
        return Component.translatable(this.getCachedBoolean(tag, ORDERED_TO_SIT_TAG, false)
                ? "gui.shincolle.entity.mode.standby"
                : "gui.shincolle.entity.mode.follow");
    }

    public float getHealthRatio() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null && ship.getMaxHealth() > 0F) {
            return ship.getHealth() / ship.getMaxHealth();
        }

        LegacyShipStats cachedStats = this.getCachedLegacyStats();
        if (cachedStats == null) {
            return 0F;
        }

        float maxHealth = Math.max(1.0F, cachedStats.get(LegacyShipStatTables.Attr.HP));
        float currentHealth = this.getCachedFloat(this.getCachedEntityTag(), HEALTH_TAG, maxHealth);
        return Math.max(0.0F, Math.min(1.0F, currentHealth / maxHealth));
    }

    public String getHealthText() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return Math.round(ship.getHealth()) + " / " + Math.round(ship.getMaxHealth());
        }

        LegacyShipStats cachedStats = this.getCachedLegacyStats();
        if (cachedStats == null) {
            return "- / -";
        }

        float maxHealth = Math.max(1.0F, cachedStats.get(LegacyShipStatTables.Attr.HP));
        float currentHealth = this.getCachedFloat(this.getCachedEntityTag(), HEALTH_TAG, maxHealth);
        return Math.round(currentHealth) + " / " + Math.round(maxHealth);
    }

    public String getAttackText() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return String.format(java.util.Locale.ROOT, "%.1f", ship.getLegacyStats().attackLight());
        }

        LegacyShipStats cachedStats = this.getCachedLegacyStats();
        return cachedStats != null ? String.format(java.util.Locale.ROOT, "%.1f", cachedStats.attackLight()) : "-";
    }

    public String getSpeedText() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return String.format(java.util.Locale.ROOT, "%.2f", ship.getLegacyStats().attackSpeed());
        }

        LegacyShipStats cachedStats = this.getCachedLegacyStats();
        return cachedStats != null ? String.format(java.util.Locale.ROOT, "%.2f", cachedStats.attackSpeed()) : "-";
    }

    public String getRangeText() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return String.format(java.util.Locale.ROOT, "%.1f", ship.getLegacyStats().attackRange());
        }

        LegacyShipStats cachedStats = this.getCachedLegacyStats();
        return cachedStats != null ? String.format(java.util.Locale.ROOT, "%.1f", cachedStats.attackRange()) : "-";
    }

    public Component getRoleLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return Component.translatable("gui.shincolle.ship_inventory.role." + ship.getSpec().archetype().name().toLowerCase(java.util.Locale.ROOT));
        }

        ShipEntitySpec spec = this.resolveCachedSpec(this.getShipCacheEntry());
        if (spec == null) {
            return Component.translatable("gui.shincolle.ship_inventory.role.unknown");
        }

        return Component.translatable("gui.shincolle.ship_inventory.role." + spec.archetype().name().toLowerCase(java.util.Locale.ROOT));
    }

    public int getShipLevel() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getShipLevel() : this.getCachedInt(this.getCachedEntityTag(), SHIP_LEVEL_TAG, 0);
    }

    public Component getMoraleLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return Component.translatable("gui.shincolle.ship_inventory.morale." + ship.getMoraleTier());
        }

        CompoundTag tag = this.getCachedEntityTag();
        if (tag.isEmpty()) {
            return Component.literal("-");
        }

        return Component.translatable("gui.shincolle.ship_inventory.morale." + this.resolveMoraleTier(this.getCachedInt(tag, SHIP_MORALE_TAG, 0)));
    }

    public String getMoraleText() {
        LegacyShipEntity ship = this.getShip();
        return ship != null ? ship.getMorale() + " / 16000"
                : this.getCachedEntityTag().isEmpty() ? "-" : this.getCachedInt(this.getCachedEntityTag(), SHIP_MORALE_TAG, 0) + " / 16000";
    }

    public Component getMarriageLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return Component.translatable(ship.isMarried()
                    ? "gui.shincolle.ship_inventory.marriage.yes"
                    : "gui.shincolle.ship_inventory.marriage.no");
        }

        return Component.translatable(this.getCachedBoolean(this.getCachedEntityTag(), SHIP_MARRIED_TAG, false)
                ? "gui.shincolle.ship_inventory.marriage.yes"
                : "gui.shincolle.ship_inventory.marriage.no");
    }

    public int getModernizationCount() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.getModernizationDisplayCount();
        }

        CompoundTag tag = this.getCachedEntityTag();
        return this.getCachedInt(tag, MODERN_HEALTH_TAG, 0)
                + this.getCachedInt(tag, MODERN_ATTACK_TAG, 0)
                + this.getCachedInt(tag, MODERN_SPEED_TAG, 0)
                + this.getCachedInt(tag, MODERN_RANGE_TAG, 0);
    }

    public int getRescueCount() {
        int count = 0;

        for (int slot = 0; slot < this.shipInventory.getContainerSize(); slot++) {
            ItemStack stack = this.shipInventory.getItem(slot);
            if (stack.is(ModItems.REPAIRGODDESS.get())) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public ShipEquipmentBehaviorState getBehaviorState() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.getEquipmentBehaviorState();
        }

        ShipEntitySpec spec = this.resolveCachedSpec(this.getShipCacheEntry());
        return spec == null ? ShipEquipmentBehaviorState.EMPTY : ShipEquipmentBehaviorState.fromInventory(this.shipInventory, spec.archetype());
    }

    public Component getSensorBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.sensor",
                behaviorState.airRadarLevel(),
                behaviorState.surfaceRadarLevel(),
                behaviorState.sonarLevel(),
                behaviorState.fcsLevel());
    }

    public Component getUtilityBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.utility",
                behaviorState.flareLevel(),
                behaviorState.searchlightLevel(),
                behaviorState.catapultLevel(),
                behaviorState.turbineLevel(),
                behaviorState.transportTier());
    }

    public Component getRouteBehaviorLabel() {
        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        return Component.translatable("gui.shincolle.ship_inventory.behavior.route",
                Component.translatable(behaviorState.autonomousRoute()
                        ? "gui.shincolle.ship_inventory.behavior.on"
                        : "gui.shincolle.ship_inventory.behavior.off"));
    }

    public Component getTorpedoBehaviorLabel() {
        return Component.translatable("gui.shincolle.ship_inventory.behavior.torpedo",
                this.getBehaviorState().torpedoSpeedLevel());
    }

    public Component getMarriageBonusLabel() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return Component.translatable(ship.isMarried()
                    ? "gui.shincolle.ship_inventory.marriage.bonus.active"
                    : "gui.shincolle.ship_inventory.marriage.bonus.none");
        }

        if (!this.getCachedBoolean(this.getCachedEntityTag(), SHIP_MARRIED_TAG, false)) {
            return Component.translatable("gui.shincolle.ship_inventory.marriage.bonus.none");
        }

        return Component.translatable("gui.shincolle.ship_inventory.marriage.bonus.active");
    }

    public int getAiFlags() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.getAiFlagsBitmask();
        }

        CompoundTag tag = this.getCachedEntityTag();
        int flags = 0;
        if (this.getCachedBoolean(tag, AI_AUTO_TARGET_TAG, true)) {
            flags |= com.lulan.shincolle.network.GameplayCommandHandler.AI_FLAG_AUTO_TARGET;
        }
        if (this.getCachedBoolean(tag, AI_ALLOW_PVP_TAG, false)) {
            flags |= com.lulan.shincolle.network.GameplayCommandHandler.AI_FLAG_ALLOW_PVP;
        }
        if (this.getCachedBoolean(tag, AI_AUTO_SUPPLY_TAG, true)) {
            flags |= com.lulan.shincolle.network.GameplayCommandHandler.AI_FLAG_AUTO_SUPPLY;
        }
        if (this.getCachedBoolean(tag, AI_ROUTE_STAY_TAG, true)) {
            flags |= com.lulan.shincolle.network.GameplayCommandHandler.AI_FLAG_ROUTE_STAY;
        }
        return flags;
    }

    public int getAiFollowRange() {
        LegacyShipEntity ship = this.getShip();
        return ship != null
                ? ship.getAiFollowRange()
                : this.getCachedInt(this.getCachedEntityTag(), AI_FOLLOW_RANGE_TAG, DEFAULT_AI_FOLLOW_RANGE);
    }

    public String getRouteEnergyText() {
        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.getRouteEnergyText();
        }

        ShipEquipmentBehaviorState behaviorState = this.getBehaviorState();
        int routeEnergy = this.getCachedInt(this.getCachedEntityTag(), ROUTE_ENERGY_TAG, 0);
        int routeCapacity = (8 + behaviorState.transportTier() * 8) * 400;
        return routeEnergy + " / " + routeCapacity;
    }

    public int getCurrentTeamId() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getCurrentTeamId)
                .orElse(0);
    }

    public int getCurrentFormationId() {
        return TeitokuHelper.get(this.playerInventory.player)
                .map(TeitokuData::getFormationId)
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    public Component getCurrentTeamLabel() {
        return Component.translatable("gui.shincolle.ship_inventory.team", this.getCurrentTeamId() + 1);
    }

    public Component getCurrentTeamShortLabel() {
        return Component.literal("T" + (this.getCurrentTeamId() + 1));
    }

    public Component getCurrentFormationLabel() {
        return Component.translatable("gui.shincolle.formation.format" + this.getCurrentFormationId());
    }

    public Component getCurrentFormationShortLabel() {
        return Component.literal(switch (this.getCurrentFormationId()) {
            case 1 -> "Ahead";
            case 2 -> "Double";
            case 3 -> "Diamond";
            case 4 -> "Echelon";
            case 5 -> "Abreast";
            default -> "None";
        });
    }

    public int getCargoUsedSlots() {
        int used = 0;

        for (int slot = CARGO_SLOT_START; slot < LegacyShipEntity.SHIP_SLOT_COUNT; slot++) {
            if (!this.shipInventory.getItem(slot).isEmpty()) {
                used++;
            }
        }

        return used;
    }

    public int getEquipmentUsedSlots() {
        int used = 0;

        for (int slot = 0; slot < LegacyShipEntity.EQUIPMENT_SLOT_COUNT; slot++) {
            if (!this.shipInventory.getItem(slot).isEmpty()) {
                used++;
            }
        }

        return used;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_MODE) {
            return false;
        }

        ServerboundShipCommandPacket packet = ServerboundShipCommandPacket.toggleSit(0, this.shipId, this.getShipUid());
        if (player instanceof ServerPlayer serverPlayer) {
            return ShipCommandService.handle(serverPlayer, packet);
        }
        return ShipCommandService.handleForTesting(player, packet);
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level() instanceof ServerLevel) {
            LegacyShipEntity ship = this.getShip();
            return ship != null && ship.isAlive() && ship.isOwnedBy(player);
        }

        LegacyShipEntity ship = this.getShip();
        if (ship != null) {
            return ship.isAlive() && ship.isOwnedBy(player);
        }

        ShipWorldCacheEntry cached = this.getShipCacheEntry();
        return cached != null && !cached.dead() && this.isOwnedByViewer(cached);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();
        boolean moved;

        if (index < PLAYER_SLOT_START) {
            moved = this.moveItemStackTo(stackInSlot, PLAYER_SLOT_START, SLOT_END, true);
        } else {
            moved = false;

            LegacyShipEntity ship = this.getShip();
            if (ship != null && ship.canEquip(stackInSlot)) {
                moved = this.moveItemStackTo(stackInSlot, EQUIPMENT_SLOT_START, CARGO_SLOT_START, false);
            }

            if (!moved) {
                moved = this.moveItemStackTo(stackInSlot, CARGO_SLOT_START, PLAYER_SLOT_START, false);
            }

            if (!moved && index < HOTBAR_SLOT_START) {
                moved = this.moveItemStackTo(stackInSlot, HOTBAR_SLOT_START, SLOT_END, false);
            }

            if (!moved && index >= HOTBAR_SLOT_START) {
                moved = this.moveItemStackTo(stackInSlot, PLAYER_SLOT_START, HOTBAR_SLOT_START, false);
            }
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stackInSlot);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.shipInventory.stopOpen(player);
    }

    private @Nullable ShipWorldCacheEntry getShipCacheEntry() {
        int resolvedShipUid = this.getShipUid();
        if (resolvedShipUid <= 0) {
            return null;
        }

        if (this.playerInventory.player.level() instanceof ServerLevel serverLevel) {
            return ShipCacheSavedData.get(serverLevel).getShip(resolvedShipUid);
        }

        return TeitokuHelper.getClientShipCacheEntry(resolvedShipUid);
    }

    private @Nullable ShipEntitySpec resolveCachedSpec(@Nullable ShipWorldCacheEntry entry) {
        if (entry == null) {
            return null;
        }

        ShipEntitySpec spec = ShipEntitySpecs.findByLegacyClassId(entry.legacyClassId());
        return spec != null ? spec : ShipEntitySpecs.findByEggMeta(entry.variantEggMeta());
    }

    private LegacyShipStats getCachedLegacyStats() {
        CompoundTag tag = this.getCachedEntityTag();
        ShipEntitySpec spec = this.resolveCachedSpec(this.getShipCacheEntry());
        if (tag.isEmpty() || spec == null) {
            return null;
        }

        return LegacyShipStats.create(
                spec.legacyClassId(),
                spec.archetype(),
                spec.hostile(),
                Math.max(1, this.getCachedInt(tag, SHIP_LEVEL_TAG, 1)),
                this.getCachedInt(tag, SHIP_MORALE_TAG, 0),
                this.getCachedInt(tag, MODERN_HEALTH_TAG, 0),
                this.getCachedInt(tag, MODERN_ATTACK_TAG, 0),
                this.getCachedInt(tag, MODERN_SPEED_TAG, 0),
                this.getCachedInt(tag, MODERN_RANGE_TAG, 0),
                this.getCachedBoolean(tag, SHIP_MARRIED_TAG, false),
                this.resolveCachedFormationId(),
                ShipEquipmentProfile.fromInventory(this.shipInventory, spec.archetype()),
                java.util.List.of());
    }

    private CompoundTag getCachedEntityTag() {
        ShipWorldCacheEntry cached = this.getShipCacheEntry();
        return cached != null ? cached.entityTag() : new CompoundTag();
    }

    private int resolveCachedFormationId() {
        if (this.getShipUid() <= 0) {
            return TeitokuData.DEFAULT_FORMATION_ID;
        }

        return TeitokuHelper.get(this.playerInventory.player)
                .map(data -> {
                    int teamId = data.findTeamIdByShipUid(this.getShipUid());
                    if (teamId < 0 || data.countShipsInTeam(teamId) <= 4) {
                        return TeitokuData.DEFAULT_FORMATION_ID;
                    }
                    return LegacyShipStatTables.normalizeFormationId(data.getFormationId(teamId));
                })
                .orElse(TeitokuData.DEFAULT_FORMATION_ID);
    }

    private boolean isOwnedByViewer(ShipWorldCacheEntry entry) {
        int playerUid = TeitokuHelper.getPlayerUid(this.playerInventory.player);
        if (entry.ownerUid() > 0 && playerUid > 0) {
            return entry.ownerUid() == playerUid;
        }

        return !entry.ownerName().isBlank()
                && entry.ownerName().equals(this.playerInventory.player.getGameProfile().getName());
    }

    private int resolveMoraleTier(int morale) {
        if (morale > LegacyShipStatTables.MORALE_EXCITED_LOWER) {
            return 4;
        }
        if (morale > LegacyShipStatTables.MORALE_HAPPY_LOWER) {
            return 3;
        }
        if (morale > LegacyShipStatTables.MORALE_NORMAL_LOWER) {
            return 2;
        }
        if (morale > LegacyShipStatTables.MORALE_TIRED_LOWER) {
            return 1;
        }
        return 0;
    }

    private int getCachedInt(CompoundTag tag, String key, int defaultValue) {
        return tag.contains(key) ? tag.getInt(key) : defaultValue;
    }

    private float getCachedFloat(CompoundTag tag, String key, float defaultValue) {
        return tag.contains(key) ? tag.getFloat(key) : defaultValue;
    }

    private boolean getCachedBoolean(CompoundTag tag, String key, boolean defaultValue) {
        return tag.contains(key) ? tag.getBoolean(key) : defaultValue;
    }

    private static int resolveInitialShipUid(Inventory inventory, int shipId) {
        LegacyShipEntity ship = resolveShip(inventory, shipId, ServerboundShipCommandPacket.NO_UID);
        return ship != null ? ship.getShipUid() : ServerboundShipCommandPacket.NO_UID;
    }

    private static @Nullable LegacyShipEntity resolveShip(Inventory inventory, int shipId, int shipUid) {
        if (inventory.player.level().getEntity(shipId) instanceof LegacyShipEntity ship) {
            return ship;
        }

        return shipUid > 0 ? TeitokuHelper.findOwnedShipByUid(inventory.player, shipUid) : null;
    }
}
