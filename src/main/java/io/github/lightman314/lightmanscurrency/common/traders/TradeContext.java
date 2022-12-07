package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.trader.InteractionSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.fluid.FluidStack;
import io.github.lightman314.lightmanscurrency.util.FabricStorageUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.fabricmc.fabric.impl.transfer.transaction.TransactionManagerImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@SuppressWarnings("removal")
public class TradeContext {

    public enum TradeResult {
        /**
         * Remote trade was successfully executed
         */
        SUCCESS(null),
        /**
         * Trade failed as the trader is out of stock
         */
        FAIL_OUT_OF_STOCK("lightmanscurrency.remotetrade.fail.nostock"),

        /**
         * Trade failed as the player could not afford the trade
         */
        FAIL_CANNOT_AFFORD("lightmanscurrency.remotetrade.fail.cantafford"),
        /**
         * Trade failed as there's no room for the output items
         */
        FAIL_NO_OUTPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.output"),
        /**
         * Trade failed as there's no room for the input items
         */
        FAIL_NO_INPUT_SPACE("lightmanscurrency.remotetrade.fail.nospace.input"),
        /**
         * Trade failed as the trade rules denied the trade
         */
        FAIL_TRADE_RULE_DENIAL("lightmanscurrency.remotetrade.fail.traderule"),
        /**
         * Trade failed as the trade is no longer valid
         */
        FAIL_INVALID_TRADE("lightmanscurrency.remotetrade.fail.invalid"),
        /**
         * Trade failed as this trader does not support remote trades
         */
        FAIL_NOT_SUPPORTED("lightmanscurrency.remotetrade.fail.notsupported"),
        /**
         * Trade failed as the trader was null
         */
        FAIL_NULL("lightmanscurrency.remotetrade.fail.null");
        public boolean hasMessage() { return this.failMessage != null; }
        public final Text failMessage;
        TradeResult(String message) { this.failMessage = message == null ? null : Text.translatable(message); }
    }

    public final boolean isStorageMode;

    //Trader Data (public as it will be needed for trade data context)
    private final TraderData trader;
    public boolean hasTrader() { return this.trader != null; }
    public TraderData getTrader() { return this.trader; }

    //Player Data
    private final PlayerEntity player;
    public boolean hasPlayer() { return this.player != null; }
    public PlayerEntity getPlayer() { return this.player; }

    //Public as it will be needed to run trade events to confirm a trades alerts/cost for display purposes
    private final PlayerReference playerReference;
    public boolean hasPlayerReference() { return this.playerReference != null; }
    public final PlayerReference getPlayerReference() { return this.playerReference; }

    //Money/Payment related data
    private final BankAccount.AccountReference bankAccount;
    private boolean hasBankAccount() { return this.bankAccount != null && this.bankAccount.get() != null; }

    private final Inventory coinSlots;
    private boolean hasCoinSlots() { return this.hasPlayer() && this.coinSlots != null; }

    private final CoinValue storedMoney;
    private boolean hasStoredMoney() { return this.storedMoney != null; }

    private final BiConsumer<CoinValue,Boolean> moneyListener;

    //Interaction Slots (bucket/battery slot, etc.)
    private final InteractionSlot interactionSlot;
    private boolean hasInteractionSlot(String type) { return this.getInteractionSlot(type) != null; }
    private InteractionSlot getInteractionSlot(String type) { if(this.interactionSlot == null) return null; if(this.interactionSlot.isType(type)) return this.interactionSlot; return null; }
    private ItemStack getInteractionSlotStack(String type) {
        InteractionSlot slot = getInteractionSlot(type);
        return slot == null ? ItemStack.EMPTY : slot.getStack();
    }

    //Item related data
    private final TraderItemStorage itemStorage;
    private boolean hasItemStorage() { return this.itemStorage != null; }

    private final Storage<ItemVariant> itemHandler;
    private boolean hasItemHandler() { return this.itemStorage != null; }

    //Fluid related data
    private final Storage<FluidVariant> fluidTank;
    private boolean hasFluidTank() { return this.fluidTank != null; }

    //Energy related data
    /*private final IEnergyStorage energyTank;
    private boolean hasEnergyTank() { return this.energyTank != null; }*/

    private TradeContext(Builder builder) {
        this.isStorageMode = builder.storageMode;
        this.trader = builder.trader;
        this.player = builder.player;
        this.playerReference = builder.playerReference;
        this.bankAccount = builder.bankAccount;
        this.coinSlots = builder.coinSlots;
        this.storedMoney = builder.storedCoins;
        this.moneyListener = builder.moneyListener;
        this.interactionSlot = builder.interactionSlot;
        this.itemStorage = builder.itemStorage;
        this.itemHandler = builder.itemHandler;
        this.fluidTank = builder.fluidHandler;
        //this.energyTank = builder.energyHandler;
    }

    public boolean hasPaymentMethod() { return this.hasPlayer() || this.hasCoinSlots() || this.hasBankAccount() || this.hasStoredMoney(); }

    public boolean hasFunds(CoinValue price)
    {
        return this.getAvailableFunds() >= price.getRawValue();
    }

    public long getAvailableFunds() {
        long funds = 0;
        if(this.hasBankAccount())
            funds += this.bankAccount.get().getCoinStorage().getRawValue();
        if(this.hasPlayer())
        {
            WalletHandler walletHandler = WalletHandler.getWallet(this.player);
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet.getItem()))
                funds += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
        }
        if(this.hasStoredMoney())
            funds += this.storedMoney.getRawValue();
        if(this.hasCoinSlots() && this.hasPlayer())
            funds += MoneyUtil.getValue(this.coinSlots);
        return funds;
    }

    public boolean getPayment(CoinValue price)
    {
        if(this.hasFunds(price))
        {
            if(this.moneyListener != null)
                this.moneyListener.accept(price, false);
            long amountToWithdraw = price.getRawValue();
            if(this.hasCoinSlots() && this.hasPlayer())
            {
                amountToWithdraw = MoneyUtil.takeObjectsOfValue(amountToWithdraw, this.coinSlots, true);
                if(amountToWithdraw < 0)
                {
                    List<ItemStack> change = MoneyUtil.getCoinsOfValue(-amountToWithdraw);
                    for(ItemStack stack : change)
                    {
                        ItemStack c = InventoryUtil.TryPutItemStack(this.coinSlots, stack);
                        if(!c.isEmpty())
                            InventoryUtil.GiveToPlayer(this.player, c);
                    }
                }
            }
            if(this.hasStoredMoney() && amountToWithdraw > 0)
            {
                long removeAmount = Math.min(amountToWithdraw, this.storedMoney.getRawValue());
                amountToWithdraw -= removeAmount;
                storedMoney.loadFromOldValue(storedMoney.getRawValue() - removeAmount);
            }
            if(this.hasBankAccount() && amountToWithdraw > 0)
            {
                CoinValue withdrawAmount = this.bankAccount.get().withdrawCoins(new CoinValue(amountToWithdraw));
                amountToWithdraw -= withdrawAmount.getRawValue();
                if(this.hasTrader() && withdrawAmount.getRawValue() > 0)
                {
                    this.bankAccount.get().LogInteraction(this.getTrader(), withdrawAmount, false);
                }
            }
            if(this.hasPlayer() && amountToWithdraw > 0)
            {
                WalletHandler walletHandler = WalletHandler.getWallet(this.player);
                ItemStack wallet = walletHandler.getWallet();
                if(WalletItem.isWallet(wallet.getItem()))
                {
                    DefaultedList<ItemStack> walletInventory = WalletItem.getWalletInventory(wallet);
                    amountToWithdraw = MoneyUtil.takeObjectsOfValue(amountToWithdraw, walletInventory, true);
                    WalletItem.putWalletInventory(wallet, walletInventory);
                    if(amountToWithdraw < 0)
                    {
                        for(ItemStack stack : MoneyUtil.getCoinsOfValue(-amountToWithdraw))
                        {
                            ItemStack c = WalletItem.PickupCoin(wallet, stack);
                            if(!c.isEmpty())
                                InventoryUtil.GiveToPlayer(this.player, c);
                        }
                        amountToWithdraw = 0;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean givePayment(CoinValue price)
    {
        if(this.moneyListener != null)
            this.moneyListener.accept(price, true);
        if(this.hasBankAccount())
        {
            this.bankAccount.get().depositCoins(price);
            if(this.hasTrader())
                this.bankAccount.get().LogInteraction(this.getTrader(), price, true);
            return true;
        }
        else if(this.hasStoredMoney())
        {
            this.storedMoney.addValue(price);
            return true;
        }
        else if(this.hasPlayer())
        {
            List<ItemStack> coins = MoneyUtil.getCoinsOfValue(price);
            List<ItemStack> change = new ArrayList<>();
            WalletHandler walletHandler = WalletHandler.getWallet(this.player);
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet.getItem()))
            {
                for(int i = 0; i < coins.size(); ++i)
                {
                    ItemStack coin = WalletItem.PickupCoin(wallet, coins.get(i));
                    if(!coin.isEmpty())
                        change.add(coin);
                }
            }
            if(this.hasCoinSlots() && change.size() > 0)
            {
                for(int i = 0; i < change.size(); ++i)
                {
                    ItemStack remainder = InventoryUtil.TryPutItemStack(this.coinSlots, change.get(i));
                    if(!remainder.isEmpty())
                        InventoryUtil.GiveToPlayer(this.player, remainder);
                }
            }
            else if(change.size() > 0)
            {
                for(int i = 0; i < change.size(); ++i)
                {
                    InventoryUtil.GiveToPlayer(this.player, change.get(i));
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Whether the given item stack is present in the item handler, and can be successfully removed without issue.
     */
    public boolean hasItem(ItemStack item)
    {
        if(this.hasItemStorage())
            return this.itemStorage.hasItem(item);
        if(this.hasItemHandler() && this.itemHandler.supportsExtraction())
            return FabricStorageUtil.simulateExtract(this.itemHandler, ItemVariant.of(item), item.getCount()) == item.getCount();
        if(this.hasPlayer())
            return InventoryUtil.GetItemCount(this.player.getInventory(), item) >= item.getCount();
        return false;
    }

    /**
     * Whether the given item stacks are present in the item handler, and can be successfully removed without issue.
     */
    public boolean hasItems(ItemStack... items)
    {
        for(ItemStack item : InventoryUtil.combineQueryItems(items))
        {
            if(!hasItem(item))
                return false;
        }
        return true;
    }

    /**
     * Whether a ticket with the given ticket id is present in the item handler, and can be successfully removed without issue.
     */
    public boolean hasTicket(UUID ticketID) {
        if(this.hasItemStorage())
        {
            for(int i = 0; i < this.itemStorage.getSlots(); ++i)
            {
                ItemStack stack = this.itemStorage.getStackInSlot(i);
                if(stack.getItem() == ModItems.TICKET)
                {
                    UUID id = TicketItem.GetTicketID(stack);
                    if(id != null && id.equals(ticketID))
                        return true;
                }
            }
        }
        else if(this.hasItemHandler() && this.itemHandler.supportsExtraction())
        {
            List<ItemStack> matchingItems = FabricStorageUtil.getMatchingItems(this.itemHandler, item -> item.getItem() == ModItems.TICKET && TicketItem.GetTicketID(item.toStack()) == ticketID);
            for(ItemStack item : matchingItems)
            {
                if(FabricStorageUtil.simulateExtract(this.itemHandler, ItemVariant.of(item), 1) == 1)
                    return true;
            }
            return false;
        }
        else if(this.hasPlayer())
        {
            Inventory inventory = this.player.getInventory();
            for(int i = 0; i < inventory.size(); ++i)
            {
                ItemStack stack = inventory.getStack(i);
                if(stack.getItem() == ModItems.TICKET)
                {
                    UUID id = TicketItem.GetTicketID(stack);
                    if(id != null && id.equals(ticketID))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the given item stack from the item handler.
     * @return Whether the extraction was successful. Will return false if it could not be extracted correctly.
     */
    public boolean collectItem(ItemStack item)
    {
        if(this.hasItem(item))
        {
            if(this.hasItemStorage())
            {
                this.itemStorage.removeItem(item);
                return true;
            }
            else if(this.hasItemHandler() && this.itemHandler.supportsExtraction())
            {
                return FabricStorageUtil.tryExtractExact(this.itemHandler, ItemVariant.of(item), item.getCount());
            }
            else if(this.hasPlayer())
            {
                InventoryUtil.RemoveItemCount(this.player.getInventory(), item);
                return true;
            }
        }
        return false;
    }


    /**
     * Removes the given ticket from the item handler.
     * @return Whether the extraction was successful. Will return false if it could not be extracted correctly.
     *
     */
    public boolean collectTicket(UUID ticketID) {
        if(this.hasTicket(ticketID))
        {
            if(this.hasItemStorage())
            {
                for(int i = 0; i < this.itemStorage.getSlots(); ++i) {
                    ItemStack stack = this.itemStorage.getStackInSlot(i);
                    if(stack.getItem() == ModItems.TICKET)
                    {
                        UUID id = TicketItem.GetTicketID(stack);
                        if(id != null && id.equals(ticketID))
                        {
                            ItemStack extractStack = stack.copy();
                            extractStack.setCount(1);
                            this.itemStorage.removeItem(extractStack);
                        }
                    }
                }
            }
            else if(this.hasItemHandler() && this.itemHandler.supportsExtraction())
            {
                List<ItemStack> matchingItems = FabricStorageUtil.getMatchingItems(this.itemHandler, item -> item.getItem() == ModItems.TICKET && TicketItem.GetTicketID(item.toStack()) == ticketID);
                for(ItemStack item : matchingItems)
                {
                    if(FabricStorageUtil.tryExtractExact(this.itemHandler, ItemVariant.of(item), 1))
                        return true;
                }
                return false;
            }
            else if(this.hasPlayer())
            {
                Inventory inventory = this.player.getInventory();
                for(int i = 0; i < inventory.size(); ++i)
                {
                    ItemStack stack = inventory.getStack(i);
                    if(stack.getItem() == ModItems.TICKET)
                    {
                        UUID id = TicketItem.GetTicketID(stack);
                        if(id != null && id.equals(ticketID))
                        {
                            inventory.removeStack(i, 1);
                            inventory.markDirty();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canFitItem(ItemStack item)
    {
        if(item.isEmpty())
            return true;
        if(this.hasItemStorage())
            return this.itemStorage.canFitItem(item);
        if(this.hasItemHandler() && this.itemHandler.supportsInsertion())
            return FabricStorageUtil.simulateInsert(this.itemHandler, ItemVariant.of(item), item.getCount()) == item.getCount();
        if(this.hasPlayer())
            return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean canFitItems(ItemStack... items)
    {
        if(this.hasItemStorage())
            return this.itemStorage.canFitItems(items);
        if(this.hasItemHandler())
        {
            for(ItemStack item : InventoryUtil.combineQueryItems(items))
            {
                if(FabricStorageUtil.simulateInsert(this.itemHandler, ItemVariant.of(item), item.getCount()) != item.getCount())
                    return false;
            }
            return true;
        }
        if(this.hasPlayer())
            return true;
        return false;
    }

    public boolean putItem(ItemStack item)
    {
        if(this.canFitItem(item))
        {
            if(this.hasItemStorage())
            {
                this.itemStorage.forceAddItem(item);
                return true;
            }
            if(this.hasItemHandler())
                return FabricStorageUtil.tryInsertExact(this.itemHandler, ItemVariant.of(item), item.getCount());
            if(this.hasPlayer())
            {
                InventoryUtil.GiveToPlayer(this.player, item);
                return true;
            }
        }
        return false;
    }

    public boolean hasFluid(FluidStack fluid)
    {
        if(this.hasFluidTank())
        {
            return FabricStorageUtil.simulateExtract(this.fluidTank, fluid, fluid.getAmount()) == fluid.getAmount();
        }
        else if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
        {
            ItemStack bucketStack = this.getInteractionSlotStack(InteractionSlotData.FLUID_TYPE);
            Storage<FluidVariant> fluidHandler = ContainerItemContext.withInitial(bucketStack).find(FluidStorage.ITEM);
            if(fluidHandler != null)
                return FabricStorageUtil.simulateExtract(fluidHandler, fluid, fluid.getAmount()) == fluid.getAmount();
        }
        return false;
    }

    public boolean drainFluid(FluidStack fluid)
    {
        if(this.hasFluid(fluid))
        {
            if(this.hasFluidTank())
            {
                return FabricStorageUtil.tryExtractExact(this.fluidTank, fluid, fluid.getAmount());
            }
            else if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
            {
                ItemStack bucketStack = this.getInteractionSlotStack(InteractionSlotData.FLUID_TYPE);
                Storage<FluidVariant> fluidHandler = ContainerItemContext.withInitial(bucketStack).find(FluidStorage.ITEM);
                if(fluidHandler != null)
                    return FabricStorageUtil.tryExtractExact(fluidHandler, fluid, fluid.getAmount());
            }
        }
        return false;
    }

    public boolean canFitFluid(FluidStack fluid)
    {
        if(this.hasFluidTank())
            return FabricStorageUtil.simulateInsert(this.fluidTank, fluid, fluid.getAmount()) == fluid.getAmount();
        else if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
        {
            ItemStack bucketStack = this.getInteractionSlotStack(InteractionSlotData.FLUID_TYPE);
            Storage<FluidVariant> fluidHandler = ContainerItemContext.withInitial(bucketStack).find(FluidStorage.ITEM);
            if(fluidHandler != null)
                return FabricStorageUtil.simulateInsert(fluidHandler, fluid, fluid.getAmount()) == fluid.getAmount();
        }
        return false;
    }

    public boolean fillFluid(FluidStack fluid)
    {
        if(this.canFitFluid(fluid))
        {
            if(this.hasFluidTank())
            {
                return FabricStorageUtil.tryInsertExact(this.fluidTank, fluid, fluid.getAmount());
            }
            else if(this.hasInteractionSlot(InteractionSlotData.FLUID_TYPE))
            {
                ItemStack bucketStack = this.getInteractionSlotStack(InteractionSlotData.FLUID_TYPE);
                Storage<FluidVariant> fluidHandler = ContainerItemContext.withInitial(bucketStack).find(FluidStorage.ITEM);
                if(fluidHandler != null)
                    return FabricStorageUtil.tryInsertExact(fluidHandler, fluid, fluid.getAmount());
            }
        }
        return false;
    }

    /*public boolean hasEnergy(int amount)
    {
        if(this.hasEnergyTank())
            return this.energyTank.extractEnergy(amount, true) == amount;
        else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
        {
            ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
            AtomicBoolean hasEnergy = new AtomicBoolean(false);
            batteryStack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyHandler ->{
                hasEnergy.set(energyHandler.extractEnergy(amount, true) == amount);
            });
            return hasEnergy.get();
        }
        return false;
    }

    public boolean drainEnergy(int amount)
    {
        if(this.hasEnergy(amount))
        {
            if(this.hasEnergyTank())
            {
                this.energyTank.extractEnergy(amount, false);
                return true;
            }
            if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
            {
                ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
                batteryStack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyHandler ->{
                    energyHandler.extractEnergy(amount, false);
                });
                return true;
            }
        }
        return false;
    }

    public boolean canFitEnergy(int amount)
    {
        if(this.hasEnergyTank())
            return this.energyTank.receiveEnergy(amount, true) == amount;
        else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
        {
            ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
            AtomicBoolean fitsEnergy = new AtomicBoolean(false);
            batteryStack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyHandler ->{
                fitsEnergy.set(energyHandler.receiveEnergy(amount, true) == amount);
            });
            return fitsEnergy.get();
        }
        return false;
    }

    public boolean fillEnergy(int amount)
    {
        if(this.canFitEnergy(amount))
        {
            if(this.hasEnergyTank())
            {
                this.energyTank.receiveEnergy(amount, false);
                return true;
            }
            else if(this.hasInteractionSlot(InteractionSlotData.ENERGY_TYPE))
            {
                ItemStack batteryStack = this.getInteractionSlot(InteractionSlotData.ENERGY_TYPE).getItem();
                batteryStack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyHandler ->{
                    energyHandler.receiveEnergy(amount, false);
                });
                return true;
            }
        }
        return false;
    }*/

    public static TradeContext createStorageMode(TraderData trader) { return new Builder(trader).build(); }
    public static Builder create(TraderData trader, PlayerEntity player) { return new Builder(trader, player); }
    public static Builder create(TraderData trader, PlayerReference player) { return new Builder(trader, player); }

    public static class Builder
    {

        //Core
        private final boolean storageMode;
        private final TraderData trader;
        private final PlayerEntity player;
        private final PlayerReference playerReference;

        //Money
        private BankAccount.AccountReference bankAccount;
        private Inventory coinSlots;
        private CoinValue storedCoins;
        private BiConsumer<CoinValue,Boolean> moneyListener;

        //Interaction Slots
        private InteractionSlot interactionSlot;

        //Item
        private TraderItemStorage itemStorage;
        private Storage<ItemVariant> itemHandler;
        //Fluid
        private Storage<FluidVariant> fluidHandler;
        //Energy
        //private IEnergyStorage energyHandler;

        private Builder(TraderData trader) { this.storageMode = true; this.trader = trader; this.player = null; this.playerReference = null; }
        private Builder(TraderData trader, PlayerEntity player) { this.trader = trader; this.player = player; this.playerReference = PlayerReference.of(player); this.storageMode = false; }
        private Builder(TraderData trader, PlayerReference player) { this.trader = trader; this.playerReference = player; this.player = null; this.storageMode = false; }

        public Builder withBankAccount(BankAccount.AccountReference bankAccount) { this.bankAccount = bankAccount; return this; }
        public Builder withCoinSlots(Inventory coinSlots) { this.coinSlots = coinSlots; return this; }
        public Builder withStoredCoins(CoinValue storedCoins) { this.storedCoins = storedCoins; return this; }

        public Builder withMoneyListener(BiConsumer<CoinValue,Boolean> moneyListener) { this.moneyListener = moneyListener; return this; }

        public Builder withInteractionSlot(InteractionSlot interactionSlot) { this.interactionSlot = interactionSlot; return this; }

        public Builder withItemStorage(TraderItemStorage itemStorage) { this.itemStorage = itemStorage; return this; }
        public Builder withItemHandler(Storage<ItemVariant> itemHandler) { this.itemHandler = itemHandler; return this; }
        public Builder withFluidHandler(Storage<FluidVariant> fluidHandler) { this.fluidHandler = fluidHandler; return this; }
        //public Builder withEnergyHandler(IEnergyStorage energyHandler) { this.energyHandler = energyHandler; return this; }

        public TradeContext build() { return new TradeContext(this); }

    }

}