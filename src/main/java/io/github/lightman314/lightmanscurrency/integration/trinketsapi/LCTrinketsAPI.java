package io.github.lightman314.lightmanscurrency.integration.trinketsapi;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LCTrinketsAPI {

    public static boolean isActive() { return FabricLoader.getInstance().isModLoaded("trinkets"); }

    @Nullable
    public static ItemStack findMoneyMendingItem(@NotNull PlayerEntity player)
    {
        if(!isActive())
            return null;
        return LCTrinketsInternal.findMoneyMendingTrinket(player);
    }

}