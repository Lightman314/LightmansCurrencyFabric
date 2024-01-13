package io.github.lightman314.lightmanscurrency.network.server.messages.wallet;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.factory.WalletBankMenuFactory;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageOpenWalletBankMenu extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "wallet_openmenu_bank");

    private final int walletIndex;
    public CMessageOpenWalletBankMenu(int walletIndex) { super(PACKET_ID); this.walletIndex = walletIndex; }
    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setInt("slot",this.walletIndex); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        player.openHandledScreen(new WalletBankMenuFactory(data.getInt("slot")));
    }


}
