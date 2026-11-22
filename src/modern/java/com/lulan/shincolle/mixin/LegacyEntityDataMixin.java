package com.lulan.shincolle.mixin;

import com.lulan.shincolle.entity.ship.LegacyShipEntity;
import com.lulan.shincolle.entity.other.RecoveredShipItemEntity;
import com.lulan.shincolle.item.LegacyItemData;
import com.lulan.shincolle.teitoku.TeitokuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LegacyEntityDataMixin {
    @Inject(method = "load", at = @At("HEAD"))
    private void shincolle$upgradeNestedItems(CompoundTag tag, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player || entity instanceof LegacyShipEntity || entity instanceof RecoveredShipItemEntity) {
            LegacyItemData.upgradeNested(entity.registryAccess(), tag);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void shincolle$importForgeCapability(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof Player player
                && !tag.getCompound("neoforge:attachments").contains("shincolle:teitoku", 10)
                && tag.getCompound("ForgeCaps").contains("shincolle:teitoku", 10)) {
            TeitokuHelper.get(player).orElseThrow().loadFromTag(tag.getCompound("ForgeCaps").getCompound("shincolle:teitoku"));
        }
    }
}
