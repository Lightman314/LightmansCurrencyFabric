package io.github.lightman314.lightmanscurrency.common.crafting;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.enchantments.CoinMagnetEnchantment;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class WalletMagnetUpgradeRecipe extends SpecialCraftingRecipe {

    public static final Identifier TAG_UPGRADE_ITEM = new Identifier(LightmansCurrency.MODID,"coin_magnet_upgrade_item");

    public WalletMagnetUpgradeRecipe(Identifier id, CraftingRecipeCategory category) { super(id, category); }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        Optional<ItemStack> optional = findWallet(inventory);
        if(optional.isEmpty())
            return false;
        ItemStack walletStack = optional.get();
        int currentLevel = WalletItem.GetMagnetLevel(walletStack);
        int pearlCount = upgradeItemCount(inventory);
        return pearlCount > 0 && currentLevel + pearlCount <= CoinMagnetEnchantment.MAX_LEVEL;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        Optional<ItemStack> optional = findWallet(inventory);
        if(optional.isEmpty())
            return ItemStack.EMPTY;
        ItemStack walletCopy = optional.get().copy();
        walletCopy.setCount(1);
        int oldLevel = WalletItem.GetMagnetLevel(walletCopy);
        int pearlCount = upgradeItemCount(inventory);
        WalletItem.SetMagnetLevel(walletCopy, oldLevel + pearlCount);
        return walletCopy;
    }

    private Optional<ItemStack> findWallet(RecipeInputInventory inventory)
    {
        Optional<ItemStack> result = Optional.empty();
        for(int i = 0; i < inventory.size(); ++i)
        {
            ItemStack stack = inventory.getStack(i);
            if(stack.getItem() instanceof WalletItem wallet)
            {
                if(WalletItem.CanPickup(wallet))
                {
                    if(result.isPresent()) //Fail if multiple wallets present
                        return Optional.empty();
                    result = Optional.of(stack);
                }
                else
                    return Optional.empty();
            }
        }
        return result;
    }

    private int upgradeItemCount(RecipeInputInventory inventory)
    {
        int count = 0;
        for(int i = 0; i < inventory.size(); ++i)
        {
            ItemStack stack = inventory.getStack(i);
            if(InventoryUtil.ItemHasTag(stack, TAG_UPGRADE_ITEM))
                count++;
            else if(!stack.isEmpty() && !WalletItem.isWallet(stack)) //Fail if a non-wallet & non-ender pearl item is present.
                return 0;
        }
        return count;
    }

    @Override
    public boolean fits(int width, int height) { return width * height > 2; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.WALLET_MAGNET_UPGRADE; }

}
