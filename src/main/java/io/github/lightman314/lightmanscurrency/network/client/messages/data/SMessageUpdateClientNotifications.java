package io.github.lightman314.lightmanscurrency.network.client.messages.data;

import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageUpdateClientNotifications extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "datasync_notifications_update");

    private final NotificationData data;

    public SMessageUpdateClientNotifications(NotificationData data) { super(PACKET_ID); this.data = data; }


    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("data",this.data.save()); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        ClientNotificationData.UpdateNotifications(NotificationData.loadFrom(data.getCompound("data")));
    }

}
