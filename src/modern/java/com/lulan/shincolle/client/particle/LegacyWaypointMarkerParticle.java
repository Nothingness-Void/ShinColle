package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.ShinColle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Legacy ParticleTeam's block-bound type 9, without its transparent arrow. */
public final class LegacyWaypointMarkerParticle extends LegacyBillboardParticle {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/particles/particleteam.png");
    private static final float HALF_SIZE = 0.2F * 3F;

    public LegacyWaypointMarkerParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setSize(0F, 0F);
        this.setParticleSpeed(0D, 0D, 0D);
        this.hasPhysics = false;
        this.lifetime = 31;
        this.alpha = 0.9F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setPos(this.x, this.y + this.age * 0.002D, this.z);
        this.alpha = 0.9F - this.age * 0.027F;
        if (this.age++ > this.lifetime) this.remove();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y + 0.3D);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        vertex(buffer, x + HALF_SIZE, y, z + HALF_SIZE, 1F, 1F);
        vertex(buffer, x + HALF_SIZE, y, z - HALF_SIZE, 1F, 0.5F);
        vertex(buffer, x - HALF_SIZE, y, z - HALF_SIZE, 0F, 0.5F);
        vertex(buffer, x - HALF_SIZE, y, z + HALF_SIZE, 0F, 1F);
        vertex(buffer, x + HALF_SIZE, y, z - HALF_SIZE, 1F, 1F);
        vertex(buffer, x + HALF_SIZE, y, z + HALF_SIZE, 1F, 0.5F);
        vertex(buffer, x - HALF_SIZE, y, z + HALF_SIZE, 0F, 0.5F);
        vertex(buffer, x - HALF_SIZE, y, z - HALF_SIZE, 0F, 1F);
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);
    }

    private void vertex(BufferBuilder buffer, float x, float y, float z, float u, float v) {
        buffer.vertex(x, y, z).uv(u, v).color(1F, 0F, 0F, this.alpha).uv2(240, 240).endVertex();
    }
}
