package com.lulan.shincolle.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

abstract class LegacyBillboardParticle extends Particle {
    protected float quadSize;

    protected LegacyBillboardParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(float partialTick) {
        return net.minecraft.world.phys.AABB.INFINITE;
    }

    protected void drawQuad(Camera camera, float partialTick, ResourceLocation texture,
                            float u0, float u1, float v0, float v1, float width, float height,
                            boolean doubleSided, boolean upright) {
        float yaw = camera.getYRot() * 0.017453292F;
        float pitch = camera.getXRot() * 0.017453292F;
        float rotX = Mth.cos(yaw);
        float rotZ = Mth.cos(pitch);
        float rotYZ = Mth.sin(yaw);
        float rotXY = upright ? 0F : -rotYZ * Mth.sin(pitch);
        float rotXZ = upright ? 0F : rotX * Mth.sin(pitch);
        float px = (float)(xo + (x - xo) * partialTick - camera.getPosition().x);
        float py = (float)(yo + (y - yo) * partialTick - camera.getPosition().y);
        float pz = (float)(zo + (z - zo) * partialTick - camera.getPosition().z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        vertex(buffer, px - rotX * width - rotXY * width, py - rotZ * height,
                pz - rotYZ * width - rotXZ * width, u1, v1);
        vertex(buffer, px - rotX * width + rotXY * width, py + rotZ * height,
                pz - rotYZ * width + rotXZ * width, u1, v0);
        vertex(buffer, px + rotX * width + rotXY * width, py + rotZ * height,
                pz + rotYZ * width + rotXZ * width, u0, v0);
        vertex(buffer, px + rotX * width - rotXY * width, py - rotZ * height,
                pz + rotYZ * width - rotXZ * width, u0, v1);
        if (doubleSided) {
            vertex(buffer, px + rotX * width - rotXY * width, py - rotZ * height,
                    pz + rotYZ * width - rotXZ * width, u0, v1);
            vertex(buffer, px + rotX * width + rotXY * width, py + rotZ * height,
                    pz + rotYZ * width + rotXZ * width, u0, v0);
            vertex(buffer, px - rotX * width + rotXY * width, py + rotZ * height,
                    pz - rotYZ * width + rotXZ * width, u1, v0);
            vertex(buffer, px - rotX * width - rotXY * width, py - rotZ * height,
                    pz - rotYZ * width - rotXZ * width, u1, v1);
        }
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);
    }

    private void vertex(BufferBuilder buffer, float x, float y, float z, float u, float v) {
        buffer.addVertex(x, y, z).setUv(u, v).setColor(1F, 1F, 1F, alpha).setUv2(240, 240);
    }
}
