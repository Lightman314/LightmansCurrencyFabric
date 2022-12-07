package io.github.lightman314.lightmanscurrency.common.callbacks;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface EntityDeathCallback {

    public static final Event<EntityDeathCallback> EVENT = EventFactory.createArrayBacked(EntityDeathCallback.class, (listeners) -> (entity, damageSource) -> {
        for(EntityDeathCallback listener : listeners)
            listener.onEntityDeath(entity, damageSource);
    });

    void onEntityDeath(LivingEntity entity, DamageSource damageSource);

}
