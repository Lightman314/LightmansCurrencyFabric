package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PortableTerminalItem extends TooltipItem {

	public PortableTerminalItem(Settings properties)
	{
		super(properties.maxCount(1), LCTooltips.TERMINAL);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		if(world.isClient)
		{
			try{ TradingTerminalScreen.open();
			} catch(Throwable ignored) {}
		}

		return TypedActionResult.success(player.getStackInHand(hand));
	}
	
}
