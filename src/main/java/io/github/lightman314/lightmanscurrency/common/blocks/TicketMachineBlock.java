package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menu.TicketMachineMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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

    private static final MutableText TITLE = Text.translatable("gui.lightmanscurrency.ticket_machine.title");

    public TicketMachineBlock(Settings properties) { super(properties, createCuboidShape(0d,0d,0d,16d,14d,16d)); }

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
        return new TicketMenuFactory(pos);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltip, TooltipContext flagIn)
    {
        TooltipItem.addTooltip(tooltip, LCTooltips.TICKET_MACHINE);
        super.appendTooltip(stack, level, tooltip, flagIn);
    }

    private record TicketMenuFactory(BlockPos pos) implements ExtendedScreenHandlerFactory {
        @Override
        public Text getDisplayName() { return TITLE; }
        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) { buf.writeBlockPos(this.pos); }
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) { return new TicketMachineMenu(syncId, inv, pos); }
    }

}