package io.github.lightman314.lightmanscurrency.network.client.messages.auction;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction.AuctionBidTab;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageAttemptBid extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "auction_open_bid_screen");

    private final long auctionHouseID;
    private final int tradeIndex;

    public SMessageAttemptBid(long auctionHouseID, int tradeIndex) {
        super(PACKET_ID);
        this.auctionHouseID = auctionHouseID;
        this.tradeIndex = tradeIndex;
    }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setLong("trader", this.auctionHouseID)
                .setInt("trade", this.tradeIndex);
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(client.currentScreen instanceof TraderScreen screen)
            screen.setTab(new AuctionBidTab(screen, data.getLong("trader"), data.getInt("trade")));
    }

}
