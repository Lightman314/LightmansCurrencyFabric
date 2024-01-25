package io.github.lightman314.lightmanscurrency.integration.trinketsapi;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LCTrinketsInternal {

    public static ItemStack findMoneyMendingTrinket(@NotNull PlayerEntity player)
    {
        AtomicReference<ItemStack> result = new AtomicReference<>(null);
        TrinketsApi.getTrinketComponent(player).ifPresent(trinket -> {
            //Get list of damaged items
            List<Pair<SlotReference, ItemStack>> needsRepair = trinket.getEquipped(ItemStack::isDamaged);
            if(needsRepair.size() > 0)
            {
                //Get random item from the list
                int random = player.getRandom().nextInt(needsRepair.size());
                result.set(needsRepair.get(random).getRight());
            }
        });
        return result.get();
    }

}
