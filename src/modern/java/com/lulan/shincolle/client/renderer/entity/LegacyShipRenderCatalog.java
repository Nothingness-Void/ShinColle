package com.lulan.shincolle.client.renderer.entity;

import com.lulan.shincolle.entity.ship.ShipEntitySpec;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renderer-side projection of the 1.12 ship roster.
 * Every registered legacy ship variant must resolve through this table rather than a generic default path.
 */
public final class LegacyShipRenderCatalog {

    private static final Map<Integer, RenderEntry> ENTRIES = buildEntries();

    private LegacyShipRenderCatalog() {
    }

    public static RenderEntry forSpec(ShipEntitySpec spec) {
        RenderEntry entry = ENTRIES.get(spec.eggMeta());
        if (entry == null) {
            throw new IllegalStateException("No legacy ship render entry for egg meta " + spec.eggMeta());
        }
        return entry;
    }

    public static Collection<RenderEntry> entries() {
        return ENTRIES.values();
    }

    private static Map<Integer, RenderEntry> buildEntries() {
        Map<Integer, RenderEntry> entries = new LinkedHashMap<>();

        for (ShipEntitySpec spec : ShipEntitySpecs.values()) {
            RenderEntry previous = entries.put(spec.eggMeta(), new RenderEntry(
                    spec.eggMeta(),
                    spec.modelSourceStem(),
                    spec.modelSourceLocation(),
                    spec.textureLocation(),
                    spec.archetype().shadowRadius(),
                    spec.hostile()));
            if (previous != null) {
                throw new IllegalStateException("Duplicate legacy ship render entry for egg meta " + spec.eggMeta());
            }
        }

        return Map.copyOf(entries);
    }

    public record RenderEntry(int eggMeta,
                              String modelSourceStem,
                              ResourceLocation modelSourceLocation,
                              ResourceLocation textureLocation,
                              float shadowRadius,
                              boolean hostile) {
    }
}
