package com.lulan.shincolle.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class LegacyRepairGoddessParticle extends Particle {

    private final Entity host;
    private final float[][] beams;
    private final int emissionCount;
    private int nextBeam;

    public LegacyRepairGoddessParticle(ClientLevel level, Entity host) {
        super(level, host.getX(), host.getY(), host.getZ());
        this.host = host;
        int setting = Minecraft.getInstance().options.particles().get().getId();
        this.beams = new float[(3 - setting) * 30][8];
        this.emissionCount = 4 - setting;
        this.lifetime = 120;
        this.setSize(0F, 0F);
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        if (this.age++ > this.lifetime || this.host.isRemoved()) {
            this.remove();
            return;
        }
        float radius = this.host.getBbWidth() * 2F;
        for (int i = 0; i < this.emissionCount; i++) {
            this.beams[this.nextBeam] = new float[] {
                    (float) (this.host.getX() - this.x) + (this.random.nextFloat() - 0.5F) * radius,
                    (float) (this.host.getY() - this.y) + this.host.getBbHeight() * 0.4F + (this.random.nextFloat() - 0.5F) * radius,
                    (float) (this.host.getZ() - this.z) + (this.random.nextFloat() - 0.5F) * radius,
                    0.5F + this.random.nextFloat() * 1.2F, 0.001F, 0.001F, 1F, 0F
            };
            this.nextBeam = (this.nextBeam + 1) % this.beams.length;
        }
        for (float[] beam : this.beams) {
            beam[1] += 0.03F;
            beam[7]++;
            if (beam[3] != 0F) {
                beam[6] = Math.min(1F, this.random.nextFloat() + 0.1F);
            }
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Vec3 cameraPos = camera.getPosition();
        for (float[] beam : this.beams) {
            if (beam[3] == 0F) {
                continue;
            }
            float size = 0.1F * (20F - beam[7]) * 0.05F;
            for (int i = 0; i < 4; i++) {
                Vector3f corner = new Vector3f(i < 2 ? -size : size, i == 0 || i == 3 ? -size : size, 0F);
                corner.rotate(camera.rotation());
                buffer.vertex(this.x - cameraPos.x + beam[0] + corner.x(),
                        this.y - cameraPos.y + beam[1] + corner.y(),
                        this.z - cameraPos.z + beam[2] + corner.z())
                        .color(beam[3], beam[4], beam[5], beam[6]).endVertex();
            }
        }
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
}
