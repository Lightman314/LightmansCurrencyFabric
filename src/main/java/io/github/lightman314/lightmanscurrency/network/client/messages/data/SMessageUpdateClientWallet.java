package io.github.lightman314.lightmanscurrency.network.client.messages.data;

import io.github.lightman314.lightmanscurrency.client.data.ClientWalletData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class SMessageUpdateClientWallet extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "datasync_wallet_update");

    private final UUID playerID;
    private final WalletHandler walletHandler;
    public SMessageUpdateClientWallet(UUID playerID, WalletHandler walletHandler) { super(PACKET_ID); this.playerID = playerID; this.walletHandler = walletHandler; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setUUID("player", this.playerID)
                .setCompound("wallet", this.walletHandler.save()); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        UUID playerID = data.getUUID("player");
        WalletHandler walletHandler = new WalletHandler();
        walletHandler.load(data.getCompound("wallet"));
        ClientWalletData.UpdateWallet(playerID, walletHandler);
    }

}
