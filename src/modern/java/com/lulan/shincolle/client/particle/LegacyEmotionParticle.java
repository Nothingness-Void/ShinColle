package com.lulan.shincolle.client.particle;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.client.model.legacy.compat.Values;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class LegacyEmotionParticle extends LegacyBillboardParticle {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShinColle.MOD_ID, "textures/particles/particleemotion.png");
    public static final int EMO_NUMBER = 30;

    private Entity host = null;
    private int particleType, playTimes, fadeTick, fadeState, stayTick, stayTickCount, frameSize;
    private float playSpeed, playSpeedCount, particleIconX, particleIconY, addHeight, entType;
    private float[] spawnRange;
    private double px, py, pz, addx, addy, addz;


    public LegacyEmotionParticle(ClientLevel world, Entity host, double posX, double posY, double posZ, float height, int entType, int type)
    {
        super(world, posX, posY, posZ);
        this.host = host;
        this.setSize(0F, 0F);
        this.setPos(posX, posY, posZ);
        this.xo = posX;
        this.yo = posY;
        this.zo = posZ;
        this.xd = 0D;
        this.zd = 0D;
        this.yd = 0D;
        this.particleType = type;
        this.quadSize = this.random.nextFloat() * 0.05F + 0.275F;
        this.alpha = 0F;
        this.playSpeed = 1F;
        this.playSpeedCount = 0F;
        this.stayTick = 10;
        this.stayTickCount = 0;
        this.fadeTick = 0;
        this.fadeState = 0;  //0:fade in, 1:normal, 2:fade out, 3:set dead
        this.frameSize = 1;
        this.addHeight = height;
        this.entType = entType;  //0:any entity, 1:entity, 2:block
        this.age = -1;  //prevent showing the emo's initial moving from posY = 0
        this.hasPhysics = false;

        //set icon position
        switch(this.particleType)
        {
        case 1:   //小愛心
            this.particleIconX = 0.0625F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 4;
            //no stay
            this.stayTick = 0;
            break;
        case 2:   //噴汗
            this.particleIconX = 0.0625F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 3;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 5;
            //no stay
            this.stayTick = 0;
            break;
        case 3:   //問號
            this.particleIconX = 0.125F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            break;
        case 4:   //驚嘆號
            this.particleIconX = 0.125F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //long stay
            this.stayTick = 20;
            break;
        case 5:   //點點點
            this.particleIconX = 0.1875F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //long stay
            this.stayTick = 20;
            //slow play
            this.playSpeed = 0.5F;
            break;
        case 6:   //冒青筋
            this.particleIconX = 0.1875F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            break;
        case 7:   //音符
            this.particleIconX = 0.25F;
            this.particleIconY = 0F;
            this.lifetime = 15;
            this.playTimes = 1;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //short stay
            this.stayTick = 3;
            //slow play
            this.playSpeed = 0.7F;
            break;
        case 8:   //cry
            this.particleIconX = 0.3125F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 3;
            //short fade in
            this.fadeTick = 3;
            //no stay
            this.stayTick = 0;
            //slow play
            this.playSpeed = 0.5F;
            break;
        case 9:   //流口水
            this.particleIconX = 0.3125F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 2;
            //short fade in
            this.fadeTick = 3;
            //no stay
            this.stayTick = 1;
            //slow play
            this.playSpeed = 0.5F;
            break;
        case 10:  //混亂
            this.particleIconX = 0.375F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 4;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //short stay
            this.stayTick = 1;
            break;
        case 11:  //尋找
            this.particleIconX = 0.375F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 2;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //short stay
            this.stayTick = 0;
            //slow play
            this.playSpeed = 0.75F;
            break;
        case 12:  //驚嚇
            this.particleIconX = 0.4375F;
            this.particleIconY = 0F;
            this.lifetime = 14;
            this.playTimes = 1;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //long stay
            this.stayTick = 20;
            //slow play
            this.playSpeed = 0.75F;
            //large frame
            this.frameSize = 2;
            break;
        case 13:  //點頭
            this.particleIconX = 0.5F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 2;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //no stay
            this.stayTick = 0;
            //slow play
            this.playSpeed = 0.75F;
            break;
        case 14:  //+_+
            this.particleIconX = 0.5F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 2;
            //short fade in
            this.fadeTick = 3;
            //no stay
            this.stayTick = 0;
            break;
        case 15:  //kiss
            this.particleIconX = 0.5625F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //long stay
            this.stayTick = 15;
            //slow play
            this.playSpeed = 0.7F;
            break;
        case 16:  //lol
            this.particleIconX = 0.5625F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 3;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //no stay
            this.stayTick = 0;
            break;
        case 17:  //奸笑
            this.particleIconX = 0.625F;
            this.particleIconY = 0F;
            this.lifetime = 15;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //slow play
            this.playSpeed = 0.5F;
            break;
        case 18:  //殘念
            this.particleIconX = 0.6875F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //no stay
            this.stayTick = 0;
            //slow play
            this.playSpeed = 0.4F;
            break;
        case 19:  //舔舔
            this.particleIconX = 0.6875F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 3;
            //cancel fade in
            this.alpha = 1F;
            this.fadeState = 1;
            this.fadeTick = 3;
            //no stay
            this.stayTick = 0;
            //slow play
            this.playSpeed = 0.75F;
            break;
        case 20:  //orz
            this.particleIconX = 0.75F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //long stay
            this.stayTick = 20;
            //slow play
            this.playSpeed = 0.5F;
            break;
        case 21:  //O
            this.particleIconX = 0.75F;
            this.particleIconY = 0.5F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 22:  //X
            this.particleIconX = 0.75F;
            this.particleIconY = 0.5625F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 23:  //!?
            this.particleIconX = 0.75F;
            this.particleIconY = 0.625F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 24:  //rock
            this.particleIconX = 0.75F;
            this.particleIconY = 0.6875F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 25:  //paper
            this.particleIconX = 0.75F;
            this.particleIconY = 0.75F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 26:  //scissors
            this.particleIconX = 0.75F;
            this.particleIconY = 0.8125F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 27:  //-w-
            this.particleIconX = 0.75F;
            this.particleIconY = 0.875F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 28:  //-口-
            this.particleIconX = 0.75F;
            this.particleIconY = 0.9375F;
            this.lifetime = 0;
            this.playTimes = 1;
            //long stay
            this.stayTick = 40;
            break;
        case 29:  //blink
            this.particleIconX = 0.8125F;
            this.particleIconY = 0F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //slow play
            this.playSpeed = 0.35F;
            //long stay
            this.stayTick = 20;
            break;
        case 30:  //哼
            this.particleIconX = 0.8125F;
            this.particleIconY = 0.5F;
            this.lifetime = 7;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //slow play
            this.playSpeed = 0.75F;
            //short stay
            this.stayTick = 3;
            break;
        case 31:  //臉紅紅
            this.particleIconX = 0.875F;
            this.particleIconY = 0F;
            this.lifetime = 3;
            this.quadSize += 0.2F;
            this.playTimes = 1;
            //short fade in
            this.fadeTick = 3;
            //slow play
            this.playSpeed = 0.75F;
            //long stay
            this.stayTick = 30;
            break;
        case 32:  //尷尬
            this.particleIconX = 0.875F;
            this.particleIconY = 0.25F;
            this.lifetime = 5;
            this.playTimes = 4;
            //slow play
            this.playSpeed = 0.75F;
            //no stay
            this.stayTick = 0;
            break;
        case 33:  //:P
            this.particleIconX = 0.875F;
            this.particleIconY = 0.625F;
            this.lifetime = 4;
            this.playTimes = 1;
            //slow play
            this.playSpeed = 0.25F;
            //long stay
            this.stayTick = 30;
            break;
        case 34:  //|||
            this.particleIconX = 0.875F;
            this.particleIconY = 0.9375F;
            this.lifetime = 0;
            this.quadSize += 0.3F;
            this.playTimes = 1;
            //long stay
            this.stayTick = 50;
            break;
        default:  //汗
            this.particleIconX = 0F;
            this.particleIconY = 0F;
            this.lifetime = 15;
            this.playTimes = 1;
            break;
        }

        //init position
        this.px = posX;
        this.py = posY;
        this.pz = posZ;
        this.addx = 0D;
        this.addy = 0D;
        this.addz = 0D;

        calcParticlePosition();
    }

@Override
public void tick()
    {
        //update pos
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if(host != null)
        {
            updateHostPosition();
        }

        //fade state
        switch (this.fadeState)
        {
        case 0:   //fade in
            this.fadeTick++;
            this.alpha = this.fadeTick * 0.2F;

            if (this.fadeTick > 5) this.fadeState = 1;
            break;
        case 1:   //age++
            this.playSpeedCount += this.playSpeed;
            this.age = this.frameSize * (int)this.playSpeedCount;
            this.alpha = 1F;
            break;
        case 2:   //fade out
            this.fadeTick--;
            this.alpha = this.fadeTick * 0.2F;

            if (this.fadeTick < 1)
            {
                this.remove();
                return;
            }
            break;
        default:
            this.remove();
            return;
        }

        //stay at last frame
        if (this.age >= lifetime)
        {
            this.age = this.lifetime;

            //count stay ticks
            if (this.stayTickCount > this.stayTick)
            {
                this.age = this.lifetime + 1;  //next loop flag
                this.stayTickCount = 0;
            }
            else
            {
                this.stayTickCount += 1;
            }
        }

        //loop play
        if (this.age > this.lifetime)
        {
            //loop times--
            if (--this.playTimes <= 0)
            {
                this.fadeState = 2;  //change to fade out
            }
            else
            {
                this.age = 0;
                this.playSpeedCount = 0F;
            }
        }
    }

private void updateHostPosition()
    {
        //get host position
        if (this.host != null)
        {
            this.x = this.host.getX() + addx;
            this.y = this.host.getY() + addy;
            this.z = this.host.getZ() + addz;
        }
    }

private void calcParticlePosition()
    {
        //get host position
        if (this.host != null)
        {
            this.px = this.host.getX();
            this.py = this.host.getY();
            this.pz = this.host.getZ();
        }

        //get player view angle
        float angle = Minecraft.getInstance().player.yBodyRot % 360 * Values.N.DIV_PI_180;
        float[] newPos;

        //tweak emote position by entity type
        if (entType == 1)  //entity type
        {
            //replace emotes into player's view cone
            float frontDist = 0.7F;
            float leftDist = -0.2F;

            switch (this.particleType)
            {
            case 12:  //omg
                leftDist = 0F;
                addy += 0.6D;
                break;
            case 15:  //kiss
                frontDist = 1.5F;
                leftDist = -0.7F;
                break;
            case 19:  //lick
                frontDist = 1.4F;
                leftDist = -1.1F;
                break;
            case 34:  //lll
                frontDist = -0.2F;
                leftDist = 0F;
                addy -= 0.2D;
                break;
            }

            newPos = rotateXZByAxis(frontDist, leftDist, angle, 1F);
            addx += newPos[1];
            addy -= 0.2D;
            addz += newPos[0];
        }
        else  //block type
        {
            newPos = rotateXZByAxis(0F, -0.2F, angle, 1F);
            addx += newPos[1];
            addy += 0.5D;
            addz += newPos[0];
        }


        //enlarge if boss entity
        float addx2 = 0F;
        float addy2 = 0F;
        float addz2 = 0F;

        if (this.addHeight > 2F)
        {
            this.quadSize += 1F;
            addx2 = 1.2F;
            addy2 = 1.5F;
            addz2 = 0.5F;
        }

        //set particle position
        switch (this.particleType)
        {
        case 2:  //right side
            newPos = rotateXZByAxis(-0.2F - addz2, this.random.nextFloat() * 0.3F - 1F - addx2, angle, 1F);
            addx = addx + newPos[1];
            addy = addy + this.random.nextDouble() * this.addHeight * 0.2D + this.addHeight * 1.8D + addy2;
            addz = addz + newPos[0];
            break;
        case 15: //front
            newPos = rotateXZByAxis(this.random.nextFloat() * 0.1F - 0.7F - addx2, this.random.nextFloat() * 0.1F + 0.2F + addz2, angle, 1F);
            addx = addx + newPos[1];
            addy = addy + this.random.nextDouble() * this.addHeight * 0.2D + this.addHeight * 1.6D + addy2;
            addz = addz + newPos[0];
            break;
        case 34: //top
            newPos = rotateXZByAxis(0.15F, 0F, angle, 1F);
            addx = addx + newPos[1];
            addy = addy + this.random.nextDouble() * this.addHeight * 0.15D + this.addHeight * 1.9D + addy2;
            addz = addz + newPos[0];
            break;
        default: //left side
            newPos = rotateXZByAxis(-0.4F - addz2, this.random.nextFloat() * 0.3F + 0.7F + addx2, angle, 1F);
            addx = addx + newPos[1];
            addy = addy + this.random.nextDouble() * this.addHeight * 0.5D + this.addHeight * 1.5D + addy2;
            addz = addz + newPos[0];
            break;
        }

        //set position
        this.setPos(px + addx, py + addy, pz + addz);

    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (age < 0) return;
        int frame = age > lifetime ? lifetime : age;
        float v0 = particleIconY + frame * 0.0625F;
        drawQuad(camera, partialTick, TEXTURE, particleIconX, particleIconX + 0.0625F,
                v0, v0 + 0.0625F * frameSize, quadSize, quadSize * frameSize, true, false);
    }

    private static float[] rotateXZByAxis(float z, float x, float rad, float scale) {
        float cosD = Mth.cos(rad);
        float sinD = Mth.sin(rad);
        return new float[] {(z * cosD + x * sinD) * scale, (x * cosD - z * sinD) * scale};
    }
}
