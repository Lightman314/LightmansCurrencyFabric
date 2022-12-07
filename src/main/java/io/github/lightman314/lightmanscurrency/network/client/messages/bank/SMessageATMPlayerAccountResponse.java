package io.github.lightman314.lightmanscurrency.network.client.messages.bank;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.SelectionTab;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SMessageATMPlayerAccountResponse extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_select_player_account_response");

    private final MutableText message;
    public SMessageATMPlayerAccountResponse(MutableText message) { super(PACKET_ID); this.message = message; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeString(Text.Serializer.toJson(this.message)); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(client.currentScreen instanceof ATMScreen screen)
        {
            if(screen.currentTab() instanceof SelectionTab tab)
                tab.ReceiveSelectPlayerResponse(Text.Serializer.fromJson(buffer.readString()));
        }
    }
}
