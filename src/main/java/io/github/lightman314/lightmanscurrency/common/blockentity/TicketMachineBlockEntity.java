package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class TicketMachineBlockEntity extends BlockEntity {

    Inventory storage = new SimpleInventory(2);
    public Inventory getStorage() { return this.storage; }

    public TicketMachineBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.TICKET_MACHINE, pos, state); }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        InventoryUtil.saveAllItems("Items", compound, this.storage);
        super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        this.storage = InventoryUtil.loadAllItems("Items", compound, 2);
        super.readNbt(compound);
    }

}