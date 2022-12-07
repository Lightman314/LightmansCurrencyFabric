package io.github.lightman314.lightmanscurrency.common.traders.item.handler;

import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item.ItemInterfaceHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.FabricStorageUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import java.util.*;

public class TraderItemHandler {

    private final ItemTraderData trader;
    private final Map<Direction,TraderHandler> handlers = new HashMap<>();

    public TraderItemHandler(ItemTraderData trader) { this.trader = trader; }

    public Storage<ItemVariant> getHandler(Direction relativeSide) {
        if(!this.handlers.containsKey(relativeSide))
            this.handlers.put(relativeSide, new TraderHandler(this.trader, relativeSide));
        return this.handlers.get(relativeSide);
    }

    private static class TraderHandler implements Storage<ItemVariant>
    {
        private final ItemTraderData trader;
        private final Direction side;

        protected TraderHandler(ItemTraderData trader, Direction side) { this.trader = trader; this.side = side; }

        protected  final TraderItemStorage getStorage() { return this.trader.getStorage(); }
        protected  final void markStorageDirty() { this.trader.markStorageDirty(); }

        @Override
        public boolean supportsInsertion() { return this.trader.allowInputSide(this.side); }
        @Override
        public boolean supportsExtraction() { return this.trader.allowOutputSide(this.side); }

        public boolean allowExtraction(ItemStack stack) {
            for(ItemTradeData trade : this.trader.getTradeData())
            {
                if(trade.isPurchase())
                {
                    for(int i = 0; i < 2; ++i)
                    {
                        if(InventoryUtil.ItemMatches(trade.getSellItem(i), stack))
                            return true;
                    }
                }
                else if(trade.isBarter())
                {
                    for(int i = 0; i < 2; ++i)
                    {
                        if(InventoryUtil.ItemMatches(trade.getBarterItem(i), stack))
                            return true;
                    }
                }
            }
            return false;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if(!this.supportsInsertion())
                return 0;
            ItemStack insertStack = FabricStorageUtil.getStack(resource, maxAmount);
            if(this.getStorage().allowItem(insertStack))
            {
                ItemStack fillStack = insertStack.copy();
                fillStack.setCount(Math.min(this.getStorage().getFittableAmount(fillStack), (int)maxAmount));
                if(fillStack.isEmpty())
                    return 0;
                transaction.addOuterCloseCallback(result -> {
                    if(result.wasCommitted()) {
                        this.getStorage().forceAddItem(fillStack);
                        this.markStorageDirty();
                    }
                });
                return fillStack.getCount();
            }
            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if(!this.supportsExtraction())
                return 0;
            ItemStack drainStack = FabricStorageUtil.getStack(resource, maxAmount);
            if(this.allowExtraction(drainStack))
            {
                ItemStack removalStack = drainStack.copy();
                removalStack.setCount(Math.min(this.getStorage().getItemCount(removalStack), (int)maxAmount));
                if(removalStack.isEmpty())
                    return 0;
                transaction.addOuterCloseCallback(result -> {
                    if(result.wasCommitted()) {
                        this.getStorage().removeItem(removalStack);
                        this.markStorageDirty();
                    }
                });
                return removalStack.getCount();
            }
            return 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            List<StorageView<ItemVariant>> slotView = new ArrayList<>();
            List<ItemStack> storageItems = this.getStorage().getContents();
            for(int i = 0; i < storageItems.size(); ++i)
                slotView.add(new HandlerSlot(this, i));
            return FabricStorageUtil.createIterator(slotView);
        }

        private record HandlerSlot(TraderHandler parent, int slot) implements StorageView<ItemVariant> {

            private ItemStack getStack() {
                List<ItemStack> contents = this.parent.getStorage().getContents();
                return this.slot >= contents.size() ? ItemStack.EMPTY : contents.get(this.slot);
            }

            @Override
            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) { return this.parent.extract(resource, maxAmount, transaction); }
            @Override
            public boolean isResourceBlank() { return this.getResource().isBlank(); }
            @Override
            public ItemVariant getResource() { return ItemVariant.of(this.getStack()); }
            @Override
            public long getAmount() { return this.getStack().getCount(); }
            @Override
            public long getCapacity() { return this.parent.getStorage().getMaxAmount(); }

        }

    }

}
