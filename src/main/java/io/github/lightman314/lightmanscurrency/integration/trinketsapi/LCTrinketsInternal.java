package io.github.lightman314.lightmanscurrency.integration.trinketsapi;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LCTrinketsInternal {

    public static ItemStack findMoneyMendingTrinket(@NotNull LivingEntity player)
    {
        AtomicReference<ItemStack> result = new AtomicReference<>(null);
        TrinketsApi.getTrinketComponent(player).ifPresent(trinket -> {
            //Get list of damaged items
            List<Pair<SlotReference, ItemStack>> needsRepair = trinket.getEquipped(ItemStack::isDamaged);
            if(!needsRepair.isEmpty())
            {
                //Get random item from the list
                int random = player.getRandom().nextInt(needsRepair.size());
                result.set(needsRepair.get(random).getRight());
            }
        });
        return result.get();
    }

    public static boolean hasWalletSlot(@NotNull LivingEntity player) {
        AtomicBoolean valid = new AtomicBoolean(false);
        TrinketsApi.getTrinketComponent(player).ifPresent(trinket -> {
            if(trinket.getInventory().containsKey("legs"))
            {
                Map<String, TrinketInventory> legInventory = trinket.getInventory().get("legs");
                if(legInventory.containsKey("wallet"))
                    valid.set(legInventory.get("wallet").size() > 0);
            }
        });
        return valid.get();
    }

    public static ItemStack getWallet(@NotNull LivingEntity player)
    {
        AtomicReference<ItemStack> result = new AtomicReference<>(ItemStack.EMPTY);
        TrinketsApi.getTrinketComponent(player).ifPresent(trinket -> {
            if(trinket.getInventory().containsKey("legs"))
            {
                Map<String, TrinketInventory> legInventory = trinket.getInventory().get("legs");
                if(legInventory.containsKey("wallet"))
                {
                    TrinketInventory slots = legInventory.get("wallet");
                    if(slots.size() > 0)
                        result.set(slots.getStack(0));
                }
            }
        });
        return result.get();
    }

    public static boolean setWallet(@NotNull LivingEntity player, @NotNull ItemStack wallet)
    {
        AtomicBoolean success = new AtomicBoolean(false);
        TrinketsApi.getTrinketComponent(player).ifPresent(trinket -> {
            if(trinket.getInventory().containsKey("legs"))
            {
                Map<String, TrinketInventory> legInventory = trinket.getInventory().get("legs");
                if(legInventory.containsKey("wallet"))
                {
                    TrinketInventory slots = legInventory.get("wallet");
                    if(slots.size() > 0)
                    {
                        //Only replace the wallet item if no item is already there
                        if(slots.getStack(0).isEmpty())
                        {
                            slots.setStack(0, wallet);
                            success.set(true);
                        }
                    }
                }
            }
        });
        return success.get();
    }

}