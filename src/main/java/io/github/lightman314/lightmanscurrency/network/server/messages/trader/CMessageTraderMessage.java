package io.github.lightman314.lightmanscurrency.network.server.messages.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageTraderMessage extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_message_generic");

    private final long traderID;
    private final NbtCompound message;

    public CMessageTraderMessage(long traderID, NbtCompound message) {super(PACKET_ID); this.traderID = traderID; this.message = message; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeLong(this.traderID); buffer.writeNbt(this.message); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        TraderData trader = TraderSaveData.GetTrader(false, buffer.readLong());
        if(trader != null)
            trader.receiveNetworkMessage(player, buffer.readUnlimitedNbt());
    }

}
