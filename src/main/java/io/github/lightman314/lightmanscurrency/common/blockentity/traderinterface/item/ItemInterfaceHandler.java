package io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item;

import java.util.*;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.util.FabricStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ItemInterfaceHandler extends ConfigurableSidedHandler {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "item_interface");

    protected final ItemTraderInterfaceBlockEntity blockEntity;

    private final Map<Direction,Handler> handlers = new HashMap<Direction, Handler>();

    public ItemInterfaceHandler(ItemTraderInterfaceBlockEntity blockEntity) { this.blockEntity = blockEntity; }

    @Override
    public Identifier getType() { return TYPE; }

    @Override
    public String getTag() { return "ItemData"; }

    @Override
    public Storage<ItemVariant> getItemStorage(Direction relativeSide) {
        if(this.inputSides.get(relativeSide) || this.outputSides.get(relativeSide))
        {
            if(!this.handlers.containsKey(relativeSide))
                this.handlers.put(relativeSide, new Handler(this, relativeSide));
            return this.handlers.get(relativeSide);
        }
        return null;
    }

    private static class Handler implements Storage<ItemVariant>
    {

        final ItemInterfaceHandler handler;
        final Direction side;

        Handler(ItemInterfaceHandler handler, Direction side) { this.handler = handler; this.side = side; }

        @Override
        public final boolean supportsInsertion() { return this.handler.inputSides.get(this.side); }
        @Override
        public final boolean supportsExtraction() { return this.handler.outputSides.get(this.side); }

        private TraderItemStorage getBuffer() { return this.handler.blockEntity.getItemBuffer(); }
        private void markBufferDirty() { this.handler.blockEntity.setItemBufferDirty(); }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if(!this.supportsInsertion())
                return 0;
            ItemStack insertStack = FabricStorageUtil.getStack(resource, maxAmount);
            if(this.handler.blockEntity.allowInput(insertStack))
            {
                ItemStack fittableStack = insertStack.copy();
                fittableStack.setCount(Math.min(this.getBuffer().getFittableAmount(insertStack), (int)maxAmount));
                if(fittableStack.isEmpty())
                    return 0;
                transaction.addOuterCloseCallback(result -> {
                    if(result.wasCommitted())
                    {
                        this.getBuffer().forceAddItem(fittableStack);
                        this.markBufferDirty();
                    }
                });
                return fittableStack.getCount();
            }
            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if(!this.supportsExtraction())
                return 0;
            ItemStack drainStack = FabricStorageUtil.getStack(resource, maxAmount);
            if(this.handler.blockEntity.allowOutput(drainStack))
            {
                ItemStack removalStack = drainStack.copy();
                removalStack.setCount(Math.min(this.getBuffer().getItemCount(drainStack), (int)maxAmount));
                if(removalStack.isEmpty())
                    return 0;
                transaction.addOuterCloseCallback(result -> {
                    if(result.wasCommitted())
                    {
                        this.getBuffer().removeItem(removalStack);
                        this.markBufferDirty();
                    }
                });
                return removalStack.getCount();
            }
            return 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
            List<StorageView<ItemVariant>> slotView = new ArrayList<>();
            List<ItemStack> bufferItems = this.getBuffer().getContents();
            for(int i = 0; i < bufferItems.size(); ++i)
                slotView.add(new HandlerSlot(this, i));
            return FabricStorageUtil.createIterator(slotView);
        }

        private record HandlerSlot(Handler parent, int slot) implements StorageView<ItemVariant> {

            private ItemStack getStack() {
                List<ItemStack> contents = this.parent.getBuffer().getContents();
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
            public long getCapacity() { return this.parent.handler.blockEntity.getStorageStackLimit(); }

        }

    }

}