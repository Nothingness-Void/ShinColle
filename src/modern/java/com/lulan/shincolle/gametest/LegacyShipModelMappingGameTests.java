package com.lulan.shincolle.gametest;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.ship.LegacyShipAttackKind;
import com.lulan.shincolle.entity.ship.LegacyShipBehaviorCatalog;
import com.lulan.shincolle.entity.ship.LegacyShipModelKind;
import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ShinColle.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegacyShipModelMappingGameTests {
    private LegacyShipModelMappingGameTests() {}

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
