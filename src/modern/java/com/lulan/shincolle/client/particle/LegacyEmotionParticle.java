package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.ShinColle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.resources.ResourceLocation;

/** Renders the legacy type-28 emotion from the original sprite sheet. */
public final class LegacyEmotionParticle extends SingleQuadParticle {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/particles/ParticleEmotion.png");
    private static final int LIFETIME = 48;

    public LegacyEmotionParticle(ClientLevel level, double x, double y, double z,
                                 double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.setSize(0.0F, 0.0F);
        this.quadSize = 0.275F + this.random.nextFloat() * 0.05F;
        this.lifetime = LIFETIME;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.hasPhysics = false;
        this.setAlpha(0.0F);
    }

    @Override
    public void tick() {
        if (this.age < 5) {
            this.setAlpha((this.age + 1) * 0.2F);
        } else if (this.age < 45) {
            this.setAlpha(1.0F);
        } else {
            this.setAlpha(Math.max(0.0F, (LIFETIME - this.age) * 0.2F));
        }
        super.tick();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, TEXTURE);
        super.render(consumer, camera, partialTick);
        RenderSystem.disableBlend();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    protected float getU0() {
        return 0.75F;
    }

    @Override
    protected float getU1() {
        return 0.8125F;
    }

    @Override
    protected float getV0() {
        return 0.9375F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }
}
