package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NotificationButton extends InventoryButton {

    private static NotificationButton lastButton = null;

    public static final int SIZE = 9;

    public static final ScreenPosition OFFSET = ScreenPosition.of(10,0);

    public NotificationButton(HandledScreen<?> screen) {
        super(screen, SIZE, SIZE, b -> NotificationScreen.open(), NotificationScreen.GUI_TEXTURE, NotificationButton::getNotificationResourcePosition);
        lastButton = this;
    }

    @Override
    @NotNull
    protected ScreenPosition getScreenPosition(ScreenPosition parentCorner, boolean isParentCreative) {
        return parentCorner.offset(isParentCreative ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get(): LCConfig.CLIENT.notificationAndTeamButtonPosition.get()).offset(OFFSET);
    }

    private static Pair<Integer,Integer> getNotificationResourcePosition() { return Pair.of(ClientNotificationData.GetNotifications().unseenNotification() ? 200 + SIZE : 200, 0); }

    public static void tryRenderTooltip(DrawContext gui, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            gui.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("tooltip.button.notification"), mouseX, mouseY);
    }

}