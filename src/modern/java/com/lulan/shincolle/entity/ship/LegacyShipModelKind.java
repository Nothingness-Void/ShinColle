package com.lulan.shincolle.entity.ship;

public enum LegacyShipModelKind {
    ModelTransportWa,
    ModelBattleshipRe,
    ModelBattleshipRu,
    ModelBattleshipTa,
    ModelHeavyCruiserRi,
    ModelHeavyCruiserNe,
    ModelCarrierWo,
    ModelDestroyerI,
    ModelDestroyerRo,
    ModelDestroyerHa,
    ModelDestroyerNi,
    ModelAirfieldHime,
    ModelBattleshipHime,
    ModelDestroyerHime,
    ModelCarrierHime,
    ModelCAHime,
    ModelHarbourHime,
    ModelIsolatedHime,
    ModelMidwayHime,
    ModelNorthernHime,
    ModelSubmHime,
    ModelSSNH,
    ModelSubmKa,
    ModelSubmYo,
    ModelSubmSo,
    ModelCarrierWDemon,
    ModelBattleshipNagato,
    ModelBattleshipYamato,
    ModelBBKongou,
    ModelBBHiei,
    ModelBBHaruna,
    ModelBBKirishima,
    ModelCarrierAkagi,
    ModelCarrierKaga,
    ModelCruiserTenryuu,
    ModelCruiserTatsuta,
    ModelCruiserAtago,
    ModelCruiserTakao,
    ModelDestroyerAkatsuki,
    ModelDestroyerHibiki,
    ModelDestroyerIkazuchi,
    ModelDestroyerInazuma,
    ModelDestroyerShimakaze,
    ModelSubmRo500,
    ModelSubmU511;

    public static LegacyShipModelKind forSpec(ShipEntitySpec spec) {
        return valueOf(spec.modelSourceStem());
    }

    public float shadowRadius() {
        return switch (this) {
            case ModelNorthernHime, ModelSSNH, ModelSubmKa, ModelSubmSo, ModelSubmYo,
                    ModelDestroyerAkatsuki, ModelDestroyerHibiki, ModelDestroyerIkazuchi,
                    ModelDestroyerInazuma, ModelDestroyerShimakaze, ModelSubmRo500, ModelSubmU511 -> 0.5F;
            case ModelCruiserTenryuu, ModelCruiserTatsuta, ModelDestroyerHime -> 0.6F;
            case ModelHarbourHime, ModelMidwayHime -> 0.8F;
            case ModelDestroyerI, ModelDestroyerRo, ModelDestroyerHa, ModelDestroyerNi -> 0.9F;
            case ModelCarrierWo -> 1F;
            default -> 0.7F;
        };
    }
}
