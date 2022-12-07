package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class CashRegisterBlock extends RotatableBlock implements BlockEntityProvider {

    public CashRegisterBlock(Settings properties) { super(properties); }

    public CashRegisterBlock(Settings properties, VoxelShape shape) { super(properties,shape); }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {return new CashRegisterBlockEntity(pos, state);}

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
    {
        if(!level.isClient)
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CashRegisterBlockEntity)
            {
                CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
                register.loadDataFromItems(stack.getNbt());
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
        {
            //Open UI
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CashRegisterBlockEntity)
            {
                CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
                BlockEntityUtil.sendUpdatePacket(blockEntity);
                register.OpenContainer(player);
            }
        }
        return ActionResult.SUCCESS;
    }

}