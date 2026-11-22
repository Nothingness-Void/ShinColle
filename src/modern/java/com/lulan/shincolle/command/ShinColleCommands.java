package com.lulan.shincolle.command;

import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.item.LegacyEntityItemMarker;
import com.lulan.shincolle.network.CombatFxDispatcher;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.ShipCacheSavedData;
import com.lulan.shincolle.teitoku.ShipWorldCacheEntry;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Legacy 1.12.2 {@code /ship*} slash command parity registered via Brigadier.
 */
public final class ShinColleCommands {

    private static final Map<String, Integer> EMOTE_ALIASES = buildEmoteAliasMap();

    private ShinColleCommands() {
    }

    /** Legacy ShipCmd* checkPermission only accepted EntityPlayer senders. */
    private static boolean isPlayerSource(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer;
    }

    /** Legacy EntityHelper.checkOP: creative mode counts as OP, otherwise PlayerList op check. */
    public static boolean checkOp(ServerPlayer player) {
        return player.isCreative() || player.server.getPlayerList().isOp(player.getGameProfile());
    }

    private static boolean checkOp(CommandSourceStack source, boolean notifyNotOp) {
        if (source.getEntity() instanceof ServerPlayer player && checkOp(player)) {
            return true;
        }
        if (notifyNotOp) {
            source.sendFailure(Component.translatable("chat.shincolle.command.notop"));
        }
        return false;
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        registerChangeOwner(dispatcher, "shipchangeowner");
        registerChangeOwner(dispatcher, "shipch");
        registerEmotes(dispatcher, "shipemotes");
        registerEmotes(dispatcher, "em");
        registerEmotes(dispatcher, "emo");
        registerEmotes(dispatcher, "emote");
        registerEmotes(dispatcher, "emotes");
        registerShip(dispatcher);
        registerShipKill(dispatcher);
        registerShipAttrs(dispatcher);
        registerShipClearDrop(dispatcher);
        registerShipInfo(dispatcher);
        registerStopAi(dispatcher, "shipstopai");
        registerStopAi(dispatcher, "shipstop");
        registerUpdateOwnerUid(dispatcher);
    }

    private static void registerChangeOwner(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(name)
                .requires(ShinColleCommands::isPlayerSource)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(collectPlayerNames(ctx.getSource().getServer()), builder))
                        .executes(ctx -> checkOp(ctx.getSource(), true)
                                ? executeChangeOwner(ctx.getSource(), StringArgumentType.getString(ctx, "player"))
                                : 0));
        dispatcher.register(cmd);
    }

    private static void registerEmotes(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(name)
                .executes(ctx -> executeEmotes(ctx.getSource(), null))
                .then(Commands.argument("emote", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(EMOTE_ALIASES.keySet(), builder))
                        .executes(ctx -> executeEmotes(ctx.getSource(), StringArgumentType.getString(ctx, "emote"))));
        dispatcher.register(cmd);
    }

    private static void registerShip(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ship")
                .requires(ShinColleCommands::isPlayerSource)
                .then(Commands.literal("list")
                        .executes(ctx -> executeShipList(ctx.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> executeShipList(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))))
                .then(Commands.literal("get")
                        .then(Commands.argument("uid", IntegerArgumentType.integer(1))
                                .executes(ctx -> checkOp(ctx.getSource(), true)
                                        ? executeShipGet(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "uid"))
                                        : 0)))
                .then(Commands.literal("del")
                        .then(Commands.argument("uid", IntegerArgumentType.integer(1))
                                .executes(ctx -> checkOp(ctx.getSource(), true)
                                        ? executeShipDel(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "uid"))
                                        : 0))));
    }

    private static void registerShipKill(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shipkill")
                .requires(ShinColleCommands::isPlayerSource)
                .then(Commands.argument("classId", IntegerArgumentType.integer(2))
                        .executes(ctx -> checkOp(ctx.getSource(), false)
                                ? executeShipKill(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "classId"), 64)
                                : 0)
                        .then(Commands.argument("range", IntegerArgumentType.integer(1, 512))
                                .executes(ctx -> checkOp(ctx.getSource(), false)
                                        ? executeShipKill(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "classId"),
                                                IntegerArgumentType.getInteger(ctx, "range"))
                                        : 0))));
    }

    private static void registerShipAttrs(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shipattrs")
                .requires(ShinColleCommands::isPlayerSource)
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 150))
                        .executes(ctx -> checkOp(ctx.getSource(), true)
                                ? executeShipAttrs(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "level"), null)
                                : 0)
                        .then(Commands.argument("hp", IntegerArgumentType.integer(0, 100))
                                .then(Commands.argument("atk", IntegerArgumentType.integer(0, 100))
                                        .then(Commands.argument("def", IntegerArgumentType.integer(0, 100))
                                                .then(Commands.argument("spd", IntegerArgumentType.integer(0, 100))
                                                        .then(Commands.argument("mov", IntegerArgumentType.integer(0, 100))
                                                                .then(Commands.argument("hit", IntegerArgumentType.integer(0, 100))
                                                                        .executes(ctx -> !checkOp(ctx.getSource(), true) ? 0 : executeShipAttrs(ctx.getSource(),
                                                                                IntegerArgumentType.getInteger(ctx, "level"),
                                                                                new int[] {
                                                                                        IntegerArgumentType.getInteger(ctx, "hp"),
                                                                                        IntegerArgumentType.getInteger(ctx, "atk"),
                                                                                        IntegerArgumentType.getInteger(ctx, "def"),
                                                                                        IntegerArgumentType.getInteger(ctx, "spd"),
                                                                                        IntegerArgumentType.getInteger(ctx, "mov"),
                                                                                        IntegerArgumentType.getInteger(ctx, "hit")
                                                                                }))))))))));
    }

    private static void registerShipClearDrop(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shipcleardrop")
                .requires(ShinColleCommands::isPlayerSource)
                .executes(ctx -> checkOp(ctx.getSource(), false) ? executeShipClearDrop(ctx.getSource(), 128) : 0)
                .then(Commands.argument("range", IntegerArgumentType.integer(0, 512))
                        .executes(ctx -> checkOp(ctx.getSource(), false)
                                ? executeShipClearDrop(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "range"))
                                : 0)));
    }

    private static void registerShipInfo(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shipinfo")
                .executes(ctx -> executeShipInfo(ctx.getSource())));
    }

    private static void registerStopAi(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .requires(ShinColleCommands::isPlayerSource)
                .executes(ctx -> checkOp(ctx.getSource(), false) ? executeStopAi(ctx.getSource()) : 0));
    }

    private static void registerUpdateOwnerUid(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shipupdateowneruid")
                .requires(ShinColleCommands::isPlayerSource)
                .executes(ShinColleCommands::executeUpdateOwnerUidSelf)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(collectPlayerNames(ctx.getSource().getServer()), builder))
                        .executes(ctx -> checkOp(ctx.getSource(), true)
                                ? executeUpdateOwnerUidTarget(ctx.getSource(), StringArgumentType.getString(ctx, "player"))
                                : 0)));
    }

    private static int executeChangeOwner(CommandSourceStack source, String targetPlayerName) {
        ServerPlayer targetPlayer = resolvePlayerByName(source.getServer(), targetPlayerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("shipchangeowner: player not found: " + targetPlayerName));
            return 0;
        }
        int targetUid = TeitokuHelper.getPlayerUid(targetPlayer);
        if (targetUid <= 0) {
            targetUid = TeitokuSavedData.get(source.getLevel()).getOrCreateUid(targetPlayer);
        }
        if (targetUid <= 0) {
            source.sendFailure(Component.literal("shipchangeowner: invalid player UID for " + targetPlayerName));
            return 0;
        }

        LegacyShipEntity ship = raycastShip(source, 32.0D);
        if (ship == null) {
            source.sendFailure(Component.literal("shipchangeowner: no target ship within 32 blocks"));
            return 0;
        }

        ship.setOwner(targetPlayer.getUUID(), targetPlayer.getGameProfile().getName(), targetUid);
        ShipCacheSavedData.get(source.getLevel()).updateFromShip(ship, !ship.isAlive(), true);
        int finalUid = targetUid;
        source.sendSuccess(() -> Component.literal("shipchangeowner: " + ship.getName().getString()
                + " -> " + targetPlayer.getGameProfile().getName() + " (UID " + finalUid + ")"), true);
        return 1;
    }

    private static int executeEmotes(CommandSourceStack source, @Nullable String rawEmote) {
        int emoteId;
        if (rawEmote == null || rawEmote.isBlank()) {
            emoteId = source.getLevel().getRandom().nextInt(30);
        } else {
            Integer mapped = EMOTE_ALIASES.get(rawEmote.trim().toLowerCase(Locale.ROOT));
            if (mapped == null) {
                source.sendFailure(Component.literal("shipemotes: unknown emote: " + rawEmote));
                return 0;
            }
            emoteId = mapped;
        }

        Entity sender = source.getEntity();
        if (sender != null) {
            CombatFxDispatcher.sendLegacyEmotion(sender, emoteId);
        }
        int resolved = emoteId;
        source.sendSuccess(() -> Component.literal("shipemotes: #" + resolved), false);
        return 1;
    }

    private static int executeShipList(CommandSourceStack source, int requestedPage) {
        ShipCacheSavedData cache = ShipCacheSavedData.get(source.getLevel());
        List<ShipWorldCacheEntry> entries = new ArrayList<>(cache.getAllShips());
        entries.sort(Comparator.comparingInt(ShipWorldCacheEntry::shipUid));

        int perPage = Math.max(1, ShinColleConfig.shipNumPerPage());
        int total = entries.size();
        int maxPage = Math.max(1, (total + perPage - 1) / perPage);
        int page = Mth.clamp(requestedPage, 1, maxPage);
        int start = (page - 1) * perPage;
        int end = Math.min(total, start + perPage);

        source.sendSuccess(() -> Component.literal("Ship List [" + page + "/" + maxPage + "] (total " + total + ")"), false);
        for (int i = start; i < end; i++) {
            ShipWorldCacheEntry entry = entries.get(i);
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "#%d class=%d owner=%s(%d) pos=(%d,%d,%d) %s",
                    entry.shipUid(),
                    entry.legacyClassId(),
                    entry.ownerName().isBlank() ? "-" : entry.ownerName(),
                    entry.ownerUid(),
                    entry.posX(),
                    entry.posY(),
                    entry.posZ(),
                    entry.dead() ? "DEAD" : (entry.online() ? "LIVE" : "CACHED"))), false);
        }
        return total;
    }

    private static int executeShipGet(CommandSourceStack source, int uid) {
        ServerLevel level = source.getLevel();
        ShipCacheSavedData cache = ShipCacheSavedData.get(level);
        ShipWorldCacheEntry entry = cache.getShip(uid);
        if (entry == null) {
            source.sendFailure(Component.literal("ship get: unknown ship UID " + uid));
            return 0;
        }

        String dimId = level.dimension().location().toString();
        if (!dimId.equals(entry.dimensionId())) {
            source.sendFailure(Component.literal("ship get: ship UID " + uid + " is in dimension " + entry.dimensionId()));
            return 0;
        }

        Vec3 senderPos = source.getPosition();
        double dx = senderPos.x - entry.posX();
        double dy = senderPos.y - entry.posY();
        double dz = senderPos.z - entry.posZ();
        if (dx * dx + dy * dy + dz * dz >= 4096.0D) {
            source.sendFailure(Component.literal("ship get: ship UID " + uid + " is more than 64 blocks away"));
            return 0;
        }

        LegacyShipEntity live = findLiveShipByUid(level, entry.entityId(), uid);
        if (live != null && live.isAlive()) {
            live.stopRiding();
            live.setTarget(null);
            live.clearCommandState();
            live.setDeltaMovement(Vec3.ZERO);
            live.moveTo(senderPos.x, senderPos.y + 0.5D, senderPos.z, live.getYRot(), 0.0F);
            live.setOrderedToSit(false);
            live.setCanFollow(false);
            live.commandGuardBlock(BlockPos.containing(senderPos.x, senderPos.y + 0.5D, senderPos.z), dimId);
            cache.updateFromShip(live, false, true);
            source.sendSuccess(() -> Component.literal("ship: get: " + uid), true);
            return 1;
        }

        ShipEntitySpec spec = entry.variantEggMeta() > 1
                ? ShipEntitySpecs.findByEggMeta(entry.variantEggMeta())
                : null;
        if (spec == null) {
            spec = ShipEntitySpecs.findByLegacyClassId(entry.legacyClassId());
        }
        if (spec == null) {
            source.sendFailure(Component.literal("ship get: missing spec for UID " + uid));
            return 0;
        }

        LegacyShipEntity spawned = ModEntityTypes.LEGACY_SHIP.get().create(level);
        if (spawned == null) {
            source.sendFailure(Component.literal("ship get: failed to instantiate entity for UID " + uid));
            return 0;
        }

        spawned.setVariantEggMeta(spec.eggMeta());
        spawned.moveTo(senderPos.x, senderPos.y + 0.5D, senderPos.z, 0.0F, 0.0F);
        spawned.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(senderPos)), MobSpawnType.COMMAND, null);
        // Legacy EntityHelper.createShipEntity: full health first, then the saved NBT restores stored HP.
        spawned.setHealth(spawned.getMaxHealth());
        if (!entry.entityTag().isEmpty()) {
            CompoundTag restoreTag = entry.entityTag().copy();
            restoreTag.putInt("ShipUID", uid);
            spawned.readAdditionalSaveData(restoreTag);
            if (spawned.getHealth() < 1.0F) {
                spawned.setHealth(1.0F);
            }
            spawned.setCanDropItem(true);
        }
        spawned.setDeltaMovement(Vec3.ZERO);
        spawned.moveTo(senderPos.x, senderPos.y + 0.5D, senderPos.z, 0.0F, 0.0F);
        level.addFreshEntity(spawned);
        cache.updateFromShip(spawned, false, true);
        source.sendSuccess(() -> Component.literal("ship: spawn: " + uid), true);
        return 1;
    }

    private static int executeShipDel(CommandSourceStack source, int uid) {
        ShipCacheSavedData cache = ShipCacheSavedData.get(source.getLevel());
        if (!cache.removeShip(uid)) {
            source.sendFailure(Component.literal("ship del: unknown ship UID " + uid));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("ship: delete: " + uid), true);
        return 1;
    }

    private static int executeShipKill(CommandSourceStack source, int classId, int range) {
        int rawCid = classId - 2;
        if (rawCid < 0) {
            source.sendFailure(Component.literal("shipkill: classId must be >= 2"));
            return 0;
        }

        boolean hostile = rawCid >= 2000;
        int normalizedCid = hostile ? rawCid - 2000 : rawCid;
        Vec3 pos = source.getPosition();
        AABB area = new AABB(pos.x - range, -256.0D, pos.z - range, pos.x + range, 512.0D, pos.z + range);

        int removed = 0;
        for (LegacyShipEntity ship : source.getLevel().getEntitiesOfClass(LegacyShipEntity.class, area, Entity::isAlive)) {
            if (ship.isHostileVariant() == hostile && ship.getShipClassId() == normalizedCid) {
                ship.discard();
                removed++;
            }
        }
        int finalRemoved = removed;
        source.sendSuccess(() -> Component.literal("shipkill: removed " + finalRemoved + " ships for CID " + classId), true);
        return removed;
    }

    private static int executeShipAttrs(CommandSourceStack source, int level, @Nullable int[] bonuses) {
        LegacyShipEntity ship = raycastShip(source, 16.0D);
        if (ship == null) {
            source.sendFailure(Component.literal("shipattrs: no target ship within 16 blocks"));
            return 0;
        }

        ship.setShipLevel(level);
        if (bonuses != null && bonuses.length == 6) {
            ship.setModernizationSteps(bonuses[0], bonuses[1], bonuses[2], bonuses[3], bonuses[4], bonuses[5]);
        }
        source.sendSuccess(() -> Component.literal("shipattrs: LV " + level + " on " + ship.getName().getString()), true);
        return 1;
    }

    private static int executeShipClearDrop(CommandSourceStack source, int rawRange) {
        int range = rawRange <= 0 ? 128 : rawRange;
        Vec3 pos = source.getPosition();
        // Legacy ShipCmdShipClearDrop searched the full 1.12 build height (0..256); 1.20.1 uses the level build limits.
        AABB area = new AABB(pos.x - range, source.getLevel().getMinBuildHeight(), pos.z - range,
                pos.x + range, source.getLevel().getMaxBuildHeight(), pos.z + range);
        List<ItemEntity> drops = source.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                drop -> drop.isAlive() && LegacyEntityItemMarker.isMarked(drop));
        for (ItemEntity drop : drops) {
            drop.discard();
        }
        int count = drops.size();
        source.sendSuccess(() -> Component.literal("shipcleardrop: removed " + count + " item entities."), true);
        return count;
    }

    private static int executeShipInfo(CommandSourceStack source) {
        LegacyShipEntity ship = raycastShip(source, 32.0D);
        if (ship == null) {
            source.sendFailure(Component.literal("shipinfo: no target ship within 32 blocks"));
            return 0;
        }

        Entity sender = source.getEntity();
        String senderInfo = sender instanceof ServerPlayer player
                ? player.getGameProfile().getName() + " UID=" + TeitokuHelper.getPlayerUid(player) + " UUID=" + player.getUUID()
                : source.getTextName();
        String ownerUuid = ship.getOwnerUuid().map(UUID::toString).orElse("none");
        source.sendSuccess(() -> Component.literal("shipinfo: user=" + senderInfo
                + " | CustomName=" + ship.getName().getString()
                + " EntityID=" + ship.getId()
                + " UID=" + ship.getShipUid()
                + " OwnerUID=" + ship.getOwnerUid()
                + " OwnerUUID=" + ownerUuid
                + " LV=" + ship.getShipLevel()
                + " Morale=" + ship.getMorale()).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int executeStopAi(CommandSourceStack source) {
        boolean stopped = LegacyShipEntity.toggleStopAi();
        source.sendSuccess(() -> Component.literal("shipstopai: stopAI = " + stopped), true);
        return 1;
    }

    private static int executeUpdateOwnerUidSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        return updateShipsForOwner(ctx.getSource(), player);
    }

    private static int executeUpdateOwnerUidTarget(CommandSourceStack source, String playerName) {
        ServerPlayer target = resolvePlayerByName(source.getServer(), playerName);
        if (target == null) {
            source.sendFailure(Component.literal("shipupdateowneruid: player not found: " + playerName));
            return 0;
        }
        return updateShipsForOwner(source, target);
    }

    private static int updateShipsForOwner(CommandSourceStack source, ServerPlayer owner) {
        int ownerUid = TeitokuHelper.getPlayerUid(owner);
        if (ownerUid <= 0) {
            ownerUid = TeitokuSavedData.get(source.getLevel()).getOrCreateUid(owner);
        }
        UUID ownerUuid = owner.getUUID();
        String ownerName = owner.getGameProfile().getName();

        int updated = 0;
        for (ServerLevel world : source.getServer().getAllLevels()) {
            for (Entity entity : world.getAllEntities()) {
                if (entity instanceof LegacyShipEntity ship
                        && ship.getOwnerUuid().map(uuid -> uuid.equals(ownerUuid)).orElse(false)) {
                    ship.setOwner(ownerUuid, ownerName, ownerUid);
                    ShipCacheSavedData.get(world).updateFromShip(ship, !ship.isAlive(), true);
                    updated++;
                }
            }
        }
        int finalUpdated = updated;
        int finalUid = ownerUid;
        source.sendSuccess(() -> Component.literal("shipupdateowneruid: updated " + finalUpdated
                + " ships for " + ownerName + " (UID=" + finalUid + ")"), true);
        return updated;
    }

    private static @Nullable LegacyShipEntity raycastShip(CommandSourceStack source, double maxDist) {
        Entity entity = source.getEntity();
        Vec3 eye = entity instanceof LivingEntity living ? living.getEyePosition(1.0F) : source.getPosition();
        Vec3 look = entity != null ? entity.getViewVector(1.0F) : Vec3.directionFromRotation(source.getRotation());
        Vec3 end = eye.add(look.scale(maxDist));
        AABB searchArea = new AABB(eye, end).inflate(1.5D);

        LegacyShipEntity best = null;
        double bestDistSq = maxDist * maxDist + 1.0D;
        for (LegacyShipEntity candidate : source.getLevel().getEntitiesOfClass(
                LegacyShipEntity.class, searchArea, Entity::isAlive)) {
            if (candidate == entity) {
                continue;
            }
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.5D).clip(eye, end);
            if (hit.isPresent()) {
                double distSq = eye.distanceToSqr(hit.get());
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private static @Nullable LegacyShipEntity findLiveShipByUid(ServerLevel level, int entityIdHint, int shipUid) {
        if (entityIdHint > 0 && level.getEntity(entityIdHint) instanceof LegacyShipEntity hinted
                && hinted.getShipUid() == shipUid) {
            return hinted;
        }
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LegacyShipEntity ship && ship.getShipUid() == shipUid) {
                return ship;
            }
        }
        return null;
    }

    private static @Nullable ServerPlayer resolvePlayerByName(MinecraftServer server, String name) {
        ServerPlayer listed = server.getPlayerList().getPlayerByName(name);
        if (listed != null) {
            return listed;
        }
        for (ServerLevel world : server.getAllLevels()) {
            for (ServerPlayer player : world.players()) {
                if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                    return player;
                }
            }
        }
        return null;
    }

    private static Set<String> collectPlayerNames(MinecraftServer server) {
        Set<String> names = new LinkedHashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
        }
        for (ServerLevel world : server.getAllLevels()) {
            for (ServerPlayer player : world.players()) {
                names.add(player.getGameProfile().getName());
            }
        }
        return names;
    }

    private static Map<String, Integer> buildEmoteAliasMap() {
        return Map.ofEntries(
                Map.entry("0", 0), Map.entry("swt", 0), Map.entry("drop", 0),
                Map.entry("1", 1), Map.entry("lv", 1), Map.entry("love", 1), Map.entry("heart", 1),
                Map.entry("2", 2), Map.entry("swt2", 2), Map.entry("wah", 2), Map.entry("panic", 2),
                Map.entry("3", 3), Map.entry("?", 3),
                Map.entry("4", 4), Map.entry("!", 4),
                Map.entry("5", 5), Map.entry("...", 5),
                Map.entry("6", 6), Map.entry("an", 6), Map.entry("anger", 6), Map.entry("angry", 6),
                Map.entry("7", 7), Map.entry("note", 7), Map.entry("ho", 7),
                Map.entry("8", 8), Map.entry("sob", 8), Map.entry("cry", 8), Map.entry("sad", 8),
                Map.entry("9", 9), Map.entry("spit", 9), Map.entry("rice", 9), Map.entry("hungry", 9),
                Map.entry("10", 10), Map.entry("spin", 10), Map.entry("dizzy", 10),
                Map.entry("11", 11), Map.entry("find", 11), Map.entry("??", 11),
                Map.entry("12", 12), Map.entry("omg", 12), Map.entry("shock", 12),
                Map.entry("13", 13), Map.entry("ok", 13), Map.entry("nod", 13),
                Map.entry("14", 14), Map.entry("fsh", 14), Map.entry("flash", 14), Map.entry("+_+", 14),
                Map.entry("15", 15), Map.entry("kiss", 15), Map.entry("kis", 15),
                Map.entry("16", 16), Map.entry("lol", 16), Map.entry("ha", 16), Map.entry("heh", 16),
                Map.entry("17", 17), Map.entry("gg", 17), Map.entry("giggle", 17),
                Map.entry("18", 18), Map.entry("sigh", 18),
                Map.entry("19", 19), Map.entry("meh", 19), Map.entry("lick", 19),
                Map.entry("20", 20), Map.entry("orz", 20), Map.entry("otl", 20),
                Map.entry("21", 21), Map.entry("o", 21), Map.entry("oh", 21), Map.entry("yes", 21),
                Map.entry("22", 22), Map.entry("x", 22), Map.entry("no", 22),
                Map.entry("23", 23), Map.entry("!?", 23), Map.entry("surprised", 23),
                Map.entry("24", 24), Map.entry("rock", 24), Map.entry("bawi", 24),
                Map.entry("25", 25), Map.entry("paper", 25), Map.entry("bo", 25),
                Map.entry("26", 26), Map.entry("scissors", 26), Map.entry("gawi", 26), Map.entry("ya", 26), Map.entry("yeah", 26),
                Map.entry("27", 27), Map.entry("-w-", 27),
                Map.entry("28", 28), Map.entry("-o-", 28),
                Map.entry("29", 29), Map.entry("blink", 29), Map.entry("wink", 29),
                Map.entry("30", 30), Map.entry("pif", 30),
                Map.entry("31", 31), Map.entry("shine", 31), Map.entry("shy", 31),
                Map.entry("32", 32), Map.entry("hmm", 32),
                Map.entry("33", 33), Map.entry(":p", 33),
                Map.entry("34", 34), Map.entry("lll", 34)
        );
    }
}
