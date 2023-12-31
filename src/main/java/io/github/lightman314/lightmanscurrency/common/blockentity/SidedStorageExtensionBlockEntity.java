package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SidedStorageExtensionBlockEntity extends BlockEntity implements SidedStorageBlockEntity {

    public SidedStorageExtensionBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.STORAGE_EXTENSION, pos, state); }

    @Nullable
    public Storage<FluidVariant> getFluidStorage(Direction side) {
        SidedStorageBlockEntity source = this.getSource();
        if(source instanceof SidedStorageExtensionBlockEntity)
            return null;
        return source == null ? null : source.getFluidStorage(side);
    }

    @Nullable
    public Storage<ItemVariant> getItemStorage(Direction side) {
        SidedStorageBlockEntity source = this.getSource();
        if(source instanceof SidedStorageExtensionBlockEntity)
            return null;
        return source == null ? null : source.getItemStorage(side);
    }

    private SidedStorageBlockEntity getSource() {
        Block block = this.getCachedState().getBlock();
        if(block instanceof ICapabilityBlock handlerBlock)
        {
            BlockEntity blockEntity = handlerBlock.getCapabilityBlockEntity(this.getCachedState(), this.world, this.pos);
            if(blockEntity instanceof SidedStorageBlockEntity)
                return (SidedStorageBlockEntity) blockEntity;
        }
        return null;
    }

}
