package io.github.lightman314.lightmanscurrency.network.client.messages.config;

import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SPacketSyncConfig extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "config_server_sync");

    private final Identifier configID;
    private final Map<String,String> data;

    public SPacketSyncConfig(Identifier configID, Map<String,String> data) {
        super(PACKET_ID);
        this.configID = configID;
        this.data = data;
    }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setResource("id", this.configID);
        this.data.forEach((id,dat) -> dataBuilder.setString("_" + id, dat));
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        Identifier configID = data.getResource("id");
        Map<String,String> dataMap = new HashMap<>();
        data.getAllKeys().forEach(key -> {
            if(!key.equals("id"))
                dataMap.put(key.substring(1),data.getString(key));
        });
        SyncedConfigFile.handleSyncData(configID, dataMap);
    }

}
