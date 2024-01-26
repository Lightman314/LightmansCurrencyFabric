package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.Map.Entry;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.integration.trinketsapi.LCTrinketsAPI;
import io.github.lightman314.lightmanscurrency.network.client.messages.enchantments.SMessageMoneyMendingClink;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class MoneyMendingEnchantment extends Enchantment {

    public MoneyMendingEnchantment(Rarity rarity, EquipmentSlot... slots) { super(rarity, EnchantmentTarget.BREAKABLE, slots); }

    @Override
    public int getMinPower(int level) { return level * 25; }

    @Override
    public int getMaxPower(int level) { return this.getMinPower(level) + 50; }

    @Override
    public boolean isTreasure() { return true; }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    protected boolean canAccept(Enchantment otherEnchant) {
        return otherEnchant != Enchantments.MENDING && super.canAccept(otherEnchant);
    }

    public static CoinValue getRepairCost() { return LCConfig.SERVER.moneyMendingRepairCost.get(); }

    public static void runPlayerTick(ServerPlayerEntity player) {
        WalletHandler walletHandler = WalletHandler.getWallet(player);
        ItemStack wallet = walletHandler.getWallet();
        if (WalletItem.isWallet(wallet)) {
            DefaultedList<ItemStack> walletInventory = WalletItem.getWalletInventory(wallet);
            long currentWalletValue = MoneyUtil.getValue(walletInventory);
            final long repairCost = MoneyMendingEnchantment.getRepairCost().getRawValue();
            if (repairCost > currentWalletValue)
                return;
            //Go through the players inventory searching for items with the money mending enchantment
            Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.chooseEquipmentWith(ModEnchantments.MONEY_MENDING, player, ItemStack::isDamaged);
            final ItemStack result;
            if(entry == null)
                result = LCTrinketsAPI.findMoneyMendingItem(player);
            else
                result = entry.getValue();
            if (result != null) {
                //Repair the item
                int currentDamage = result.getDamage();
                long repairAmount = Math.min(currentDamage, currentWalletValue / repairCost);
                result.setDamage(currentDamage - (int) repairAmount);
                currentWalletValue -= repairAmount * repairCost;
                //Remove the coins from the players inventory
                SimpleInventory newWalletInventory = new SimpleInventory(walletInventory.size());
                for (ItemStack coinStack : MoneyUtil.getCoinsOfValue(currentWalletValue)) {
                    ItemStack leftovers = InventoryUtil.TryPutItemStack(newWalletInventory, coinStack);
                    if (!leftovers.isEmpty()) {
                        //Force the extra coins into the players inventory
                        InventoryUtil.GiveToPlayer(player, leftovers);
                    }

                }
                WalletItem.putWalletInventory(wallet, InventoryUtil.buildList(newWalletInventory));
                walletHandler.setWallet(wallet);
                //Reload the wallets contents if the wallet menu is open.
                if (player.currentScreenHandler instanceof WalletMenuBase)
                    ((WalletMenuBase) player.currentScreenHandler).reloadWalletContents();

                //Send Money Mending clink message
                new SMessageMoneyMendingClink().sendTo(player);
            }
        }
    }

}