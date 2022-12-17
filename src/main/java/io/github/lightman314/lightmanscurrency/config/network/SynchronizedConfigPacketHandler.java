package io.github.lightman314.lightmanscurrency.config.network;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.config.SynchronizedConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SynchronizedConfigPacketHandler implements ClientPlayNetworking.PlayChannelHandler {

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        try{
            Identifier type = buf.readIdentifier();
            int jsonLength = buf.readInt();
            String jsonText = buf.readString(jsonLength);
            JsonObject json = JsonHelper.deserialize(jsonText);

            SynchronizedConfig.loadFromPacket(type, json);
        } catch(Throwable t) { LightmansCurrency.LogError("Error handling config synchronization packet.", t); }
    }
}
