package io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage.ITraderInputOutputFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ItemTraderInterfaceBlockEntity extends TraderInterfaceBlockEntity implements ITraderInputOutputFilter {

    private final TraderItemStorage itemBuffer = new TraderItemStorage(this);
    public TraderItemStorage getItemBuffer() { return this.itemBuffer; }

    ItemInterfaceHandler itemHandler;
    public ItemInterfaceHandler getItemHandler() { return this.itemHandler; }

    public ItemTraderInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_TRADER_INTERFACE, pos, state);
        this.itemHandler = this.addHandler(new ItemInterfaceHandler(this));
    }

    @Override
    public TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext) {
        return baseContext.withItemStorage(this.itemBuffer);
    }

    public boolean allowInput(ItemStack item) {
        if(this.getInteractionType().trades)
        {
            //Check trade for barter items to restock
            TradeData t = this.getReferencedTrade();
            if(t instanceof ItemTradeData trade)
            {
                if(trade.isBarter())
                {
                    for(int i = 0; i < 2; ++i)
                    {
                        if(InventoryUtil.ItemMatches(item, trade.getBarterItem(i)))
                            return true;
                    }
                }
                else if(trade.isPurchase())
                {
                    for(int i = 0; i < 2; ++i)
                    {
                        if(InventoryUtil.ItemMatches(item, trade.getSellItem(i)))
                            return true;
                    }
                }
            }
            return false;
        }
        else
        {
            //Scan all trades for sell items to restock
            TraderData trader = this.getTrader();
            if(trader instanceof ItemTraderData)
            {
                for(ItemTradeData trade : ((ItemTraderData) trader).getTradeData())
                {
                    if(trade.isSale() || trade.isBarter())
                    {
                        for(int i = 0; i < 2; ++i)
                        {
                            if(InventoryUtil.ItemMatches(item, trade.getSellItem(i)))
                                return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public boolean allowOutput(ItemStack item) {
        return !this.allowInput(item);
    }

    @Override
    public boolean isItemRelevant(ItemStack item) {
        if(this.getInteractionType().trades)
        {
            TradeData t = this.getReferencedTrade();
            if(t instanceof ItemTradeData trade)
            {
                return trade.allowItemInStorage(item);
            }
        }
        else
        {
            TraderData trader = this.getTrader();
            if(trader instanceof ItemTraderData)
            {
                for(ItemTradeData trade : ((ItemTraderData) trader).getTradeData())
                {
                    if(trade.allowItemInStorage(item))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getStorageStackLimit() {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        for(int i = 0; i < this.getUpgradeInventory().size(); ++i)
        {
            ItemStack stack = this.getUpgradeInventory().getStack(i);
            if(stack.getItem() instanceof UpgradeItem upgradeItem)
            {
                if(this.allowUpgrade(upgradeItem))
                {
                    if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
                    {
                        limit += upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
                    }
                }
            }
        }
        return limit;
    }

    @Override
    protected ItemTradeData deserializeTrade(NbtCompound compound) { return ItemTradeData.loadData(compound, false); }

    @Override
    protected void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        this.saveItemBuffer(compound);
    }

    protected final NbtCompound saveItemBuffer(NbtCompound compound) {
        this.itemBuffer.save(compound, "Storage");
        return compound;
    }

    public void setItemBufferDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveItemBuffer(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        if(compound.contains("Storage"))
            this.itemBuffer.load(compound, "Storage");
    }

    @Override
    public boolean validTraderType(TraderData trader) { return trader instanceof ItemTraderData; }

    protected final ItemTraderData getItemTrader() {
        TraderData trader = this.getTrader();
        if(trader instanceof ItemTraderData)
            return (ItemTraderData)trader;
        return null;
    }

    @Override
    protected void drainTick() {
        ItemTraderData trader = this.getItemTrader();
        if(trader != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
        {
            for(int i = 0; i < trader.getTradeCount(); ++i)
            {
                ItemTradeData trade = trader.getTrade(i);
                if(trade.isValid())
                {
                    List<ItemStack> drainItems = new ArrayList<>();
                    if(trade.isPurchase())
                    {
                        drainItems.add(trade.getSellItem(0));
                        drainItems.add(trade.getSellItem(1));
                    }

                    if(trade.isBarter())
                    {
                        drainItems.add(trade.getBarterItem(0));
                        drainItems.add(trade.getBarterItem(1));
                    }
                    for(ItemStack drainItem : drainItems)
                    {
                        if(!drainItem.isEmpty())
                        {
                            //Drain the item from the trader
                            int drainableAmount = trader.getStorage().getItemCount(drainItem);
                            if(drainableAmount > 0)
                            {
                                ItemStack movingStack = drainItem.copy();
                                movingStack.setCount(Math.min(movingStack.getMaxCount(), drainableAmount));
                                //Remove the stack from storage
                                ItemStack removed = trader.getStorage().removeItem(movingStack);
                                //InventoryUtil.RemoveItemCount(trader.getStorage(), movingStack);
                                //Put the stack in the item buffer (if possible)
                                this.itemBuffer.tryAddItem(removed);
                                //If some items couldn't be put in the item buffer, put them back in storage
                                if(!removed.isEmpty())
                                    trader.getStorage().forceAddItem(removed);
                                this.setItemBufferDirty();
                                trader.markStorageDirty();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void restockTick() {
        ItemTraderData trader = this.getItemTrader();
        if(trader != null && trader.hasPermission(this.owner.getPlayerForContext(), Permissions.INTERACTION_LINK))
        {
            for(int i = 0; i < trader.getTradeCount(); ++i)
            {
                ItemTradeData trade = trader.getTrade(i);
                if(trade.isValid() && (trade.isBarter() || trade.isSale()))
                {
                    for(int s = 0; s < 2; ++s)
                    {
                        ItemStack stockItem = trade.getSellItem(s);
                        if(!stockItem.isEmpty())
                        {
                            int stockableAmount = this.itemBuffer.getItemCount(stockItem);
                            if(stockableAmount > 0)
                            {
                                ItemStack movingStack = stockItem.copy();
                                movingStack.setCount(Math.min(movingStack.getMaxCount(), stockableAmount));
                                //Remove the item from the item buffer
                                ItemStack removedItem = this.itemBuffer.removeItem(movingStack);
                                if(removedItem.getCount() == movingStack.getCount())
                                {
                                    trader.getStorage().tryAddItem(movingStack);
                                    if(!movingStack.isEmpty())
                                    {
                                        //Place the leftovers back in storage
                                        this.itemBuffer.forceAddItem(movingStack);
                                    }
                                }
                                else
                                    this.itemBuffer.forceAddItem(removedItem);
                                this.setItemBufferDirty();
                                trader.markStorageDirty();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void tradeTick() {
        TradeData t = this.getTrueTrade();
        if(t instanceof ItemTradeData trade)
        {
            if(trade.isValid())
            {
                if(trade.isSale())
                {
                    //Confirm that we have enough space to store the purchased item(s)
                    if(this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
                    {
                        this.interactWithTrader();
                        this.setItemBufferDirty();
                    }
                }
                else if(trade.isPurchase())
                {
                    //Confirm that we have enough of the item in storage to sell the item(s)
                    if(this.itemBuffer.hasItems(trade.getSellItem(0), trade.getSellItem(1)))
                    {
                        this.interactWithTrader();
                        this.setItemBufferDirty();
                    }
                }
                else if(trade.isBarter())
                {
                    //Confirm that we have enough space to store the purchased item AND
                    //That we have enough of the item in storage to barter away.
                    if(this.itemBuffer.hasItems(trade.getBarterItem(0), trade.getBarterItem(1)) && this.itemBuffer.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
                    {
                        this.interactWithTrader();
                        this.setItemBufferDirty();
                    }
                }
            }
        }

    }

    @Override
    protected void hopperTick() {
        AtomicBoolean markBufferDirty = new AtomicBoolean(false);
        for(Direction relativeSide : Direction.values())
        {
            if(this.itemHandler.getInputSides().get(relativeSide) || this.itemHandler.getOutputSides().get(relativeSide))
            {
                Direction actualSide = relativeSide;
                if(this.getCachedState().getBlock() instanceof IRotatableBlock b)
                {
                    actualSide = IRotatableBlock.getActualSide(b.getFacing(this.getCachedState()), relativeSide);
                }

                BlockPos queryPos = this.pos.offset(actualSide);
                BlockEntity be = this.world.getBlockEntity(queryPos);
                if(be instanceof SidedInventory sidedInventory)
                {
                    //Collect items from neighboring blocks
                    if(this.itemHandler.getInputSides().get(relativeSide))
                    {
                        boolean query = true;
                        int[] slots = sidedInventory.getAvailableSlots(actualSide.getOpposite());
                        for(int x = 0; query && x < slots.length; ++x)
                        {
                            int slot = slots[x];
                            ItemStack stack = sidedInventory.getStack(slot);
                            int fittableAmount = this.itemBuffer.getFittableAmount(stack);
                            if(fittableAmount > 0 && sidedInventory.canExtract(slot, stack, actualSide.getOpposite()))
                            {
                                query = false;
                                ItemStack result = sidedInventory.removeStack(slot, fittableAmount);
                                this.itemBuffer.forceAddItem(result);
                                markBufferDirty.set(true);
                            }
                        }
                    }
                    if(this.itemHandler.getOutputSides().get(relativeSide))
                    {
                        List<ItemStack> buffer = this.itemBuffer.getContents();
                        boolean query = true;
                        for(int i = 0; query && i < buffer.size(); ++i)
                        {
                            ItemStack stack = buffer.get(i).copy();
                            if(this.allowOutput(stack))
                            {
                                int[] slots = sidedInventory.getAvailableSlots(actualSide.getOpposite());
                                for(int x = 0; query && x < slots.length; ++x)
                                {
                                    int slot = slots[x];
                                    ItemStack existingStack = sidedInventory.getStack(slot);
                                    if((InventoryUtil.ItemMatches(stack, existingStack) && existingStack.getCount() < existingStack.getMaxCount()) || existingStack.isEmpty() && sidedInventory.canInsert(slot, stack.copy(), actualSide.getOpposite()))
                                    {
                                        query = false;
                                        markBufferDirty.set(true);
                                        if(existingStack.isEmpty())
                                        {
                                            int placeAmount = Math.min(stack.getCount(), stack.getMaxCount());
                                            ItemStack placeStack = stack.copy();
                                            placeStack.setCount(placeAmount);
                                            sidedInventory.setStack(slot,placeStack);
                                            sidedInventory.markDirty();
                                            this.itemBuffer.removeItem(placeStack.copy());
                                        }
                                        else
                                        {
                                            int placeAmount = Math.min(existingStack.getMaxCount() - existingStack.getCount(), stack.getCount());
                                            existingStack.increment(placeAmount);
                                            ItemStack removeStack = existingStack.copy();
                                            removeStack.setCount(placeAmount);
                                            this.itemBuffer.removeItem(removeStack);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

            }
        }
        if(markBufferDirty.get())
            this.setItemBufferDirty();

    }

    @Override
    public void initMenuTabs(TraderInterfaceMenu menu) { menu.setTab(TraderInterfaceTab.TAB_STORAGE, new ItemStorageTab(menu)); }

    @Override
    public boolean allowAdditionalUpgrade(UpgradeType type) { return type == UpgradeType.ITEM_CAPACITY; }

    @Override
    public void getAdditionalContents(List<ItemStack> contents) { contents.addAll(this.itemBuffer.getSplitContents()); }

    @Override
    public MutableText getName() { return Text.translatable("block.lightmanscurrency.item_trader_interface"); }

}