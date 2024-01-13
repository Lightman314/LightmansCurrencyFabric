package io.github.lightman314.lightmanscurrency.network.server.messages.walletslot;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageSetWalletVisible extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "wallet_set_visible");

    private final boolean nowVisible;
    public CMessageSetWalletVisible(boolean nowVisible) { super(PACKET_ID); this.nowVisible = nowVisible; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setBoolean("visible", this.nowVisible); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        WalletHandler walletHandler = WalletHandler.getWallet(player);
        walletHandler.setVisible(data.getBoolean("visible"));
    }

}
