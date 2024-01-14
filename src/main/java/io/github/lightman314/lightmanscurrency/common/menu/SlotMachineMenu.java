package io.github.lightman314.lightmanscurrency.common.menu;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.messages.slot_machine.SMessageSlotMachine;
import io.github.lightman314.lightmanscurrency.network.server.messages.slot_machine.CMessageSlotMachine;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class SlotMachineMenu extends Menu {

    private final long traderID;
    public final PlayerEntity player;

    public boolean isClient() { return this.player.getWorld().isClient; }

    public final SlotMachineTraderData getTrader() { if(TraderSaveData.GetTrader(this.isClient(), this.traderID) instanceof SlotMachineTraderData trader) return trader; return null; }

    private final Inventory coins;

    List<Slot> coinSlots = new ArrayList<>();

    private final List<RewardCache> rewards = new ArrayList<>();
    public final boolean hasPendingReward() { return this.rewards.size() > 0; }
    public final RewardCache getNextReward() { if(this.rewards.size() == 0) return null; return this.rewards.get(0); }

    public final RewardCache getAndRemoveNextReward()
    {
        if(this.rewards.size() == 0)
            return null;
        return this.rewards.remove(0);
    }

    public SlotMachineMenu(int windowID, PlayerInventory inventory, long traderID) {
        super(ModMenus.SLOT_MACHINE, windowID);
        this.player = inventory.player;
        this.traderID = traderID;
        this.coins = new SimpleInventory(5);

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 198));
        }

        //Coin Slots
        for(int x = 0; x < this.coins.size(); x++)
        {
            this.coinSlots.add(this.addSlot(new CoinSlot(this.coins, x, 8 + (x + 4) * 18, 108)));
        }

        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
            trader.userOpen(this.player);

    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                //Move from inventory to coin slots
                if(!this.insertItem(slotStack, 36, this.slots.size(), false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(index < this.slots.size())
            {
                //Move from coin slots to inventory
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

    @Override
    public boolean canUse(PlayerEntity player) { return this.getTrader() != null; }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        //Force-give rewards if closed before reward is handled
        for(RewardCache reward : this.rewards)
            reward.giveToPlayer();
        this.rewards.clear();
        //Clear the coin slots
        this.dropInventory(player, this.coins);
        //Close the trader
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
            trader.userClose(this.player);
    }

    public final void clearContainer(Inventory container) { this.dropInventory(this.player, container); }

    public final TradeContext getContext() { return this.getContext(null); }

    public final TradeContext getContext(RewardCache rewardHolder)
    {
        TradeContext.Builder builder = TradeContext.create(this.getTrader(), this.player).withCoinSlots(this.coins);
        if(rewardHolder != null)
        {
            builder.withInventory(rewardHolder.itemHolder);
            builder.withStoredCoins(rewardHolder.money);
        }

        return builder.build();
    }

    public void CollectCoinStorage() {
        if(this.getTrader() != null)
        {
            TraderData trader = this.getTrader();
            if(trader.hasPermission(this.player, Permissions.COLLECT_COINS))
            {
                CoinValue storedMoney = trader.getInternalStoredMoney();
                if(storedMoney.getRawValue() > 0)
                {
                    TradeContext tempContext = this.getContext();
                    if(!tempContext.hasPaymentMethod())
                        return;
                    tempContext.givePayment(storedMoney);
                    trader.clearStoredMoney();
                }
            }
            else
                Permissions.PermissionWarning(this.player, "collect stored coins", Permissions.COLLECT_COINS);
        }
    }

    private void ExecuteTrades(int count)
    {
        LightmansCurrency.LogDebug("Attempting to execute " + count + " trade(s)");
        if(this.rewards.size() > 0)
        {
            LightmansCurrency.LogWarning("Attempted to execute trades while previous trades are still pending.");
            return;
        }
        SlotMachineTraderData trader = this.getTrader();
        if(trader != null)
        {
            boolean flag = true;
            for(int i = 0; flag && i < count; ++i)
            {
                RewardCache holder = new RewardCache();
                TradeContext.TradeResult result = trader.ExecuteTrade(this.getContext(holder), 0);
                if(result.isSuccess())
                {
                    if(holder.itemHolder.isEmpty() && holder.money.getRawValue() <= 0)
                        LightmansCurrency.LogError("Successful Slot Machine Trade executed, but no items or money were received!");
                    else
                    {
                        this.rewards.add(holder);
                        LightmansCurrency.LogDebug("Successful Slot Machine Trade executed.");
                    }
                }
                else
                {
                    LightmansCurrency.LogWarning("Failed to execute the trade on attempt #" + (i + 1) + ".\nReason: " + result.failMessage.getString());
                    flag = false;
                }
            }
            if(this.rewards.size() > 0)
            {
                NbtCompound rewardData = new NbtCompound();
                NbtList resultList = new NbtList();
                for(RewardCache result : this.rewards)
                    resultList.add(result.save());
                rewardData.put("Rewards", resultList);
                this.SendMessageToClient(LazyPacketData.builder().setCompound("SyncRewards", rewardData));
                LightmansCurrency.LogDebug("Trades complete, syncing rewards with the client!");
            }
        }
        else
            LightmansCurrency.LogWarning("No trader found on the Slot Machine Menu");

    }

    public boolean GiveNextReward()
    {
        RewardCache nextReward = this.getAndRemoveNextReward();
        if(nextReward != null)
        {
            nextReward.giveToPlayer();
            return true;
        }
        return false;
    }

    public void SendMessageToClient(LazyPacketData.Builder builder)
    {
        if(this.isClient())
            return;
        new SMessageSlotMachine(builder.build()).sendTo(this.player);
    }

    public void SendMessageToServer(LazyPacketData.Builder builder)
    {
        if(!this.isClient())
            return;
        new CMessageSlotMachine(builder.build()).sendToServer();
    }

    public void HandleMessage(LazyPacketData message) {
        if(message.contains("ExecuteTrade"))
        {
            if(this.rewards.size() > 0)
                return;
            ExecuteTrades(message.getInt("ExecuteTrade"));
        }
        if(message.contains("GiveNextReward"))
        {
            this.GiveNextReward();
        }
        if(message.contains("AnimationsCompleted"))
        {
            //Give next reward while a reward is still present
            while(this.GiveNextReward()) { }
        }
        if(message.contains("SyncRewards") && this.isClient())
        {
            this.rewards.clear();
            NbtCompound rewardData = message.getCompound("SyncRewards");
            NbtList rewardList = rewardData.getList("Rewards", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < rewardList.size(); ++i)
                this.rewards.add(this.loadReward(rewardList.getCompound(i)));
        }
    }

    public final RewardCache loadReward(NbtCompound tag) {
        CoinValue value = new CoinValue();
        value.load(tag, "Money");
        return new RewardCache(InventoryUtil.loadAllItems("Items", tag, SlotMachineEntry.ITEM_LIMIT), value); }

    public final class RewardCache
    {
        public final Inventory itemHolder;
        public CoinValue money = new CoinValue();
        public RewardCache() { this.itemHolder = new SimpleInventory(SlotMachineEntry.ITEM_LIMIT); }
        public RewardCache(Inventory itemHolder, CoinValue money) { this.itemHolder = itemHolder; this.money = money; }
        public void giveToPlayer()
        {
            SlotMachineMenu.this.clearContainer(this.itemHolder);
            this.itemHolder.clear();
            MoneyUtil.ProcessChange(null, SlotMachineMenu.this.player, this.money);
            this.money = new CoinValue();
        }

        public List<ItemStack> getDisplayItems()
        {
            if(this.money.getRawValue() > 0)
            {
                return MoneyUtil.getCoinsOfValue(this.money.getRawValue());
            }
            else
            {
                List<ItemStack> items = new ArrayList<>();
                for(int i = 0; i < this.itemHolder.size(); ++i)
                {
                    ItemStack item = this.itemHolder.getStack(i);
                    if(!item.isEmpty())
                        items.add(item.copy());
                }
                return items;
            }
        }

        public NbtCompound save()
        {
            NbtCompound tag = new NbtCompound();
            InventoryUtil.saveAllItems("Items", tag, this.itemHolder);
            this.money.save(tag, "Money");
            return tag;
        }

    }

}