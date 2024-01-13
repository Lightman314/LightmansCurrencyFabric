package io.github.lightman314.lightmanscurrency.network.server.messages.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageExecuteTrade extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_execute_trade");

    private final int trader;
    private final int tradeIndex;
    public CMessageExecuteTrade(int trader, int tradeIndex) { super(PACKET_ID); this.trader = trader; this.tradeIndex = tradeIndex; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setInt("trader", this.trader)
                .setInt("trade", this.tradeIndex);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof TraderMenu menu)
            menu.ExecuteTrade(data.getInt("trader"), data.getInt("trade"));
    }

}
