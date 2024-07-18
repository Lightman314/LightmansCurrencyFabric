package io.github.lightman314.lightmanscurrency.mixin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
public abstract class MerchantScreenHandlerMixin {

    @Unique
    private MerchantScreenHandler self() { return (MerchantScreenHandler)(Object)this; }

    @Accessor("merchant")
    public abstract Merchant getTrader();
    @Accessor("merchantInventory")
    public abstract MerchantInventory getTradeContainer();
    @Unique
    private PlayerEntity getPlayer() { Merchant m = this.getTrader(); if(m != null) return m.getCustomer(); return null; }

    @Inject(at = @At("HEAD"), method = "switchTo")
    private void tryMoveItemsEarly(int trade, CallbackInfo info)
    {
        //Clear coin items into the wallet instead of their inventory
        try {
            MerchantScreenHandler self = this.self();
            if(trade >= 0 && trade < self.getRecipes().size())
                this.EjectMoneyIntoWallet(this.getPlayer(), false);
        } catch (Throwable ignored) {}
    }

    @Inject(at = @At("TAIL"), method = "switchTo")
    private void tryMoveItems(int trade, CallbackInfo info)
    {
        try {
            MerchantScreenHandler self = this.self();
            if(trade >= 0 && trade < self.getRecipes().size())
            {
                MerchantInventory tradeContainer = this.getTradeContainer();
                if(tradeContainer.getStack(0).isEmpty() && tradeContainer.getStack(1).isEmpty())
                {
                    TradeOffer offer = self.getRecipes().get(trade);
                    if(MoneyUtil.isCoin(offer.getAdjustedFirstBuyItem(), false) && isCoinOrEmpty(offer.getSecondBuyItem()))
                    {
                        ItemStack coinA = offer.getAdjustedFirstBuyItem().copy();
                        ItemStack coinB = offer.getSecondBuyItem().copy();

                        CoinValue tradeValue = MoneyUtil.getCoinValue(ImmutableList.of(coinA, coinB));
                        LightmansCurrency.LogDebug("Coin Value of the selected trade is " + tradeValue.getString());
                        PlayerEntity player = this.getPlayer();
                        if(player == null)
                            return;

                        WalletHandler walletHandler = WalletHandler.getWallet(player);
                        if(walletHandler.getWallet().isEmpty())
                            return;
                        CoinValue availableFunds = MoneyUtil.getCoinValue(WalletItem.getWalletInventory(walletHandler.getWallet()));

                        CoinValue fundsToExtract = CoinValue.EMPTY;
                        int coinACount = coinA.getCount();
                        int coinBCount = coinB.isEmpty() ? 0 : coinB.getCount();
                        int coinAMaxCount = coinA.getMaxCount();
                        int coinBMaxCount = coinB.isEmpty() ? 0 : coinB.getMaxCount();
                        int coinToAddA = 0;
                        int coinToAddB = 0;

                        for(boolean keepLooping = true; keepLooping;)
                        {
                            int tempC2AA = coinAMaxCount > coinToAddA ? MathUtil.clamp(coinToAddA + coinACount, 0, coinAMaxCount) : coinToAddA;
                            int tempC2AB = coinBMaxCount > coinToAddB ? MathUtil.clamp(coinToAddB + coinBCount, 0, coinBMaxCount) : coinToAddB;

                            coinA.setCount(tempC2AA);
                            coinB.setCount(tempC2AB);

                            CoinValue thisValue = MoneyUtil.getCoinValue(ImmutableList.of(coinA, coinB));
                            if(availableFunds.getRawValue() < thisValue.getRawValue())
                                keepLooping = false;
                            else
                            {
                                fundsToExtract = thisValue;
                                coinToAddA = tempC2AA;
                                coinToAddB = tempC2AB;
                                if(coinToAddA >= coinAMaxCount && coinToAddB >= coinBMaxCount)
                                    keepLooping = false;
                            }
                        }

                        if((coinToAddA > 0 || coinToAddB > 0) && fundsToExtract.hasAny())
                        {
                            coinA.setCount(coinToAddA);
                            coinB.setCount(coinToAddB);
                            if(MoneyUtil.ProcessPayment(null, player, fundsToExtract))
                            {
                                tradeContainer.setStack(0, coinA.copy());
                                tradeContainer.setStack(1, coinB.copy());
                                LightmansCurrency.LogDebug("Moved " + fundsToExtract.getString() + " worth of coins into the Merchant Menu!");
                            }
                        }
                    }
                }
            }
        } catch(Throwable ignored) {}
    }

    @Inject(at = @At("HEAD"), method = "onClosed")
    private void removed(PlayerEntity player, CallbackInfo info) {
        if(this.isPlayerAliveAndValid(player))
            this.EjectMoneyIntoWallet(player, true);
    }

    @Unique
    private boolean isPlayerAliveAndValid(PlayerEntity player)
    {
        if(player.isAlive())
        {
            if(player instanceof ServerPlayerEntity sp)
                return !sp.isDisconnected();
            return true;
        }
        return false;
    }

    @Unique
    private void EjectMoneyIntoWallet(PlayerEntity player, boolean noUpdate)
    {
        MerchantInventory tradeContainer = this.getTradeContainer();
        ItemStack item = tradeContainer.getStack(0);
        if (!item.isEmpty() && MoneyUtil.isCoin(item, false)) {
            MoneyUtil.ProcessChange(null, player, MoneyUtil.getCoinValue(item));
            if(noUpdate)
                tradeContainer.removeStack(0);
            else
                tradeContainer.setStack(0, ItemStack.EMPTY);
        }
        item = tradeContainer.getStack(1);
        if (!item.isEmpty() && MoneyUtil.isCoin(item, false)) {
            MoneyUtil.ProcessChange(null, player, MoneyUtil.getCoinValue(item));
            if(noUpdate)
                tradeContainer.removeStack(1);
            else
                tradeContainer.setStack(1, ItemStack.EMPTY);
        }
    }

    @Unique
    private static boolean isCoinOrEmpty(ItemStack item) { return MoneyUtil.isCoin(item, false) || item.isEmpty(); }

}