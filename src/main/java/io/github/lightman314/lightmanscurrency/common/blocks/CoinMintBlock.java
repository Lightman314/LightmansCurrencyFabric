package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class CoinMintBlock extends RotatableBlock implements BlockEntityProvider {

	private static final MutableText TITLE = Text.translatable("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Settings properties) { super(properties, createCuboidShape(1d,0d,1d,15d,16d,15d)); }
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new CoinMintBlockEntity(pos, state); }

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) { return TickerUtil.createTickerHelper(type, ModBlockEntities.COIN_MINT, TickableBlockEntity::tickHandler); }
	@Override
	@SuppressWarnings("deprecation")
	public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!level.isClient)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CoinMintBlockEntity)
				player.openHandledScreen(new CoinMintMenuProvider(pos));
		}
		return ActionResult.SUCCESS;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinMintBlockEntity mintEntity)
			mintEntity.dumpContents(level, pos);
		super.onStateReplaced(state, level, pos, newState, isMoving);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flag)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.COIN_MINT);
		super.appendTooltip(stack, level, tooltip, flag);
	}

	private record CoinMintMenuProvider(BlockPos blockPos) implements ExtendedScreenHandlerFactory {
		@Override
		public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) { return new MintMenu(id, inventory, this.blockPos); }
		@Override
		public Text getDisplayName() { return TITLE; }
		@Override
		public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) { buffer.writeBlockPos(this.blockPos); }
	}

}