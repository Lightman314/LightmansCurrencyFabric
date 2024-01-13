package io.github.lightman314.lightmanscurrency.network.client.messages.notifications;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageClientNotification extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "notification_push_to_client");

    private final Notification notification;

    public SMessageClientNotification(Notification notification) { super(PACKET_ID); this.notification = notification;}

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("notification", this.notification.save()); }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        Notification notification = Notification.deserialize(data.getCompound("notification"));
        if(notification != null)
        {
            if(!NotificationEvent.CLIENT_NOTIFICATION_EVENT.invoker().display(new NotificationEvent.NotificationReceivedOnClient(client.player.getUuid(), ClientNotificationData.GetNotifications(), notification)))
                return;
            if(LCConfig.CLIENT.pushNotificationsToChat.get())
                client.inGameHud.getChatHud().addMessage(notification.getChatMessage());
        }
    }

}
