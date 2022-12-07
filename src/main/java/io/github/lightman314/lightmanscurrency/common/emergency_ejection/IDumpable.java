package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDumpable {

    //Ejection stuff
    public List<ItemStack> getContents(World level, BlockPos pos, BlockState state, boolean dropBlock);

    public MutableText getName();

    public OwnerData getOwner();

}