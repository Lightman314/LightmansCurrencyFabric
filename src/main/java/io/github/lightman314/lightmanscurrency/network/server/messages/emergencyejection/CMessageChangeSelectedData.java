package io.github.lightman314.lightmanscurrency.network.server.messages.emergencyejection;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageChangeSelectedData extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "ejectiondata_change_selection");

    private final int newSelection;
    public CMessageChangeSelectedData(int newSelection) { super(PACKET_ID); this.newSelection = newSelection; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setInt("selection", this.newSelection); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof TraderRecoveryMenu menu)
            menu.changeSelection(data.getInt("selection"));
    }

}
