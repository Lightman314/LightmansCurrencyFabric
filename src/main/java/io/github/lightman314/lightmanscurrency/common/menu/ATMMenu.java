package io.github.lightman314.lightmanscurrency.common.menu;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ATMMenu extends Menu implements BankAccount.IBankAccountAdvancedMenu {

    private PlayerEntity player;
    public PlayerEntity getPlayer() { return this.player; }

    private final Inventory coinInput = new SimpleInventory(9);
    public Inventory getCoinInput() { return this.coinInput; }

    private Text transferMessage = null;

    public ATMMenu(int windowId, PlayerInventory inventory)
    {
        super(ModMenus.ATM, windowId);

        this.player = inventory.player;

        //Coinslots
        for(int x = 0; x < coinInput.size(); x++)
        {
            this.addSlot(new CoinSlot(this.coinInput, x, 8 + x * 18, 129, false));
        }

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 161 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 219));
        }
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        //Run get bank account code during valid check so that it auto-validates the account access and updates the client as necessary.
        this.getBankAccountReference();
        return true;
    }

    @Override
    public void onClosed(PlayerEntity playerIn)
    {
        super.onClosed(playerIn);
        this.dropInventory(playerIn,  this.coinInput);
        if(!this.isClient())
        {
            BankAccount.AccountReference account = this.getBankAccountReference();
            if(account.accountType == BankAccount.AccountType.Player)
            {
                if(!account.playerID.equals(this.player.getUuid()))
                {
                    //Switch back to their personal bank account when closing the ATM if they're accessing another players bank account.
                    BankSaveData.SetSelectedBankAccount(this.player, BankAccount.GenerateReference(this.player));
                }
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index)
    {
        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            if(index < this.coinInput.size())
            {
                if(MoneyUtil.isCoin(slotStack.getItem()))
                {
                    if(!this.insertItem(slotStack,  this.coinInput.size(), this.slots.size(), true))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if(!this.insertItem(slotStack, 0, this.coinInput.size(), false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.setStack(ItemStack.EMPTY);
            }
            else
            {
                slot.markDirty();
            }
        }

        return clickedStack;

    }

    public void ExchangeCoins(String command)
    {
        ///Converting Upwards
        //Converting All Upwards
        if(command.contentEquals("convertAllUp"))
        {
            MoneyUtil.ConvertAllCoinsUp(this.coinInput);
        }
        //Convert defined coin upwards
        else if(command.startsWith("convertUp-"))
        {
            Identifier coinID = null;
            String id = "";
            try {
                id = command.substring("convertUp-".length());
                coinID = new Identifier(id);
                Item coinItem = Registries.ITEM.get(coinID);
                if(coinItem == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a registered item.");
                    return;
                }
                if(!MoneyUtil.isCoin(coinItem))
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a coin.");
                    return;
                }
                if(MoneyUtil.getUpwardConversion(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is the largest visible coin in its chain, and thus cannot be converted any larger.");
                    return;
                }
                MoneyUtil.ConvertCoinsUp(this.coinInput, coinItem);
            } catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
        }
        else if(command.contentEquals("convertAllDown"))
        {
            MoneyUtil.ConvertAllCoinsDown(this.coinInput);
        }
        else if(command.startsWith("convertDown-"))
        {
            String id = "";
            try {
                id = command.substring("convertDown-".length());
                Identifier coinID = new Identifier(id);
                Item coinItem = Registries.ITEM.get(coinID);
                if(coinItem == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a registered item.");
                    return;
                }
                if(!MoneyUtil.isCoin(coinItem))
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is not a coin.");
                    return;
                }
                if(MoneyUtil.getDownwardConversion(coinItem) == null)
                {
                    LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + coinID.toString() + "' is the smallest known coin, and thus cannot be converted any smaller.");
                    return;
                }
                MoneyUtil.ConvertCoinsDown(this.coinInput, coinItem);
            } catch(Exception e) { LightmansCurrency.LogError("Error handling ATM Conversion command '" + command + "'.\n'" + id + "' could not be parsed as an item id.", e);}
        }
        else
            LightmansCurrency.LogError("'" + command + "' is not a valid ATM Conversion command.");

    }


    public MutableText SetPlayerAccount(String playerName) {

        if(CommandLCAdmin.isAdminPlayer(this.player))
        {
            PlayerReference accountPlayer = PlayerReference.of(false, playerName);
            if(accountPlayer != null)
            {
                BankSaveData.SetSelectedBankAccount(this.player, BankAccount.GenerateReference(false, accountPlayer));
                return Text.translatable("gui.bank.select.player.success", accountPlayer.getName(false));
            }
            else
                return Text.translatable("gui.bank.transfer.error.null.to");
        }
        return Text.literal("ERROR");

    }

    public boolean hasTransferMessage() { return this.transferMessage != null; }

    public Text getTransferMessage() { return this.transferMessage; }

    @Override
    public void setTransferMessage(Text message) { this.transferMessage = message; }

    public void clearMessage() { this.transferMessage = null; }

    @Override
    public boolean isClient() { return this.player.getWorld().isClient; }

}