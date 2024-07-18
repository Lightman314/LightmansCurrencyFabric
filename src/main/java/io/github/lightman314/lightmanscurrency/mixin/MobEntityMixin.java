package io.github.lightman314.lightmanscurrency.mixin;

import io.github.lightman314.lightmanscurrency.common.callbacks.MobInitializationCallback;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Unique
    private MobEntity self() { return (MobEntity)(Object) this; }

    @Inject(at = @At("HEAD"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> info)
    {
        try{ MobInitializationCallback.EVENT.invoker().onMobInitialized(self(), world, difficulty, spawnReason, entityData, entityNbt);
        } catch(Throwable ignored) { }
    }

}
