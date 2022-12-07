package io.github.lightman314.lightmanscurrency.network.client.messages.blockentity;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SMessageSendArmorStandID extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "blockentity_armordisplay_send_stand");

    private final BlockPos pos;
    private final int id;

    public SMessageSendArmorStandID(BlockPos pos, int id) { super(PACKET_ID); this.pos = pos; this.id = id;}

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeBlockPos(this.pos); buffer.writeInt(this.id); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        BlockEntity blockEntity = client.world.getBlockEntity(buffer.readBlockPos());
        if(blockEntity instanceof ArmorDisplayTraderBlockEntity armorDisplay) {
            armorDisplay.receiveArmorStandID(buffer.readInt());
        }
    }

}
