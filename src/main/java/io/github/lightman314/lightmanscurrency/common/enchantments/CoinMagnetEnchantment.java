package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Due to no EnchantmentTarget API, the Coin Magnet enchantment is being hard-coded into the wallets.
 * Can be added to the wallet via the new {@link io.github.lightman314.lightmanscurrency.common.crafting.WalletMagnetUpgradeRecipe} recipe.
 */
public final class CoinMagnetEnchantment {

    private CoinMagnetEnchantment() {}

    //

    //Max magnet level
    public static final int MAX_LEVEL = 3;
    //Max level to calculate range for
    public static final int MAX_CALCULATION_LEVEL = MAX_LEVEL + 2;

    public static void runPlayerTick(ServerPlayerEntity player) {
        WalletHandler walletHandler = WalletHandler.getWallet(player);
        ItemStack wallet = walletHandler.getWallet();
        //Don't do anything if the stack is not a waller
        //Or if the wallet cannot pick up coins
        if(!WalletItem.isWallet(wallet) || !WalletItem.CanPickup((WalletItem)wallet.getItem()))
            return;
        //Get the level (-1 to properly calculate range)
        int magnetLevel = WalletItem.GetMagnetLevel(wallet);
        //Don't do anything if the Coin Magnet enchantment is not present.
        if(magnetLevel <= 0)
            return;
        //Calculate the search radius
        float range = getCollectionRange(magnetLevel);
        World level = player.getWorld();
        if(level == null)
            return;
        Box searchBox = new Box(player.getX() - range, player.getY() - range, player.getZ() - range, player.getX() + range, player.getY() + range, player.getZ() + range);
        boolean updateWallet = false;
        for(ItemEntity ie : level.getEntitiesByClass(ItemEntity.class, searchBox, e -> MoneyUtil.isCoin(e.getStack())))
        {
            ItemStack coinStack = ie.getStack();
            ItemStack leftovers = WalletItem.PickupCoin(wallet, coinStack);
            if(leftovers.getCount() != coinStack.getCount())
            {
                updateWallet = true;
                if(leftovers.isEmpty())
                    ie.discard();
                else
                    ie.setStack(leftovers);
                level.playSound(null, player.getBlockPos(), ModSounds.COINS_CLINKING, SoundCategory.PLAYERS, 0.4f, 1f);
            }
        }
        if(updateWallet)
        {
            walletHandler.setWallet(wallet);
            if(player.currentScreenHandler instanceof WalletMenuBase menu)
                menu.reloadWalletContents();
        }
    }

    public static float getCollectionRange(int enchantLevel) {
        enchantLevel -= 1;
        if(enchantLevel < 0)
            return 0f;
        return LCConfig.SERVER.coinMagnetBaseRange.get() + (LCConfig.SERVER.coinMagnetLeveledRange.get() * Math.min(enchantLevel, MAX_CALCULATION_LEVEL - 1));
    }

    public static Text getCollectionRangeDisplay(int enchantLevel) {
        float range = getCollectionRange(enchantLevel);
        String display = range % 1f > 0f ? String.valueOf(range) : String.valueOf(Math.round(range));
        return Text.literal(display).formatted(Formatting.GREEN);
    }

}