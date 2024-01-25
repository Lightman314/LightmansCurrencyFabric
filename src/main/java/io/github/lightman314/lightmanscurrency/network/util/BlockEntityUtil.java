package io.github.lightman314.lightmanscurrency.network.util;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.server.messages.blockentity.CPacketRequestNBT;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockEntityUtil {

    public static void sendUpdatePacket(BlockEntity blockEntity) {
        BlockEntityUpdateS2CPacket packet = BlockEntityUpdateS2CPacket.create(blockEntity);
        sendUpdatePacket(blockEntity.getWorld(), blockEntity.getPos(), packet);
    }

    public static void sendUpdatePacket(BlockEntity blockEntity, NbtCompound compound) {
        BlockEntityUpdateS2CPacket packet = BlockEntityUpdateS2CPacket.create(blockEntity, be -> compound);
        sendUpdatePacket(blockEntity.getWorld(), blockEntity.getPos(), packet);
    }
    private static void sendUpdatePacket(World world, BlockPos pos, BlockEntityUpdateS2CPacket packet)
    {
        if(world instanceof ServerWorld serverWorld)
        {
            List<ServerPlayerEntity> players = serverWorld.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(pos), false);
            players.forEach(player -> player.networkHandler.sendPacket(packet));
        }
        else
            LightmansCurrency.LogWarning("Cannot send Block Entity Update Packet from a client.");
    }

    public static void requestUpdatePacket(BlockEntity blockEntity) {
        if(blockEntity == null || blockEntity.getWorld() == null)
            return;
        requestUpdatePacket(blockEntity.getWorld(), blockEntity.getPos()); }

    public static void requestUpdatePacket(World level, BlockPos pos) { if(level.isClient) new CPacketRequestNBT(pos).sendToServer(); }

}
