package io.github.lightman314.lightmanscurrency.common.traders.item.handler;

import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Supplier;

public class TraderItemHandler {

    private final ItemTraderData trader;
    private final Map<Direction,Storage<ItemVariant>> handlers = new HashMap<>();

    public TraderItemHandler(ItemTraderData trader) { this.trader = trader; }

    public Storage<ItemVariant> getHandler(Direction relativeSide) {
        if(!this.handlers.containsKey(relativeSide))
            this.handlers.put(relativeSide, this.trader.getStorage().BuildStorage(this.allowInput(relativeSide), this.allowOutput(relativeSide), this.trader::markStorageDirty));
        return this.handlers.get(relativeSide);
    }

    private Supplier<Boolean> allowInput(Direction relativeSide) { return () -> this.trader.allowInputSide(relativeSide); }
    private Supplier<Boolean> allowOutput(Direction relativeSide) { return () -> this.trader.allowOutputSide(relativeSide); }

}
