package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CoinBlockItem extends BlockItem{

	public CoinBlockItem(Block block, Settings properties)
	{
		super(block, properties);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
	{
		super.appendTooltip(stack,  level,  tooltip,  flagIn);
		CoinItem.addCoinTooltips(stack, tooltip);
	}
	
}