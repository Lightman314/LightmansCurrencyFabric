package io.github.lightman314.lightmanscurrency.network.client.messages.admin;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SMessageSyncAdminList extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "sync_admin_list");

    private final List<UUID> serverAdminList;
    public SMessageSyncAdminList(List<UUID> serverAdminList) { super(PACKET_ID); this.serverAdminList = serverAdminList; }

    @Override
    public void encode(PacketByteBuf buffer) {
        buffer.writeInt(this.serverAdminList.size());
        this.serverAdminList.forEach(buffer::writeUuid);
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        List<UUID> clientAdminList = new ArrayList<>();
        int count = buffer.readInt();
        for(int i = count; i > 0; i--)
            clientAdminList.add(buffer.readUuid());
        CommandLCAdmin.loadAdminPlayers(clientAdminList);
    }
}
