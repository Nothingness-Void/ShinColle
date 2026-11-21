package com.lulan.shincolle.client.model.legacy.ship;

import com.lulan.shincolle.client.model.legacy.compat.ModelBase;
import com.lulan.shincolle.client.model.legacy.compat.MiscModel;
import com.lulan.shincolle.entity.ship.LegacyShipModelKind;
import java.util.ArrayList;

public final class LegacyShipModels {
    private LegacyShipModels() {}
    public static ModelBase create(LegacyShipModelKind kind) {
        ModelBase model = switch (kind) {
            case ModelTransportWa -> new ModelTransportWa();
            case ModelBattleshipRe -> new ModelBattleshipRe();
            case ModelBattleshipRu -> new ModelBattleshipRu();
            case ModelBattleshipTa -> new ModelBattleshipTa();
            case ModelHeavyCruiserRi -> new ModelHeavyCruiserRi();
            case ModelHeavyCruiserNe -> new ModelHeavyCruiserNe();
            case ModelCarrierWo -> new ModelCarrierWo();
            case ModelDestroyerI -> new ModelDestroyerI();
            case ModelDestroyerRo -> new ModelDestroyerRo();
            case ModelDestroyerHa -> new ModelDestroyerHa();
            case ModelDestroyerNi -> new ModelDestroyerNi();
            case ModelAirfieldHime -> new ModelAirfieldHime();
            case ModelBattleshipHime -> new ModelBattleshipHime();
            case ModelDestroyerHime -> new ModelDestroyerHime();
            case ModelCarrierHime -> new ModelCarrierHime();
            case ModelCAHime -> new ModelCAHime();
            case ModelHarbourHime -> new ModelHarbourHime();
            case ModelIsolatedHime -> new ModelIsolatedHime();
            case ModelMidwayHime -> new ModelMidwayHime();
            case ModelNorthernHime -> new ModelNorthernHime();
            case ModelSubmHime -> new ModelSubmHime();
            case ModelSSNH -> new ModelSSNH();
            case ModelSubmKa -> new ModelSubmKa();
            case ModelSubmYo -> new ModelSubmYo();
            case ModelSubmSo -> new ModelSubmSo();
            case ModelCarrierWDemon -> new ModelCarrierWDemon();
            case ModelBattleshipNagato -> new ModelBattleshipNagato();
            case ModelBattleshipYamato -> new ModelBattleshipYamato();
            case ModelBBKongou -> new ModelBBKongou();
            case ModelBBHiei -> new ModelBBHiei();
            case ModelBBHaruna -> new ModelBBHaruna();
            case ModelBBKirishima -> new ModelBBKirishima();
            case ModelCarrierAkagi -> new ModelCarrierAkagi();
            case ModelCarrierKaga -> new ModelCarrierKaga();
            case ModelCruiserTenryuu -> new ModelCruiserTenryuu();
            case ModelCruiserTatsuta -> new ModelCruiserTatsuta();
            case ModelCruiserAtago -> new ModelCruiserAtago();
            case ModelCruiserTakao -> new ModelCruiserTakao();
            case ModelDestroyerAkatsuki -> new ModelDestroyerAkatsuki();
            case ModelDestroyerHibiki -> new ModelDestroyerHibiki();
            case ModelDestroyerIkazuchi -> new ModelDestroyerIkazuchi();
            case ModelDestroyerInazuma -> new ModelDestroyerInazuma();
            case ModelDestroyerShimakaze -> new ModelDestroyerShimakaze();
            case ModelSubmRo500 -> new ModelSubmRo500();
            case ModelSubmU511 -> new ModelSubmU511();
        };
        if (model instanceof ModelMidwayHime midway) {
            midway.miscModelList = new ArrayList<>();
            midway.miscModelList.add(new MiscModel());
            midway.miscModelList.get(0).entity.posY = 0.34D;
            midway.miscModelList.get(0).entity.posZ = -0.18D;
        }
        return model;
    }
}
