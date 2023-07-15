package io.github.lightman314.lightmanscurrency.common.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.trader.InteractionSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class TraderMenu extends Menu {

    public final ITraderSource traderSource;
    public final PlayerEntity player;

    public static final int SLOT_OFFSET = 15;

    InteractionSlot interactionSlot;
    public InteractionSlot getInteractionSlot() { return this.interactionSlot; }
    Inventory coins = new SimpleInventory(5);
    public Inventory getCoinInventory() { return this.coins; }

    List<Slot> coinSlots = new ArrayList<>();
    public List<Slot> getCoinSlots() { return this.coinSlots; }

    public TraderMenu(int windowID, PlayerInventory inventory, long traderID) {
        this(ModMenus.TRADER, windowID, inventory, () -> TraderSaveData.GetTrader(inventory.player.getWorld().isClient, traderID));
    }

    protected TraderMenu(ScreenHandlerType<?> type, int windowID, PlayerInventory inventory, Supplier<ITraderSource> traderSource) {
        this(type, windowID, inventory, ITraderSource.getSafeSource(traderSource));
    }

    protected TraderMenu(ScreenHandlerType<?> type, int windowID, PlayerInventory inventory, ITraderSource traderSource) {
        super(type, windowID);
        this.player = inventory.player;
        this.traderSource = traderSource;
        this.init(inventory);
        for(TraderData trader : this.traderSource.getTraders()) {
            if(trader != null) trader.userOpen(this.player);
        }
    }

    public TradeContext getContext(TraderData trader) {
        return TradeContext.create(trader, this.player).withCoinSlots(this.coins).withInteractionSlot(this.interactionSlot).build();
    }

    protected void init(Inventory inventory) {

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
        }

        //Coin Slots
        for(int x = 0; x < coins.size(); x++)
        {
            this.coinSlots.add(this.addSlot(new CoinSlot(this.coins, x, SLOT_OFFSET + 8 + (x + 4) * 18, 122)));
        }

        //Interaction Slots
        List<InteractionSlotData> slotData = new ArrayList<>();
        for(TraderData trader : this.traderSource.getTraders())
            trader.addInteractionSlots(slotData);
        this.interactionSlot = new InteractionSlot(slotData, SLOT_OFFSET + 8, 122);
        this.addSlot(this.interactionSlot);

    }

    @Override
    public boolean canUse(PlayerEntity player) { return this.traderSource != null && this.traderSource.getTraders().size() > 0; }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.coins);
        this.dropInventory(player, this.interactionSlot.inventory);
        if(this.traderSource != null)
        {
            for(TraderData trader : this.traderSource.getTraders()) {
                if(trader != null) trader.userClose(this.player);
            }
        }

    }

    public void ExecuteTrade(int traderIndex, int tradeIndex) {
        //LightmansCurrency.LogInfo("Executing trade " + traderIndex + "/" + tradeIndex);
        if(this.traderSource == null)
        {
            this.closeMenu(this.player);
            return;
        }
        List<TraderData> traderList = this.traderSource.getTraders();
        if(traderIndex >= 0 && traderIndex < traderList.size())
        {
            TraderData trader = this.traderSource.getTraders().get(traderIndex);
            if(trader == null)
            {
                LightmansCurrency.LogWarning("Trader at index " + traderIndex + " is null.");
                return;
            }
            TradeResult result = trader.ExecuteTrade(this.getContext(trader), tradeIndex);
            if(result.hasMessage())
                LightmansCurrency.LogDebug(result.failMessage.getString());
        }
        else
            LightmansCurrency.LogWarning("Trader " + traderIndex + " is not a valid trader index.");
    }

    public boolean isSingleTrader() {
        if(this.traderSource == null)
        {
            this.closeMenu(this.player);
            return false;
        }
        return this.traderSource.isSingleTrader() && this.traderSource.getTraders().size() == 1;
    }

    public TraderData getSingleTrader() {
        if(this.isSingleTrader())
            return this.traderSource.getSingleTrader();
        return null;
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                //Move from inventory to coin/interaction slots
                if(!this.insertItem(slotStack, 36, this.slots.size(), false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < this.slots.size())
            {
                //Move from coin/interaction slots to inventory
                if(!this.insertItem(slotStack, 0, 36, false))
                {
                    return ItemStack.EMPTY;
                }
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

    public void CollectCoinStorage() {
        if(this.isSingleTrader())
        {
            LightmansCurrency.LogDebug("Attempting to collect coins from trader.");
            TraderData trader = this.getSingleTrader();
            if(trader != null && trader.hasPermission(this.player, Permissions.COLLECT_COINS))
            {
                CoinValue payment = trader.getInternalStoredMoney();
                if(this.getContext(trader).givePayment(payment))
                    trader.clearStoredMoney();
            }
            else
                Permissions.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
        }
    }

    public static class TraderMenuBlockSource extends TraderMenu
    {
        public TraderMenuBlockSource(int windowID, PlayerInventory inventory, BlockPos blockPosition) {
            super(ModMenus.TRADER_BLOCK, windowID, inventory, () -> {
                BlockEntity be = inventory.player.getWorld().getBlockEntity(blockPosition);
                if(be instanceof ITraderSource)
                    return (ITraderSource)be;
                return null;
            });
        }
    }

    public static class TraderMenuAllNetwork extends TraderMenu
    {
        public TraderMenuAllNetwork(int windowID, PlayerInventory inventory) { super(ModMenus.TRADER_NETWORK_ALL, windowID, inventory, ITraderSource.UniversalTraderSource(inventory.player.getWorld().isClient)); }
    }



}