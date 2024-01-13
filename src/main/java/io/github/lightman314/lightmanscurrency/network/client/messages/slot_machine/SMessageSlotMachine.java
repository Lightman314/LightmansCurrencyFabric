package io.github.lightman314.lightmanscurrency.network.client.messages.slot_machine;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageSlotMachine extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "slot_machine_menu_s2c");

    private final LazyPacketData data;

    public SMessageSlotMachine(LazyPacketData data) {
        super(PACKET_ID);
        this.data = data;
    }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.clone(this.data); }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(client.player.currentScreenHandler instanceof SlotMachineMenu menu)
            menu.HandleMessage(data);
    }

}
