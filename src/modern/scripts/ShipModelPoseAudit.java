import com.lulan.shincolle.client.model.legacy.compat.*;
import com.lulan.shincolle.client.model.legacy.ship.LegacyShipModels;
import com.lulan.shincolle.entity.ship.LegacyShipModelKind;
import com.lulan.shincolle.entity.ship.ShipEntitySpecs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

/** CPU-only vertex measurement; Y=0 is the entity/shadow plane, not an auto-grounding target. */
public final class ShipModelPoseAudit {
    private static final class Ship extends BasicEntityShip {
        final int state;
        final boolean sitting, noFuel, water;
        final RandomSource random = RandomSource.create(0);
        Ship(int state, boolean sitting, boolean noFuel, boolean water) {
            super(null);
            this.state = state;
            this.sitting = sitting;
            this.noFuel = noFuel;
            this.water = water;
        }
        public int getStateEmotion(int id) { return id == ID.S.State ? state : 0; }
        public void setStateEmotion(int id, int value, boolean sync) {}
        public int getStateMinor(int id) { return 0; }
        public boolean getStateFlag(int id) { return id == ID.F.NoFuel && noFuel; }
        public void setStateFlag(int id, boolean value) {}
        public int getFaceTick() { return 100000; }
        public int getHeadTiltTick() { return 0; }
        public int getAttackTick() { return 0; }
        public int getAttackTick2() { return 0; }
        public void setFaceTick(int value) {}
        public void setHeadTiltTick(int value) {}
        public void setAttackTick(int value) {}
        public void setAttackTick2(int value) {}
        public int getTickExisted() { return 100; }
        public float getSwingTime(float tick) { return 0; }
        public boolean getIsRiding() { return false; }
        public boolean getIsSprinting() { return false; }
        public boolean getIsSitting() { return sitting; }
        public boolean getIsSneaking() { return false; }
        public int getRidingState() { return 0; }
        public int getRiderType() { return 0; }
        public int getScaleLevel() { return 0; }
        public RandomSource getRand() { return random; }
        public RandomSource getRNG() { return random; }
        public double getShipDepth() { return water ? 0.1D : 0D; }
        public double getShipDepth(int type) { return type == 0 ? getShipDepth() : 0D; }
        public int getBrightnessForRender() { return 15728880; }
        public int getBrightnessForRender(float tick) { return 15728880; }
    }

    private static final class Bounds implements VertexConsumer {
        float minY = Float.POSITIVE_INFINITY, legY = Float.POSITIVE_INFINITY;
        boolean leg;
        int vertices, legVertices;
        public VertexConsumer addVertex(float x, float y, float z) {
            if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
                throw new AssertionError("Non-finite vertex");
            }
            minY = Math.min(minY, y);
            vertices++;
            if (leg) { legY = Math.min(legY, y); legVertices++; }
            return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
        public VertexConsumer vertex(double x, double y, double z) { return addVertex((float)x, (float)y, (float)z); }
        public VertexConsumer color(int r, int g, int b, int a) { return setColor(r, g, b, a); }
        public VertexConsumer uv(float u, float v) { return setUv(u, v); }
        public VertexConsumer overlayCoords(int u, int v) { return setUv1(u, v); }
        public VertexConsumer uv2(int u, int v) { return setUv2(u, v); }
        public VertexConsumer normal(float x, float y, float z) { return setNormal(x, y, z); }
        public void endVertex() {}
        public void defaultColor(int r, int g, int b, int a) {}
        public void unsetDefaultColor() {}
    }

    private static void tagLegs(ModelBase model, Bounds bounds) throws ReflectiveOperationException {
        Map<ModelRenderer, Boolean> legs = new IdentityHashMap<>();
        for (var field : model.getClass().getFields()) {
            String name = field.getName().toLowerCase(Locale.ROOT);
            if (field.getType() == ModelRenderer.class && !name.contains("equip")
                    && (name.contains("leg") || name.contains("foot") || name.contains("feet"))) {
                legs.put((ModelRenderer) field.get(model), true);
            }
        }
        for (var part : model.boxList) {
            for (int i = 0; i < part.cubeList.size(); i++) {
                ModelBox original = part.cubeList.get(i);
                boolean isLeg = legs.containsKey(part);
                part.cubeList.set(i, new ModelBox(part, 0, 0, 0, 0, 0, 0, 0, 0, 0) {
                    @Override public void render(float scale) {
                        bounds.leg = isLeg;
                        original.render(scale);
                    }
                });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("egg,model,state,sitting,noFuel,inWater,lowestVisibleY,lowestLegY,vertices,legVertices");
        int cases = 0;
        for (var spec : ShipEntitySpecs.values()) {
            var kind = LegacyShipModelKind.forSpec(spec);
            for (int state : new int[] {0, 16, 255}) {
                for (boolean sitting : new boolean[] {false, true}) {
                    for (boolean noFuel : new boolean[] {false, true}) {
                      for (boolean water : new boolean[] {false, true}) {
                        var model = LegacyShipModels.create(kind);
                        model.isChild = false;
                        var ship = new Ship(state, sitting, noFuel, water);
                        var bounds = new Bounds();
                        tagLegs(model, bounds);
                        var poses = new PoseStack();
                        poses.scale(-1, -1, 1);
                        poses.translate(0, -1.501F, 0);
                        LegacyRenderContext.begin(poses, bounds, 15728880, 0, 1, 1, 1, 1);
                        try {
                            model.setRotationAngles(0, 0, 100, 0, 0, 0.0625F, ship);
                            model.render(ship, 0, 0, 100, 0, 0, 0.0625F);
                        } finally {
                            LegacyRenderContext.end();
                        }
                        if (bounds.vertices == 0) throw new AssertionError("Empty model: " + kind);
                        if (kind == LegacyShipModelKind.ModelDestroyerHime && !sitting && !noFuel) {
                            if (state == 0 && water && !(bounds.legVertices == 0 && bounds.minY > 0.4F && bounds.minY < 0.45F)) {
                                throw new AssertionError("Legacy no-leg hovering pose changed");
                            }
                            if (!water && !(bounds.legVertices > 0 && Math.abs(bounds.legY) < 0.03F)) {
                                throw new AssertionError("Land legs no longer meet the ground");
                            }
                            if ((state & 16) != 0 && bounds.legVertices == 0) {
                                throw new AssertionError("Explicit legs must remain visible in water and on land");
                            }
                        }
                        System.out.printf(Locale.ROOT, "%d,%s,%d,%s,%s,%s,%.6f,%s,%d,%d%n", spec.eggMeta(), kind,
                                state, sitting, noFuel, water, bounds.minY,
                                bounds.legVertices == 0 ? "hidden_or_unnamed" : String.format(Locale.ROOT, "%.6f", bounds.legY),
                                bounds.vertices, bounds.legVertices);
                        cases++;
                      }
                    }
                }
            }
        }
        System.err.println("Measured " + cases + " model/state/pose cases at shadow plane Y=0");
    }
}
