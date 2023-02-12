package io.github.lightman314.lightmanscurrency.common.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface ItemEntityCollisionCallback {

    Event<ItemEntityCollisionCallback> EVENT = EventFactory.createArrayBacked(ItemEntityCollisionCallback.class, (listeners) -> (itemEntity, player) -> {
        for(ItemEntityCollisionCallback listener : listeners)
            listener.onItemPickup(itemEntity, player);
    });

    void onItemPickup(ItemEntity itemEntity, PlayerEntity player);

}
