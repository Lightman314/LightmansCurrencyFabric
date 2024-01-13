package io.github.lightman314.lightmanscurrency.network.server.messages.traderinterface;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageInterfaceInteraction extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "traderinterface_interaction_message");

    private final NbtCompound message;
    public CMessageInterfaceInteraction(NbtCompound message) { super(PACKET_ID); this.message = message; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("message", this.message); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof TraderInterfaceMenu menu)
            menu.receiveMessage(data.getCompound("message"));
    }

}
