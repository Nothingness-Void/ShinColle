package com.lulan.shincolle.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;

public final class LegacyFallingBlock extends FallingBlock {
    public LegacyFallingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LegacyFallingBlock> codec() {
        return simpleCodec(LegacyFallingBlock::new);
    }
}
