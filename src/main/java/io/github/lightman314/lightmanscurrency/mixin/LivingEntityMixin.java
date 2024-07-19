package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.common.callbacks.EntityDeathCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private LivingEntity self() { return (LivingEntity)(Object) this; }

    @Accessor("dead")
    protected abstract boolean isDeadInternally();

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource damageSource, CallbackInfo info) {
        LivingEntity self = this.self();
        if(!self.isRemoved() && !this.isDeadInternally())
            EntityDeathCallback.EVENT.invoker().onEntityDeath(self, damageSource);
    }

}
