package io.github.lightman314.lightmanscurrency.network.client.messages.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class SMessageStorageInteraction extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "trader_storage_message_s2c");

    private final NbtCompound message;
    public SMessageStorageInteraction(NbtCompound message) { super(PACKET_ID); this.message = message; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("message",this.message); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(client.player.currentScreenHandler instanceof TraderStorageMenu storageMenu)
            storageMenu.receiveMessage(data.getCompound("message"));
    }
}
