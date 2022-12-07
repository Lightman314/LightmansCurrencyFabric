package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@Environment(EnvType.CLIENT)
public class ClientNotificationData {

    private static NotificationData myNotifications = new NotificationData();

    public static NotificationData GetNotifications() { return myNotifications; }

    public static void UpdateNotifications(NotificationData data) {
        myNotifications = data;
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.currentScreen instanceof NotificationScreen screen)
            screen.reinit();
    }

    public static void onClientLogout(ClientPlayNetworkHandler handler, MinecraftClient client) {
        //Reset notifications
        myNotifications = new NotificationData();
    }

}