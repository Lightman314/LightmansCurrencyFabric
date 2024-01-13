package io.github.lightman314.lightmanscurrency.network.server.messages.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageCollectCoins extends ClientToServerPacket.Simple {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_collect_coins");

    public CMessageCollectCoins() { super(PACKET_ID); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof TraderMenu menu)
            menu.CollectCoinStorage();
        else if(player.currentScreenHandler instanceof TraderStorageMenu menu)
            menu.CollectCoinStorage();
        else if(player.currentScreenHandler instanceof SlotMachineMenu menu)
            menu.CollectCoinStorage();
    }

}
