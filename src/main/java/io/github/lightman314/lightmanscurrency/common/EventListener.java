package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModGameRules;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventListener {

    public static void registerEventListeners() {
        PlayerBlockBreakEvents.BEFORE.register(EventListener::onBlockBreak);
    }

    //TODO create or listen to item pickup event
    private static void onPlayerItemPickup(PlayerEntity player, ItemEntity item)
    {

    }

    private static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, /* Nullable */ BlockEntity blockEntity)
    {
        if(state.getBlock() instanceof IOwnableBlock block)
            return block.canBreak(player, world, pos, state);
        return true;
    }

    //Player Drops
    //TODO create or listen to entity/player death event
    private static void onPlayerDeath(LivingEntity entity)
    {
        if(entity.world.isClient) //Do nothing client side
            return;
        if(entity instanceof PlayerEntity player && !player.isSpectator())
        {
            WalletHandler walletHandler = WalletHandler.getWallet(player);

            ItemStack walletStack = walletHandler.getWallet();
            if(walletStack.isEmpty()) //No wallet? Nothing to drop!
                return;

            boolean keepWallet = player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || player.world.getGameRules().getBoolean(ModGameRules.KEEP_WALLET);
            int coinDropPercent = MathUtil.clamp(player.world.getGameRules().getInt(ModGameRules.COIN_DROP_PERCENT), 0, 100);

            if(keepWallet && coinDropPercent <= 0)
                return;

            if(keepWallet)
            {
                spawnWalletDrops(player, walletStack, coinDropPercent);
            }
            else
            {
                spawnDrop(player, walletStack);
                walletHandler.setWallet(ItemStack.EMPTY);
            }


        }
    }

    private static void spawnDrop(PlayerEntity player, ItemStack stack)
    {
        ItemEntity item = new ItemEntity(player.world, player.getPos().x, player.getPos().y, player.getPos().z, stack);
        player.world.spawnEntity(item);
    }

    private static void spawnWalletDrops(PlayerEntity player, ItemStack walletStack, int coinDropPercent)
    {
        double coinPercentage = MathUtil.clamp((double)coinDropPercent / 100d, 0d, 1d);
        DefaultedList<ItemStack> walletList = WalletItem.getWalletInventory(walletStack);
        long walletContents = new CoinValue(walletList).getRawValue();

        long droppedAmount = (long)((double)walletContents * coinPercentage);
        if(droppedAmount < 1)
            return;

        Inventory walletInventory = InventoryUtil.buildInventory(walletList);
        long extra = MoneyUtil.takeObjectsOfValue(droppedAmount, walletInventory, true);
        if(extra < 0)
        {
            List<ItemStack> extraCoins = MoneyUtil.getCoinsOfValue(-extra);
            for (ItemStack extraCoin : extraCoins) {
                ItemStack coinStack = InventoryUtil.TryPutItemStack(walletInventory, extraCoin);
                //Drop anything that wasn't able to fit back into the wallet
                if (!coinStack.isEmpty())
                    spawnDrop(player, coinStack);
            }
        }

        //Update the wallet stacks contents
        WalletItem.putWalletInventory(walletStack, InventoryUtil.buildList(walletInventory));

        //Drop the expected coins
        List<ItemStack> coinsOfValue = MoneyUtil.getCoinsOfValue(droppedAmount);
        for (ItemStack coinStack : coinsOfValue) {
            while (!coinStack.isEmpty())
                spawnDrop(player, coinStack.split(1));
        }

    }

}
