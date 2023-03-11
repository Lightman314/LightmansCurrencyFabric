package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.Direction;

public interface SidedStorageBlockEntity {

    Storage<ItemVariant> getItemStorage(Direction side);
    Storage<FluidVariant> getFluidStorage(Direction side);

    static void setup() {
        ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
            if(blockEntity instanceof SidedStorageBlockEntity ssbe)
                return ssbe.getItemStorage(side);
            return null;
        });

        FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
            if(blockEntity instanceof SidedStorageBlockEntity ssbe)
                return ssbe.getFluidStorage(side);
            return null;
        });
    }

}
