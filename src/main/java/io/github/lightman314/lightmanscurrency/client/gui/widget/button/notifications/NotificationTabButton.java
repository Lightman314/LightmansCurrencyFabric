package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraft.client.font.TextRenderer;

import java.util.function.Supplier;

public class NotificationTabButton extends TabButton {

    final Supplier<NotificationData> dataSource;
    final NotificationCategory category;

    public NotificationTabButton(PressAction pressable, TextRenderer font, Supplier<NotificationData> dataSource, NotificationCategory category) {
        super(pressable, font, category);
        this.category = category;
        this.dataSource = dataSource;
    }

    protected boolean unseenNotifications() { return this.dataSource.get().unseenNotification(this.category); }

    @Override
    protected int getColor() { return this.unseenNotifications() ? 0xFFFF00 : this.tab.getColor(); }

}