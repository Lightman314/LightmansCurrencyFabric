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

public class CMessageOpenRecoveryMenu extends ClientToServerPacket.Simple {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "ejectiondata_open_menu");

    public CMessageOpenRecoveryMenu() { super(PACKET_ID); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        player.openHandledScreen(TraderRecoveryMenu.PROVIDER);
    }
}
