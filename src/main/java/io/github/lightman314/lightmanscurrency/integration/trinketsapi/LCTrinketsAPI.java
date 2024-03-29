package io.github.lightman314.lightmanscurrency.integration.trinketsapi;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LCTrinketsAPI {

    public static boolean isActive() { return FabricLoader.getInstance().isModLoaded("trinkets"); }
    public static boolean isValid(@Nullable LivingEntity player) { return player != null && isActive() && LCTrinketsInternal.hasWalletSlot(player); }

    @Nullable
    public static ItemStack findMoneyMendingItem(@NotNull LivingEntity player)
    {
        if(!isActive())
            return null;
        return LCTrinketsInternal.findMoneyMendingTrinket(player);
    }

    @NotNull
    public static ItemStack getWallet(@NotNull LivingEntity player)
    {
        if(!isActive())
            return ItemStack.EMPTY;
        return LCTrinketsInternal.getWallet(player);
    }

    public static boolean setWallet(@NotNull LivingEntity player, @NotNull ItemStack wallet)
    {
        if(!isValid(player))
            return false;
        return LCTrinketsInternal.setWallet(player, wallet);
    }



}