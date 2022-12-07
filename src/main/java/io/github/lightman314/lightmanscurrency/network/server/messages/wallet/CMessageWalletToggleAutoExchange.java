package io.github.lightman314.lightmanscurrency.network.server.messages.wallet;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageWalletToggleAutoExchange extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "wallet_toggle_autoexchange");

    public CMessageWalletToggleAutoExchange() { super(PACKET_ID); }

    @Override
    protected void encode(PacketByteBuf buffer) { }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof WalletMenuBase menu)
            menu.ToggleAutoExchange();
    }
}
