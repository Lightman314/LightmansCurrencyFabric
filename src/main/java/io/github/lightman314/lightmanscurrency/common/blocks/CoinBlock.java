package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoinBlock extends FallingBlock {

	private final ItemConvertible coinItem;
	
	public CoinBlock(Settings properties, ItemConvertible coinItem)
	{
		super(properties);
		this.coinItem = coinItem;
	}
	
	protected int getCoinCount()
	{
		return 36;
	}
	
	protected SoundEvent getBreakingSound() { return ModSounds.COINS_CLINKING; }
	
	@Override
	public void onLanding(World level, BlockPos pos, BlockState fallingState, BlockState hitState, FallingBlockEntity fallingBlock) {
		
		if(!level.isClient)
		{
			//Set the block as air
			level.setBlockState(pos, Blocks.AIR.getDefaultState());
			//Spawn the coins
			for(int i = 0; i < getCoinCount(); i++)
			{
				Block.dropStack(level, pos, new ItemStack(this.coinItem));
			}
			//Play the breaking sound
			level.playSound(null, pos, this.getBreakingSound(), SoundCategory.BLOCKS, 1f, 1f);
		}
		
	}
	
}
