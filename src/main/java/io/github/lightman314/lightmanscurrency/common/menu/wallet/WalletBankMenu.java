package io.github.lightman314.lightmanscurrency.common.menu.wallet;

import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class WalletBankMenu extends WalletMenuBase implements BankAccount.IBankAccountMenu {

    public static final int BANK_WIDGET_SPACING = 128;

    public WalletBankMenu(int windowId, PlayerInventory inventory, int walletStackIndex) {

        super(ModMenus.WALLET_BANK, windowId, inventory, walletStackIndex);

        this.addCoinSlots(BANK_WIDGET_SPACING + 1);
        this.addDummySlots(WalletMenuBase.getMaxWalletSlots());

    }

    @Override
    public Inventory getCoinInput() { return this.coinInput; }

    @Override
    public boolean isClient() { return this.player.getWorld().isClient; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean canUse(PlayerEntity player) {
        this.getBankAccountReference();
        return super.canUse(player) && this.hasBankAccess();
    }

    @Override
    public void onDepositOrWithdraw() {
        if(this.getAutoConvert()) //Don't need to save if converting, as the ConvertCoins function auto-saves.
            this.ExchangeCoins();
        else //Save the wallet contents on bank interaction.
            this.saveWalletContents();
    }

}