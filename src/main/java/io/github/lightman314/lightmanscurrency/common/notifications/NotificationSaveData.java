package io.github.lightman314.lightmanscurrency.common.notifications;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.events.NotificationEvent;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientNotifications;
import io.github.lightman314.lightmanscurrency.network.client.messages.notifications.SMessageClientNotification;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

public class NotificationSaveData extends PersistentState {

    private NotificationSaveData() {}

    private NotificationSaveData(NbtCompound compound) {

        NbtList notificationData = compound.getList("PlayerNotifications", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < notificationData.size(); ++i)
        {
            NbtCompound tag = notificationData.getCompound(i);
            UUID id = tag.getUuid("Player");
            NotificationData data = NotificationData.loadFrom(tag);
            if(id != null)
                this.playerNotifications.put(id, data);
        }

    }

    private final Map<UUID,NotificationData> playerNotifications = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        NbtList notificationData = new NbtList();
        this.playerNotifications.forEach((id,data) -> {
            NbtCompound tag = data.save();
            tag.putUuid("Player", id);
            notificationData.add(tag);
        });
        compound.put("PlayerNotifications", notificationData);

        return compound;
    }

    private static NotificationSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(NotificationSaveData::new, NotificationSaveData::new, "lightmanscurrency_notification_data");
        }
        return null;
    }

    @NotNull
    public static NotificationData GetNotifications(PlayerEntity player) { return player == null ? new NotificationData() : GetNotifications(player.getUuid()); }

    @NotNull
    public static NotificationData GetNotifications(UUID playerID) {
        if(playerID == null)
            return new NotificationData();
        NotificationSaveData nsd = get();
        if(nsd != null)
        {
            if(!nsd.playerNotifications.containsKey(playerID))
            {
                nsd.playerNotifications.put(playerID, new NotificationData());
                nsd.markDirty();
            }
            return nsd.playerNotifications.get(playerID);
        }
        return new NotificationData();
    }

    public static void MarkNotificationsDirty(UUID playerID) {
        NotificationSaveData nsd = get();
        if(nsd != null)
        {
            nsd.markDirty();
            MinecraftServer server = ServerHook.getServer();
            if(server != null)
            {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerID);
                if(player != null)
                    new SMessageUpdateClientNotifications(GetNotifications(playerID)).sendTo(player);
            }
        }
    }

    public static boolean PushNotification(UUID playerID, Notification notification) { return PushNotification(playerID, notification, true); }

    public static boolean PushNotification(UUID playerID, Notification notification, boolean pushToChat) {
        if(notification == null)
        {
            LightmansCurrency.LogError("Cannot push a null notification!");
            return false;
        }
        NotificationData data = GetNotifications(playerID);
        if(data != null)
        {
            //Post event to see if we should sent the notification
            NotificationEvent.NotificationSent.Pre event = new NotificationEvent.NotificationSent.Pre(playerID, data, notification);
            if(!NotificationEvent.PRE_SEND_EVENT.invoker().allow(event))
                return false;

            //Passed the pre event, add the notification to the notification data
            data.addNotification(event.getNotification());
            //Mark the data as dirty
            MarkNotificationsDirty(playerID);
            //Run the post event to notify anyone who cares that the notification was created.
            NotificationEvent.NOTIFICATION_SENT_EVENT.invoker().listen(new NotificationEvent.NotificationSent.Post(playerID, data, event.getNotification()));

            //Send the notification message to the client so that it will be posted in chat
            if(pushToChat)
            {
                MinecraftServer server = ServerHook.getServer();
                if(server != null)
                {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerID);
                    if(player != null)
                        new SMessageClientNotification(notification).sendTo(player);
                }
            }

            return true;
        }
        return false;
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {
        //Only send their personal notifications
        NotificationData notifications = GetNotifications(player);
        new SMessageUpdateClientNotifications(notifications).sendTo(sender);
    }

}