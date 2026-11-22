package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.item.LegacyEntityItemMarker;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyDropMarkerPersistenceGameTests {
    private static final String MARKER = "shincolle:BasicEntityItem";

    private LegacyDropMarkerPersistenceGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void forgeDataDropMarkerSurvivesLoadRadarAndClearCommand(GameTestHelper helper) throws Exception {
        CompoundTag saved = itemTag(helper);
        CompoundTag forgeData = new CompoundTag();
        forgeData.putBoolean(MARKER, true);
        forgeData.putString("othermod:unrelated", "not a ShinColle marker");
        saved.put("ForgeData", forgeData);
        ItemEntity migrated = loadItem(helper, saved);
        helper.assertTrue(LegacyEntityItemMarker.isMarked(migrated),
                "real Entity.load must restore a drop marker stored in ForgeData");
        helper.assertTrue(!migrated.getPersistentData().contains("othermod:unrelated"),
                "migration must import only ShinColle's marker, not arbitrary Forge mod data");
        CompoundTag resaved = migrated.saveWithoutId(new CompoundTag());
        helper.assertTrue(resaved.getCompound("NeoForgeData").getBoolean(MARKER)
                        && LegacyEntityItemMarker.isMarked(loadItem(helper, resaved)),
                "migrated marker must persist through native save/load");
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(2, 2, 2)).getCenter();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "ReviewDropMarker"));
        player.setGameMode(GameType.CREATIVE);
        player.setPos(origin);
        migrated.setPos(origin.x + 0.25D, origin.y, origin.z + 0.25D);
        ItemEntity ordinary = new ItemEntity(level, origin.x + 0.5D, origin.y, origin.z + 0.5D,
                new ItemStack(Items.APPLE));
        try {
            helper.assertTrue(level.addFreshEntity(migrated) && level.addFreshEntity(ordinary),
                    "radar/command fixtures must enter the level");
            ListTag contacts = TeitokuHelper.buildDeskRadarPayloadTag(player)
                    .getList(TeitokuHelper.DESK_RADAR_PAYLOAD_TAG, Tag.TAG_COMPOUND);
            helper.assertTrue(hasContact(contacts, migrated) && !hasContact(contacts, ordinary),
                    "Desk radar must discover the loaded ForgeData drop and skip ordinary items");
            int removed = level.getServer().getCommands().getDispatcher().execute("shipcleardrop 1",
                    player.createCommandSourceStack().withPermission(4));
            helper.assertTrue(removed >= 1 && !migrated.isAlive() && ordinary.isAlive(),
                    "shipcleardrop must remove the loaded ForgeData drop without removing ordinary items");
        } finally {
            migrated.discard();
            ordinary.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void absentAndMalformedForgeMarkersRemainUnmarked(GameTestHelper helper) {
        CompoundTag saved = itemTag(helper);
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loadItem(helper, saved)),
                "missing ForgeData must not invent a drop marker");
        CompoundTag forgeData = new CompoundTag();
        saved.put("ForgeData", forgeData);
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loadItem(helper, saved)),
                "empty ForgeData must stay unmarked");
        forgeData.putString(MARKER, "true");
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loadItem(helper, saved)),
                "a malformed non-boolean marker must not mark an item");
        forgeData.putBoolean(MARKER, false);
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loadItem(helper, saved)),
                "an explicit false marker must stay false");
        saved.putString("ForgeData", "malformed");
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loadItem(helper, saved)),
                "a non-compound ForgeData tag must stay unmarked");
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void nativeMarkerWinsOverLegacyForgeData(GameTestHelper helper) {
        CompoundTag saved = itemTag(helper);
        CompoundTag forgeData = new CompoundTag();
        forgeData.putBoolean(MARKER, true);
        saved.put("ForgeData", forgeData);
        CompoundTag nativeData = new CompoundTag();
        nativeData.putBoolean(MARKER, false);
        nativeData.putString("othermod:native", "preserve");
        saved.put("NeoForgeData", nativeData);
        ItemEntity loaded = loadItem(helper, saved);
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loaded)
                        && loaded.getPersistentData().getString("othermod:native").equals("preserve"),
                "native persistent marker, including false, must win without damaging other native data");
        loaded = loadItem(helper, loaded.saveWithoutId(new CompoundTag()));
        helper.assertTrue(!LegacyEntityItemMarker.isMarked(loaded),
                "save/reload must not resurrect a superseded legacy marker");
        helper.succeed();
    }

    private static CompoundTag itemTag(GameTestHelper helper) {
        return new ItemEntity(helper.getLevel(), 0D, 0D, 0D, new ItemStack(Items.EGG))
                .saveWithoutId(new CompoundTag());
    }

    private static ItemEntity loadItem(GameTestHelper helper, CompoundTag saved) {
        var item = new ItemEntity(helper.getLevel(), 0D, 0D, 0D, new ItemStack(Items.EGG));
        item.load(saved.copy());
        return item;
    }

    private static boolean hasContact(ListTag contacts, ItemEntity item) {
        for (int i = 0; i < contacts.size(); i++) {
            CompoundTag contact = contacts.getCompound(i);
            if (contact.getString("Kind").equals("ITEM")
                    && contact.getFloat("X") == (float) item.getX()
                    && contact.getFloat("Y") == (float) item.getY()
                    && contact.getFloat("Z") == (float) item.getZ()) {
                return true;
            }
        }
        return false;
    }
}
