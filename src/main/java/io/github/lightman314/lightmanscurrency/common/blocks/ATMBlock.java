package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.menu.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menu.factory.SimpleMenuFactory;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.TallRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ATMBlock extends TallRotatableBlock {

	public static final NamedScreenHandlerFactory ATM_MENU_FACTORY = new SimpleMenuFactory((windowId, inventory, playerEntity) -> new ATMMenu(windowId, inventory));

	public ATMBlock(Settings properties) { super(properties); }
	
	@Override
	public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(!level.isClient)
			player.openHandledScreen(ATM_MENU_FACTORY);
		return ActionResult.SUCCESS;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, BlockView world, List<Text> tooltip, TooltipContext flag) {
		TooltipItem.addTooltip(tooltip, LCTooltips.ATM);
		super.appendTooltip(stack, world, tooltip, flag);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
		if(!state.get(ISBOTTOM))
			return VoxelShapes.empty();
		return super.getCullingShape(state, world, pos);
	}
	
}
