package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class CoinJarBlock extends RotatableBlock implements BlockEntityProvider {

    public CoinJarBlock(Settings properties) { super(properties); }

    public CoinJarBlock(Settings properties, VoxelShape shape) { super(properties, shape); }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new CoinJarBlockEntity(pos, state); }

    @Override
    public void onPlaced(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CoinJarBlockEntity)
        {
            CoinJarBlockEntity jar = (CoinJarBlockEntity)blockEntity;
            jar.readItemTag(stack);
        }
        super.onPlaced(level,pos,state, player, stack);
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
        {
            ItemStack coinStack = player.getStackInHand(hand);
            if(!MoneyUtil.isCoin(coinStack, false))
                return ActionResult.SUCCESS;
            //Add coins to the bank
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CoinJarBlockEntity)
            {
                CoinJarBlockEntity jar = (CoinJarBlockEntity)blockEntity;
                if(jar.addCoin(coinStack))
                    coinStack.decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World level, BlockPos pos, BlockState state, PlayerEntity player)
    {

        //Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if(tileEntity instanceof CoinJarBlockEntity)
        {
            CoinJarBlockEntity jarEntity = (CoinJarBlockEntity)tileEntity;
            if(EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, player) > 0)
            {
                //Drop the item for this block, with the JarData in it.
                @SuppressWarnings("deprecation")
                ItemStack dropStack = new ItemStack(this.asItem());
                if(jarEntity.getStorage().size() > 0)
                    jarEntity.writeItemTag(dropStack);
                Block.dropStack(level, pos, dropStack);
            }
            else
            {
                //Only drop the coins within the jar
                jarEntity.getStorage().forEach(coin -> Block.dropStack(level, pos, coin));
            }
        }

        super.onBreak(level, pos, state, player);

    }

}