package io.github.lightman314.lightmanscurrency.network.server.messages.auction;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageSubmitBid extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "auction_submit_bid");

    private final long auctionHouseID;
    private final int tradeIndex;
    private final CoinValue bidAmount;
    public CMessageSubmitBid(long auctionHouseID, int tradeIndex, CoinValue bidAmount) { super(PACKET_ID); this.auctionHouseID = auctionHouseID; this.tradeIndex = tradeIndex; this.bidAmount = bidAmount; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeLong(this.auctionHouseID); buffer.writeInt(this.tradeIndex); buffer.writeNbt(this.bidAmount.save(new NbtCompound(), CoinValue.DEFAULT_KEY)); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof TraderMenu menu)
        {
            TraderData data = TraderSaveData.GetTrader(false, buffer.readLong());
            if(data instanceof AuctionHouseTrader ah)
                ah.makeBid(player, menu, buffer.readInt(), new CoinValue(buffer.readNbt()));
        }
    }


}
