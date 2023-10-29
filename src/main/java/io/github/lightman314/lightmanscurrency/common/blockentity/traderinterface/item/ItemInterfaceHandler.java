package io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item;

import java.util.*;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class ItemInterfaceHandler extends ConfigurableSidedHandler {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "item_interface");

    protected final ItemTraderInterfaceBlockEntity blockEntity;

    private final Map<Direction,Storage<ItemVariant>> handlers = new HashMap<>();

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
                this.handlers.put(relativeSide, this.blockEntity.getItemBuffer().BuildStorage(() -> this.inputSides.get(relativeSide), () -> this.outputSides.get(relativeSide), this.blockEntity::setItemBufferDirty));
            return this.handlers.get(relativeSide);
        }
        return null;
    }

}