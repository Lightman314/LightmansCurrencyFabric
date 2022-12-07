package io.github.lightman314.lightmanscurrency.common.loot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class EntityLootBlocker extends PersistentState {


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return nbt;
    }

    public static boolean BlockEntityDrops(LivingEntity entity) {
        return false;
    }

}
