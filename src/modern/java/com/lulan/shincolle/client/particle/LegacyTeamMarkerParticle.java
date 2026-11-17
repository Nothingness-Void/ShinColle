package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.ShinColle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Port of the legacy ParticleTeam entity marker used by ship state effects. */
public final class LegacyTeamMarkerParticle extends Particle {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/particles/ParticleTeam.png");
    private static final int LIFETIME = 30;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Entity host;
    private final int markerType;
    private final double hostHeight;
    private final float markerScale;
    private float arrowAlpha = 1.0F;
    private float circleAlpha = 0.8F;

    public LegacyTeamMarkerParticle(ClientLevel level, Entity host, float scale, int markerType) {
        super(level, host.getX(), host.getY(), host.getZ());
        this.host = host;
        this.markerType = markerType;
        this.hostHeight = host.getBbHeight();
        this.markerScale = scale;
        this.setSize(0.0F, 0.0F);
        this.setPos(host.getX(), host.getY(), host.getZ());
        this.setParticleSpeed(0.0D, 0.0D, 0.0D);
        this.hasPhysics = false;
        this.lifetime = LIFETIME;
        this.setMarkerColor(markerType);
    }

    private void setMarkerColor(int type) {
        switch (type) {
            case 1 -> this.setColor(0.0F, 1.0F, 1.0F);
            case 2 -> this.setColor(1.0F, 0.0F, 1.0F);
            case 3 -> this.setColor(1.0F, 0.9F, 0.0F);
            case 5 -> this.setColor(1.0F, 0.0F, 0.0F);
            case 6 -> this.setColor(1.0F, 1.0F, 1.0F);
            default -> this.setColor(0.0F, 1.0F, 0.0F);
        }
    }

    @Override
    public void tick() {
        if (this.host == null || !this.host.isAlive() || this.host.level() != this.level) {
            this.remove();
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setPos(this.host.getX(), this.host.getY(), this.host.getZ());

        if ((this.markerType == 4 || this.markerType == 5 || this.markerType == 6)
                && this.age > 10) {
            this.arrowAlpha = Math.max(0.0F, 1.0F - (this.age - 10.0F) / 20.0F);
            this.circleAlpha = this.arrowAlpha * 0.5F;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPosition = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.renderArrow(consumer, camera, x, y + (float) this.hostHeight + 1.3F, z);
        this.renderCircle(consumer, x, y + 0.3F, z);
        RenderSystem.disableBlend();
    }

    private void renderArrow(VertexConsumer consumer, Camera camera, float x, float y, float z) {
        Quaternionf rotation = new Quaternionf(camera.rotation());
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -2.0F, 0.0F),
                new Vector3f(-1.0F, 2.0F, 0.0F),
                new Vector3f(1.0F, 2.0F, 0.0F),
                new Vector3f(1.0F, -2.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation).mul(this.markerScale).add(x, y, z);
        }

        this.vertex(consumer, corners[0], 1.0F, 0.5F, this.arrowAlpha);
        this.vertex(consumer, corners[1], 1.0F, 0.0F, this.arrowAlpha);
        this.vertex(consumer, corners[2], 0.0F, 0.0F, this.arrowAlpha);
        this.vertex(consumer, corners[3], 0.0F, 0.5F, this.arrowAlpha);
    }

    private void renderCircle(VertexConsumer consumer, float x, float y, float z) {
        float halfScale = this.markerScale * 3.0F;
        this.vertex(consumer, x + halfScale, y, z + halfScale, 1.0F, 1.0F, this.circleAlpha);
        this.vertex(consumer, x + halfScale, y, z - halfScale, 1.0F, 0.5F, this.circleAlpha);
        this.vertex(consumer, x - halfScale, y, z - halfScale, 0.0F, 0.5F, this.circleAlpha);
        this.vertex(consumer, x - halfScale, y, z + halfScale, 0.0F, 1.0F, this.circleAlpha);

        this.vertex(consumer, x + halfScale, y, z - halfScale, 1.0F, 1.0F, this.circleAlpha);
        this.vertex(consumer, x + halfScale, y, z + halfScale, 1.0F, 0.5F, this.circleAlpha);
        this.vertex(consumer, x - halfScale, y, z + halfScale, 0.0F, 0.5F, this.circleAlpha);
        this.vertex(consumer, x - halfScale, y, z - halfScale, 0.0F, 1.0F, this.circleAlpha);
    }

    private void vertex(VertexConsumer consumer, Vector3f position,
                        float u, float v, float alpha) {
        this.vertex(consumer, position.x(), position.y(), position.z(), u, v, alpha);
    }

    private void vertex(VertexConsumer consumer, float x, float y, float z,
                        float u, float v, float alpha) {
        consumer.vertex(x, y, z)
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(FULL_BRIGHT)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
