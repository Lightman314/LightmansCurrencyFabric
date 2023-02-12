package io.github.lightman314.lightmanscurrency.common.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface EntityDeathCallback {

    Event<EntityDeathCallback> EVENT = EventFactory.createArrayBacked(EntityDeathCallback.class, (listeners) -> (entity, damageSource) -> {
        for(EntityDeathCallback listener : listeners)
            listener.onEntityDeath(entity, damageSource);
    });

    void onEntityDeath(LivingEntity entity, DamageSource damageSource);

}
