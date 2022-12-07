package io.github.lightman314.lightmanscurrency.common.notifications.events;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;

public class NotificationEvent {

    public static final Event<NotificationPreSendCallback> PRE_SEND_EVENT = EventFactory.createArrayBacked(NotificationPreSendCallback.class,
            (listeners) -> (preSendEvent) -> {
                for(NotificationPreSendCallback listener : listeners) {
                    if(!listener.allow(preSendEvent))
                        return false;
                }
                return true;
            });

    public static final Event<NotificationSentCallback> NOTIFICATION_SENT_EVENT = EventFactory.createArrayBacked(NotificationSentCallback.class,
            (listeners) -> (notificationSentEvent) -> {
                for(NotificationSentCallback listener : listeners) {
                    listener.listen(notificationSentEvent);
                }
            });

    public static final Event<ClientNotificationCallback> CLIENT_NOTIFICATION_EVENT = EventFactory.createArrayBacked(ClientNotificationCallback.class,
            (listeners) -> (clientNotificationEvent) -> {
                for(ClientNotificationCallback listener : listeners) {
                    if(!listener.display(clientNotificationEvent))
                        return false;
                }
                return true;
            });

    private final UUID playerID;
    public UUID getPlayerID() { return this.playerID; }
    private final NotificationData data;
    public NotificationData getData() { return this.data; }
    protected Notification notification;
    public Notification getNotification() { return this.notification; }

    public NotificationEvent(UUID playerID, NotificationData data, Notification notification) {
        this.playerID = playerID;
        this.data = data;
        this.notification = notification;
    }

    /**
     * Events sent when a notification is sent to a player.
     * Only run server-side.
     */
    public static class NotificationSent extends NotificationEvent {

        protected NotificationSent(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }

        /**
         * Sent whenever a notification is about to be sent to a player via TradingOffice.postNotification.
         * Can be used to modify and/or replace the sent notification.
         * Cancel the event to cancel the notification from being sent.
         */
        public static class Pre extends NotificationSent
        {

            public Pre(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }

            public void setNotification(@NotNull Notification notification) {
                if(notification == null)
                    throw new NullPointerException("Cannot set the notification to null. Cancel the event if you wish for no notification to be sent.");
                this.notification = notification;
            }

        }

        /**
         * Sent whenever a notification is successfully sent.
         * Use this to listen to notifications.
         */
        public static class Post extends NotificationSent
        {
            public Post(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
        }

    }

    public interface NotificationPreSendCallback { public boolean allow(NotificationEvent.NotificationSent.Pre preSendEvent); }
    public interface NotificationSentCallback { public void listen(NotificationSent.Post notificationSentEvent); }

    /**
     * Sent when a notification is received on the client.
     * Cancel to prevent the notification from being posted in chat.
     */
    public static class NotificationReceivedOnClient extends NotificationEvent {

        public NotificationReceivedOnClient(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }

    }

    public interface ClientNotificationCallback { public boolean display(NotificationReceivedOnClient clientNotificationEvent); }

}