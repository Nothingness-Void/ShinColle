package com.lulan.shincolle.entity.ship;

import java.util.Optional;

/**
 * Runtime projection of the scale branches used by the selected 1.12 ship models.
 */
public final class LegacyShipModelTransformCatalog {

    private LegacyShipModelTransformCatalog() {
    }

    public static Optional<RenderTransform> forModel(String modelSourceStem, int scaleLevel) {
        if (modelSourceStem == null || modelSourceStem.isEmpty()) {
            return Optional.empty();
        }

        return switch (modelSourceStem) {
            case "ModelSubmU511", "ModelSubmRo500" -> Optional.of(submarineTransform(scaleLevel));
            case "ModelBattleshipYamato" -> Optional.of(yamatoTransform(scaleLevel));
            case "ModelBattleshipNagato" -> Optional.of(nagatoTransform(scaleLevel));
            case "ModelBBKongou", "ModelBBHiei", "ModelBBHaruna", "ModelBBKirishima" ->
                    Optional.of(kongouTransform(scaleLevel));
            case "ModelCarrierKaga", "ModelCarrierAkagi" -> Optional.of(kagaTransform(scaleLevel));
            case "ModelCruiserAtago", "ModelCruiserTakao" -> Optional.of(atagoTransform(scaleLevel));
            case "ModelCruiserTenryuu", "ModelCruiserTatsuta", "ModelDestroyerShimakaze" ->
                    Optional.of(tenryuuTransform(scaleLevel));
            case "ModelDestroyerAkatsuki", "ModelDestroyerHibiki", "ModelDestroyerIkazuchi", "ModelDestroyerInazuma" ->
                    Optional.of(akatsukiTransform(scaleLevel));
            default -> Optional.empty();
        };
    }

    private static RenderTransform submarineTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> scaled(1.44F, -0.45F);
            case 2 -> scaled(1.08F, -0.06F);
            case 1 -> scaled(0.72F, 0.66F);
            default -> scaled(0.36F, 2.86F);
        };
    }

    private static RenderTransform yamatoTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(2.0F, -0.73F);
            case 2 -> uniform(1.5F, -0.48F);
            case 1 -> uniform(1.0F, 0.02F);
            default -> uniform(0.5F, 1.53F);
        };
    }

    private static RenderTransform nagatoTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(2.0F, -0.73F);
            case 2 -> uniform(1.5F, -0.48F);
            case 1 -> uniform(1.0F, 0.02F);
            default -> uniform(0.5F, 1.51F);
        };
    }

    private static RenderTransform kongouTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(1.8F, -0.69F);
            case 2 -> uniform(1.35F, -0.41F);
            case 1 -> uniform(0.9F, 0.14F);
            default -> uniform(0.45F, 1.79F);
        };
    }

    private static RenderTransform kagaTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(1.84F, -0.63F);
            case 2 -> uniform(1.38F, -0.37F);
            case 1 -> uniform(0.92F, 0.16F);
            default -> uniform(0.46F, 1.81F);
        };
    }

    private static RenderTransform atagoTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(1.72F, -0.58F);
            case 2 -> uniform(1.29F, -0.27F);
            case 1 -> uniform(0.86F, 0.35F);
            default -> uniform(0.43F, 2.0F);
        };
    }

    private static RenderTransform tenryuuTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(1.64F, -0.58F);
            case 2 -> uniform(1.23F, -0.27F);
            case 1 -> uniform(0.82F, 0.35F);
            default -> uniform(0.41F, 2.17F);
        };
    }

    private static RenderTransform akatsukiTransform(int scaleLevel) {
        return switch (scaleLevel) {
            case 3 -> uniform(1.6F, -0.53F);
            case 2 -> uniform(1.2F, -0.23F);
            case 1 -> uniform(0.8F, 0.41F);
            default -> uniform(0.4F, 2.28F);
        };
    }

    private static RenderTransform scaled(float scale, float offsetY) {
        return new RenderTransform(scale, scale * 0.95F, scale, offsetY);
    }

    private static RenderTransform uniform(float scale, float offsetY) {
        return new RenderTransform(scale, scale, scale, offsetY);
    }

    public record RenderTransform(float scaleX, float scaleY, float scaleZ, float offsetY) {
    }
}
