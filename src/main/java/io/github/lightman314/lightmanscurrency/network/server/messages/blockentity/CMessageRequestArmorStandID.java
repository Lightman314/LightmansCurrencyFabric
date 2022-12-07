package io.github.lightman314.lightmanscurrency.network.server.messages.blockentity;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class CMessageRequestArmorStandID extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "blockentity_armordisplay_request_stand");

    private final BlockPos pos;
    public CMessageRequestArmorStandID(BlockPos pos) { super(PACKET_ID); this.pos = pos; }


    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeBlockPos(this.pos); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        BlockEntity blockEntity = player.world.getBlockEntity(buffer.readBlockPos());
        if(blockEntity instanceof ArmorDisplayTraderBlockEntity armorDisplay)
            armorDisplay.sendArmorStandSyncMessageToClient(responseSender);
    }

}
