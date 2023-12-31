package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SlotMachineBlock extends TraderBlockTallRotatable {

    public static final Identifier LIGHT_MODEL_LOCATION = new Identifier(LightmansCurrency.MODID, "block/slot_machine/lights");

    public static final VoxelShape SHAPE_SOUTH = VoxelShapes.union(createCuboidShape(0d,14d,-1d, 16d, 16d, 16d), createCuboidShape(0d, 0d, 3d, 16d, 32d, 16d));
    public static final VoxelShape SHAPE_NORTH = VoxelShapes.union(createCuboidShape(0d,14d,0d, 16d, 16d, 17d), createCuboidShape(0d,0d,0d,16d,32d,13d));
    public static final VoxelShape SHAPE_EAST = VoxelShapes.union(createCuboidShape(-1d,14d,0d, 16d, 16d, 16d), createCuboidShape(3d,0d,0d,16d,32d,16d));
    public static final VoxelShape SHAPE_WEST = VoxelShapes.union(createCuboidShape(0d,14d,0d, 17d, 16d, 16d), createCuboidShape(0d,0d,0d,13d,32d,16d));

    public SlotMachineBlock(Settings properties) { super(properties, LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH,SHAPE_EAST,SHAPE_SOUTH,SHAPE_WEST)); }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new SlotMachineTraderBlockEntity(pos, state); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.SLOT_MACHINE; }

    @Nullable
    public Identifier getLightModel() { return LIGHT_MODEL_LOCATION; }

    @Override
    public Supplier<List<Text>> getItemTooltips() { return LCTooltips.SLOT_MACHINE; }


}
