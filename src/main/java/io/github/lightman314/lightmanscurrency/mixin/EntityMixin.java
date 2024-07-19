package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.common.loot.EntityLootBlocker;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    private Entity self() { return (Entity)(Object) this; }

    @Inject(at = @At("HEAD"), method = "setRemoved")
    private void setRemoved(Entity.RemovalReason reason, CallbackInfo info)
    {
        EntityLootBlocker.StopTrackingEntity(this.self());
    }

}
