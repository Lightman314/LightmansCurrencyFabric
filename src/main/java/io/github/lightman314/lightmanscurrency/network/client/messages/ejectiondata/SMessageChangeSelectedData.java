package io.github.lightman314.lightmanscurrency.network.client.messages.ejectiondata;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SMessageChangeSelectedData extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "ejectiondata_change_selection_s2c");

    private final int newSelection;
    public SMessageChangeSelectedData(int newSelection) { super(PACKET_ID); this.newSelection = newSelection; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeInt(this.newSelection); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(client.player.currentScreenHandler instanceof TraderRecoveryMenu menu)
            menu.changeSelection(buffer.readInt());
    }

}
