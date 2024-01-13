package io.github.lightman314.lightmanscurrency.network.client.messages.trader;

import io.github.lightman314.lightmanscurrency.client.data.ClientTraderData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageSyncUserCount extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_sync_user_count");

    private final long traderID;
    private final int userCount;

    public SMessageSyncUserCount(long traderID, int userCount) { super(PACKET_ID); this.traderID = traderID; this.userCount = userCount; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setLong("trader", this.traderID);
        dataBuilder.setInt("count", this.userCount);
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        TraderData trader = ClientTraderData.GetTrader(data.getLong("trader"));
        if(trader != null)
            trader.updateUserCount(data.getInt("count"));
    }

}
