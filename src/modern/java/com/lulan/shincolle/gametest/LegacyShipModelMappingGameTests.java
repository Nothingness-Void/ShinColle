package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.compat.LegacyItemProperties;
import com.lulan.shincolle.config.ShinColleConfig;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipModelKind;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.lulan.shincolle.registry.ModItems;
import com.lulan.shincolle.registry.ModBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyShipModelMappingGameTests {
    private LegacyShipModelMappingGameTests() {}

    @GameTest(template = "gameplayparitygametests.empty")
    public static void shipRuntimeDimensionsRetainLegacyCatalogValues(GameTestHelper helper) {
        var ship = com.lulan.shincolle.registry.ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        for (var spec : com.lulan.shincolle.entity.ship.ShipEntitySpecs.values()) {
            ship.setVariantEggMeta(spec.eggMeta());
            for (int scale = 0; scale < 4; scale++) {
                ship.initializeHostileRuntime(false, scale);
                ship.refreshDimensions();
                var expected = com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog.dimensionsFor(
                        spec, ship.getLegacyHostileScaleLevel());
                for (boolean sitting : new boolean[] {false, true}) {
                    ship.setOrderedToSit(sitting);
                    var actual = ship.getDimensions(net.minecraft.world.entity.Pose.STANDING);
                    helper.assertTrue(ship.getScale() == 1.0F
                                    && Math.abs(actual.width - expected.width) < 1.0E-6F
                                    && Math.abs(actual.height - expected.height) < 1.0E-6F
                                    && Math.abs(ship.getBoundingBox().getYsize() - expected.height) < 1.0E-6D,
                            "runtime hitbox must retain the legacy dimensions for " + spec.eggMeta());
                    float eyeHeight = com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog.eyeHeightFor(ship, expected);
                    helper.assertTrue(Math.abs(ship.getEyeHeight() - eyeHeight) < 1.0E-6F,
                            "EntityDimensions must carry the legacy standing eye height");
                }
            }
        }
        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void minecartPassengersRetainLegacyFeetAnchorInBothPoses(GameTestHelper helper) {
        var cart = net.minecraft.world.entity.EntityType.MINECART.create(helper.getLevel());
        var ship = com.lulan.shincolle.registry.ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        cart.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        for (var spec : ShipEntitySpecs.values()) {
            ship.setVariantEggMeta(spec.eggMeta());
            helper.assertTrue(ship.startRiding(cart, true), "ship must actually ride the cart");
            for (int emotion : new int[] {0, 4}) {
                ship.setEmotion(emotion);
                cart.positionRider(ship);
                helper.assertTrue(ship.isPassenger()
                                && Math.abs(ship.getY() - cart.getY() - ship.getMyRidingOffset()) < 1.0E-6D
                                && ship.getX() == cart.getX() && ship.getZ() == cart.getZ(),
                        "legacy cart anchor must be zero before the ship offset: egg " + spec.eggMeta()
                                + ", emotion " + emotion);
            }
            ship.stopRiding();
        }
        ship.discard();
        cart.discard();
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void destroyerHimeUsesRenderOnlyLandLegs(GameTestHelper helper) {
        var ship = com.lulan.shincolle.registry.ModEntityTypes.LEGACY_SHIP.get().create(helper.getLevel());
        ship.applySpawnSpec(ShipEntitySpecs.getByEggMeta(29), null);
        helper.assertTrue(LegacyShipModelKind.forSpec(ship.getSpec()) == LegacyShipModelKind.ModelDestroyerHime
                        && ship.getModelState() == 0 && ship.getLegacyModelEmotion(0) == 0,
                "Destroyer Hime must retain the legacy default model state, with leg bit 4 disabled");
        ship.setGrudge(1000);
        helper.assertTrue(!ship.hasNoFuel(), "the fueled ship must reach the normal model pose");
        for (int state : new int[] {0, 1, 16, 255}) {
            ship.setModelState(state);
            for (boolean sitting : new boolean[] {false, true}) {
                ship.setOrderedToSit(sitting);
                helper.assertTrue(ship.getLegacyModelEmotion(0) == state && ship.isOrderedToSit() == sitting,
                        "standing/sitting must not silently rewrite the legacy leg-visibility bit");
                for (double depth : new double[] {-0.1D, 0D, 0.1D, 3D}) {
                    boolean expected = depth <= 0D || (state & 16) != 0;
                    helper.assertTrue(com.lulan.shincolle.client.model.legacy.DestroyerHimePosePolicy
                                    .showLegs(ship.getLegacyModelEmotion(0), depth) == expected,
                            "land must show legs; water must honor the explicit leg bit");
                    helper.assertTrue(ship.getModelState() == state,
                            "the visual override must never rewrite saved model state");
                }
            }
        }
        ship.discard();
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void hudPositionUsesLegacyConfig(GameTestHelper helper) {
        try {
            double[] defaults = {0.5D, 0.6D};
            for (Object invalid : List.of(List.of(), List.of(1D), List.of(1D, 2D, 3D),
                    List.of("x", "y"), "invalid")) {
                ShinColleConfig.setTestOverride("Position_HUD", invalid);
                if (!Arrays.equals(ShinColleConfig.posHUD(), defaults)) {
                    helper.fail("Invalid or empty HUD positions must retain the legacy defaults");
                    return;
                }
            }
            ShinColleConfig.setTestOverride("Position_HUD", List.of(0.25D, 0.75D));
            if (!Arrays.equals(ShinColleConfig.posHUD(), new double[] {0.25D, 0.75D})) {
                helper.fail("HUD position must use both configured screen fractions");
                return;
            }
            double[] custom = {-0.1D, 1.2D};
            ShinColleConfig.setTestOverride("Position_HUD", custom);
            double[] result = ShinColleConfig.posHUD();
            result[0] = 0D;
            if (!Arrays.equals(ShinColleConfig.posHUD(), custom)) {
                helper.fail("HUD position must not clamp the legacy values or expose its backing array");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverride("Position_HUD");
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void heldItemPropertiesAndSettingsMatchLegacy(GameTestHelper helper) {
        for (Item item : List.of(Items.DIAMOND_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL,
                Items.IRON_HOE, Items.FISHING_ROD, Items.CARROT_ON_A_STICK, Items.STICK, Items.BONE, Items.BLAZE_ROD,
                ModItems.TARGETWRENCH.get(), ModItems.POINTERITEM.get(), ModItems.KAITAIHAMMER.get(), ModItems.OPTOOL.get())) {
            if (!LegacyItemProperties.isFull3D(new ItemStack(item))) {
                helper.fail("Legacy full-3D held item must retain its weapon scale: " + item);
                return;
            }
        }
        for (Item item : List.of(Items.APPLE, Items.STONE, Items.BOW, Items.SHIELD, Items.SHEARS,
                Items.FLINT_AND_STEEL, ModItems.BUCKETREPAIR.get())) {
            if (LegacyItemProperties.isFull3D(new ItemStack(item))) {
                helper.fail("Legacy ordinary/block item must not acquire weapon scale: " + item);
                return;
            }
        }
        if (LegacyItemProperties.isFull3D(ItemStack.EMPTY)) {
            helper.fail("An empty held stack must not be classified as full-3D");
            return;
        }
        for (Item item : List.of(Items.STONE, Items.SNOW, Items.OAK_SAPLING, Items.OAK_FENCE,
                Items.WHITE_BANNER, Items.LILY_PAD, ModBlocks.BLOCK_CRANE.get().asItem())) {
            if (!LegacyItemProperties.isBlock(new ItemStack(item))) {
                helper.fail("Legacy ItemBlock must retain block offsets and scaling: " + item);
                return;
            }
        }
        for (Item item : List.of(Items.CAKE, Items.REPEATER, Items.COMPARATOR, Items.BREWING_STAND, Items.CAULDRON,
                Items.FLOWER_POT, Items.STRING, Items.SUGAR_CANE, Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO,
                Items.OAK_DOOR, Items.IRON_DOOR, Items.OAK_SIGN, Items.SKELETON_SKULL, Items.WHITE_BED, Items.APPLE)) {
            if (LegacyItemProperties.isBlock(new ItemStack(item))) {
                helper.fail("Legacy non-ItemBlock must retain ordinary item offsets and scaling: " + item);
                return;
            }
        }
        if (LegacyItemProperties.isBlock(ItemStack.EMPTY)) {
            helper.fail("An empty held stack must not be classified as a block item");
            return;
        }
        try {
            double[] defaults = {2.5D, 0D, 0D, 0D};
            ShinColleConfig.setTestOverride("scaleHeldItem", List.of());
            if (!Arrays.equals(ShinColleConfig.scaleHeldItem(), defaults)) {
                helper.fail("Empty held-item settings must use the legacy defaults");
                return;
            }
            double[] custom = {1.25D, -1D, 2D, -3D};
            ShinColleConfig.setTestOverride("scaleHeldItem", List.of(1.25D, -1D, 2D, -3D));
            if (!Arrays.equals(ShinColleConfig.scaleHeldItem(), custom)) {
                helper.fail("Held-item settings must retain all four legacy values");
                return;
            }
            ShinColleConfig.setTestOverride("scaleHeldItem", custom);
            double[] copy = ShinColleConfig.scaleHeldItem();
            copy[0] = 99D;
            if (!Arrays.equals(ShinColleConfig.scaleHeldItem(), custom)) {
                helper.fail("Held-item settings access must not mutate the configuration");
                return;
            }
            ShinColleConfig.setTestOverride("scaleHeldItem", List.of("invalid", 0D, 0D, 0D));
            if (!Arrays.equals(ShinColleConfig.scaleHeldItem(), defaults)) {
                helper.fail("Invalid held-item settings must use the legacy defaults");
                return;
            }
            ShinColleConfig.setTestOverride("scaleHeldItem", List.of(2D, 0D, 0D));
            if (!Arrays.equals(ShinColleConfig.scaleHeldItem(), defaults)) {
                helper.fail("Wrong-length held-item settings must use the legacy defaults");
                return;
            }
        } finally {
            ShinColleConfig.clearTestOverride("scaleHeldItem");
        }
        helper.succeed();
    }

    @GameTest(template = "gameplayparitygametests.empty")
    public static void everyShipMapsToLegacyModelAndAttackParticle(GameTestHelper helper) {
        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            LegacyShipModelKind kind = LegacyShipModelKind.forSpec(spec);
            if (!kind.name().equals(spec.modelSourceStem()) || kind.shadowRadius() <= 0F) {
                helper.fail("Missing concrete legacy model/shadow mapping: " + spec.textureStem());
                return;
            }
            int lightParticle = switch (spec.textureStem()) {
                case "EntityCruiserTenryuu" -> 15;
                case "EntityCruiserTatsuta" -> 16;
                default -> 0;
            };
            int heavyParticle = lightParticle == 0 ? 0 : 12;
            int skillParticle = lightParticle == 0 ? 0 : -1;
            if (LegacyShipBehaviorCatalog.modelAttackAnimationParticleFor(spec, LegacyShipAttackKind.LIGHT, 0) != lightParticle
                    || LegacyShipBehaviorCatalog.modelAttackAnimationParticleFor(spec, LegacyShipAttackKind.HEAVY, 0) != heavyParticle
                    || LegacyShipBehaviorCatalog.modelAttackAnimationParticleFor(spec, LegacyShipAttackKind.HEAVY, -1) != skillParticle
                    || LegacyShipBehaviorCatalog.modelAttackAnimationParticleFor(spec, LegacyShipAttackKind.HEAVY, 1) != skillParticle) {
                helper.fail("Legacy attack particle branches differ: " + spec.textureStem());
                return;
            }
        }
        helper.succeed();
    }
}
