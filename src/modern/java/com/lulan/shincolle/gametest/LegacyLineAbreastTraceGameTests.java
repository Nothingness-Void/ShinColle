package com.lulan.shincolle.gametest;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.entity.ship.goal.LegacyShipFollowOwnerGoal;
import com.lulan.shincolle.registry.ModEntityTypes;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.lulan.shincolle.teitoku.TeitokuSavedData;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.UUID;

@GameTestHolder("shincolle")
@PrefixGameTestTemplate(false)
public final class LegacyLineAbreastTraceGameTests {
    private LegacyLineAbreastTraceGameTests() {}

    @GameTest(template = "legacyaircraftblockcollisiongametests.flight_space")
    public static void pickupOffRealFollowTicksMatchLegacyAbreastTargetAndArrival(GameTestHelper helper) throws Exception {
        for (int x = 1; x < 25; x++) for (int z = 1; z < 23; z++) helper.setBlock(x, 1, z, Blocks.STONE);
        var owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AbreastTrace"));
        owner.setPos(helper.absoluteVec(new Vec3(12.5, 2, 18.5)));
        var data = TeitokuHelper.get(owner).orElseThrow(IllegalStateException::new);
        data.setPlayerUid(TeitokuSavedData.get(helper.getLevel()).getOrCreateUid(owner));
        data.setFormationId(5);
        LegacyShipEntity[] placeholders = new LegacyShipEntity[6];
        for (int i = 0; i < 6; i++) {
            var member = ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
            member.applySpawnSpec(ShipEntitySpecs.getByEggMeta(53), owner);
            member.setNoAi(true);
            member.setPos(helper.absoluteVec(new Vec3(2 + i, 2, 2)));
            helper.getLevel().addFreshEntity(member);
            placeholders[i] = member;
            data.assignCurrentTeamSlot(i, member.getShipUid());
        }
        StringBuilder csv = new StringBuilder("slot,tick,targetX,targetZ,x,z,actualSq,cachedSq,continue,pathIndex,pathLength,finalX,finalZ\n");
        for (int slot = 0; slot < 6; slot++) {
            var ship = new LegacyShipEntity(ModEntityTypes.LEGACY_SHIP.get(), helper.getLevel()) {
                @Override protected void registerGoals() {}
                @Override public Player getOwnerPlayer() { return owner; }
            };
            ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(53), owner);
            data.assignCurrentTeamSlot(slot, ship.getShipUid());
            ship.setPos(helper.absoluteVec(new Vec3(12.5, 2, 3.5)));
            helper.getLevel().addFreshEntity(ship);
            ship.setAiPickItem(false);
            ship.setCanFollow(true);
            ship.setGrudge(1000);
            ship.tickCount = 41;
            for (int warm = 0; warm < 20; warm++) { ship.tickCount++; ship.tick(); }
            helper.assertTrue(ship.getLegacyFormationId() == 5 && ship.getLegacyFormationSlot() == slot && !ship.isAiPickItem(),
                    "formation5 slot and pickup-off must actually apply before tracing: " + ship.getLegacyFormationId()
                            + "/" + ship.getLegacyFormationSlot() + "/" + ship.isAiPickItem() + " uid=" + ship.getShipUid());
            var goal = new LegacyShipFollowOwnerGoal(ship);
            int offset = slot == 0 ? 0 : ((slot + 1) / 2) * 3 * ((slot & 1) == 1 ? 1 : -1);
            Vec3 expected = new Vec3(Math.floor(owner.getX()) + offset, Math.floor(owner.getY() + 0.5), Math.floor(owner.getZ()));
            boolean running = false;
            double initial = ship.position().distanceToSqr(expected);
            for (int tick = 0; tick < 384; tick++) {
                if (!running) {
                    boolean canUse = goal.canUse();
                    helper.assertTrue(canUse == (number(goal, "distanceSqr") > 7D), "start must use legacy squared7 hysteresis");
                    if (canUse) { goal.start(); running = true; }
                }
                if (running) {
                    double cached = number(goal, "distanceSqr");
                    boolean expectedContinue = cached > 4D || !ship.getShipNavigate().noPath()
                            || ship.position().distanceToSqr(expected) > 7D;
                    boolean continuing = goal.canContinueToUse();
                    helper.assertTrue(continuing == expectedContinue, "continue/arrival must use cached4/path/fresh7 legacy condition");
                    if (continuing) goal.tick(); else { goal.stop(); running = false; }
                }
                Vec3 target = (Vec3) field(goal, "position");
                helper.assertTrue(target.equals(expected) && number(goal, "minDistanceSqr") == 4D
                                && number(goal, "maxDistanceSqr") == 7D,
                        "per-tick target and pickup-off ranges must match independent legacy formula");
                var path = ship.getShipNavigate().getPath();
                var last = path == null ? null : path.getFinalPathPoint();
                csv.append(slot).append(',').append(tick).append(',').append(target.x).append(',').append(target.z)
                        .append(',').append(ship.getX()).append(',').append(ship.getZ()).append(',')
                        .append(ship.position().distanceToSqr(target)).append(',').append(number(goal, "distanceSqr"))
                        .append(',').append(running).append(',').append(path == null ? -1 : path.getCurrentPathIndex())
                        .append(',').append(path == null ? 0 : path.getCurrentPathLength()).append(',')
                        .append(last == null ? 0 : last.xCoord).append(',').append(last == null ? 0 : last.zCoord).append('\n');
                ship.tickCount++;
                ship.tick();
            }
            double remaining = ship.position().distanceToSqr(expected);
            helper.assertTrue(remaining < initial - 25D && remaining <= 7D,
                    "flat clear abreast slot must progress into the legacy arrival envelope: " + slot + " remainingSq=" + remaining);
            goal.stop();
            helper.assertTrue(ship.getShipNavigate().noPath(), "stop must clear the empty/finished navigation path");
            ship.discard();
            data.assignCurrentTeamSlot(slot, placeholders[slot].getShipUid());
        }
        for (var member : placeholders) member.discard();
        Files.writeString(helper.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("shincolle-line-abreast-trace.csv"), csv);
        helper.succeed();
    }

    private static Object field(LegacyShipFollowOwnerGoal goal, String name) throws ReflectiveOperationException {
        var field = LegacyShipFollowOwnerGoal.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(goal);
    }

    private static double number(LegacyShipFollowOwnerGoal goal, String name) throws ReflectiveOperationException {
        return ((Number) field(goal, name)).doubleValue();
    }
}
