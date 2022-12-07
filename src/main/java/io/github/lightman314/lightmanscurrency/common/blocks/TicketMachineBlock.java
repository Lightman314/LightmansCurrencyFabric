package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menu.TicketMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menu.factory.SimpleMenuFactory;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TicketMachineBlock extends RotatableBlock implements BlockEntityProvider {

    private static final VoxelShape SHAPE_NORTH = createCuboidShape(4d,0d,0d,12d,16d,8d);
    private static final VoxelShape SHAPE_SOUTH = createCuboidShape(4d,0d,8d,12d,16d,16d);
    private static final VoxelShape SHAPE_EAST = createCuboidShape(8d,0d,4d,16d,16d,12d);
    private static final VoxelShape SHAPE_WEST = createCuboidShape(0d,0d,4d,8d,16d,12d);

    private static final MutableText TITLE = Text.translatable("gui.lightmanscurrency.ticket_machine.title");

    public TicketMachineBlock(Settings properties) { super(properties, LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST)); }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new TicketMachineBlockEntity(pos, state); }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result)
    {
        if(!level.isClient)
            player.openHandledScreen(this.createScreenHandlerFactory(state,level,pos));
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos)
    {
        return new SimpleMenuFactory((windowId, playerInventory, playerEntity) -> { return new TicketMachineMenu(windowId, playerInventory, pos);}, TITLE);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flagIn)
    {
        TooltipItem.addTooltip(tooltip, LCTooltips.TICKET_MACHINE);
        super.appendTooltip(stack, level, tooltip, flagIn);
    }

}