package io.github.lightman314.lightmanscurrency.common.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TickableBlockEntity extends BlockEntity {

    public TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {super(type, pos, state); }

    private boolean firstTick = true;

    /**
     * Ticks run on client logical side only.
     */
    public void clientTick() {}
    /**
     * Ticks run on both clients & servers
     */
    public void tick() {}
    /**
     * Ticks run on the server logical side only.
     */
    public void serverTick() { }

    /**
     * Ticks on the first tick after the block entity is loaded (on both clients & servers)
     */
    public void onLoad() {}

    public static void tickHandler(World level, BlockPos pos, BlockState state, TickableBlockEntity blockEntity) {
        if(blockEntity.firstTick)
        {
            blockEntity.firstTick = false;
            blockEntity.onLoad();
        }
        if(level.isClient) blockEntity.clientTick();
        else blockEntity.serverTick();
        blockEntity.tick();
    }

}
