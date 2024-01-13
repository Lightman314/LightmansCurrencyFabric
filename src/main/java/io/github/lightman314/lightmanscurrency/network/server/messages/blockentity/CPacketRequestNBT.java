package io.github.lightman314.lightmanscurrency.network.server.messages.blockentity;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class CPacketRequestNBT extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "blockentity_request_data");

    private final BlockPos pos;

    public CPacketRequestNBT(BlockPos pos) { super(PACKET_ID); this.pos = pos; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setBlockPos("pos", this.pos); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        BlockEntity blockEntity = player.getWorld().getBlockEntity(data.getBlockPos("pos"));
        if(blockEntity != null)
            BlockEntityUtil.sendUpdatePacket(blockEntity);
    }

}
