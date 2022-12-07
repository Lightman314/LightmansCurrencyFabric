package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.blocks.ATMBlock;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PortableATMItem extends TooltipItem{

	public PortableATMItem(Settings properties) { super(properties.maxCount(1), LCTooltips.ATM); }
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		if(!world.isClient)
			player.openHandledScreen(ATMBlock.ATM_MENU_FACTORY);
		return TypedActionResult.success(player.getStackInHand(hand));
	}
	
}