package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.common.callbacks.ItemEntityCollisionCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Unique
    private ItemEntity self() { return (ItemEntity)(Object) this; }

    @Inject(at = @At("HEAD"), method = "onPlayerCollision")
    public void onPlayerCollision(PlayerEntity player, CallbackInfo info) {
        ItemEntity self = this.self();
        if(!self.getWorld().isClient && !self.isRemoved() && !self.cannotPickup())
            ItemEntityCollisionCallback.EVENT.invoker().onItemPickup(self, player);
    }
}
