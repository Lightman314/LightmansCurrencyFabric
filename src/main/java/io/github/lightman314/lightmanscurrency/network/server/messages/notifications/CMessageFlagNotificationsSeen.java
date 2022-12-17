package io.github.lightman314.lightmanscurrency.network.server.messages.notifications;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageFlagNotificationsSeen extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "notifications_flagasseen");

    private final NotificationCategory category;
    public CMessageFlagNotificationsSeen(NotificationCategory category) { super(PACKET_ID); this.category = category; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeNbt(this.category.save()); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        NotificationCategory category = NotificationCategory.deserialize(buffer.readUnlimitedNbt());
        if(category != null)
        {
            NotificationData data = NotificationSaveData.GetNotifications(player);
            if(data.unseenNotification(category))
            {
                for(Notification n : data.getNotifications(category))
                {
                    if(!n.wasSeen())
                        n.setSeen();
                }
                NotificationSaveData.MarkNotificationsDirty(player.getUuid());
            }
        }

    }

}
