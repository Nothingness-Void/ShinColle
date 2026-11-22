package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.ShinColle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public final class LegacyTextParticle extends LegacyBillboardParticle {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/particles/particletexts.png");
    private final int particleType;

    public LegacyTextParticle(ClientLevel level, double x, double y, double z, float scale, int type) {
        super(level, x, y, z);
        this.setSize(0F, 0F);
        this.xd = 0D;
        this.zd = 0D;
        this.yd = 0.1D;
        this.quadSize = scale;
        this.lifetime = 25;
        this.particleType = type;
        this.hasPhysics = false;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        drawQuad(camera, partialTick, TEXTURE, 0F, 1F,
                particleType / 5F, (particleType + 1F) / 5F, 0.8F, 0.2F, false, true);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ > this.lifetime) this.remove();
        this.move(this.xd, this.yd, this.zd);
        this.yd *= 0.9D;
    }
}
