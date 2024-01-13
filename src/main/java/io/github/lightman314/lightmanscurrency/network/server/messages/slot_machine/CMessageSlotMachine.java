package io.github.lightman314.lightmanscurrency.network.server.messages.slot_machine;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageSlotMachine extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "slot_machine_menu_c2s");

    private final LazyPacketData data;

    public CMessageSlotMachine(LazyPacketData data) {
        super(PACKET_ID);
        this.data = data;
    }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.clone(this.data); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender)
    {
        if(player != null && player.currentScreenHandler instanceof SlotMachineMenu menu)
            menu.HandleMessage(data);
    }



}

