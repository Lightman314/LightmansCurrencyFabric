package io.github.lightman314.lightmanscurrency.network.server.messages.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageOpenTrades extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_openmenu_trades");

    private final long traderID;
    public CMessageOpenTrades(long traderID) { super(PACKET_ID); this.traderID = traderID; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setLong("trader", this.traderID); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        TraderData trader = TraderSaveData.GetTrader(false, data.getLong("trader"));
        if(trader != null)
            trader.openTraderMenu(player);
    }

}