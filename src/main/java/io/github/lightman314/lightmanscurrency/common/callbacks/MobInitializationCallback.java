package io.github.lightman314.lightmanscurrency.common.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public interface MobInitializationCallback {

    Event<MobInitializationCallback> EVENT = EventFactory.createArrayBacked(MobInitializationCallback.class, (listeners) -> (entity, world, difficulty, spawnReason, entityData, nbt) -> {
        for(MobInitializationCallback listener : listeners)
            listener.onMobInitialized(entity, world, difficulty, spawnReason, entityData, nbt);
    });

    void onMobInitialized(MobEntity entity, ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt);

}
