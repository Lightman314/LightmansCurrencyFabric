package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.ATMMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.SMessageATMPlayerAccountResponse;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class CMessageATMSetPlayerAccount extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_select_player_account");

    private final String playerName;
    public CMessageATMSetPlayerAccount(String playerName) { super(PACKET_ID); this.playerName = playerName; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setString("player", this.playerName); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof ATMMenu menu)
        {
            MutableText response = menu.SetPlayerAccount(data.getString("player"));
            new SMessageATMPlayerAccountResponse(response).sendTo(responseSender);
        }
    }

}
