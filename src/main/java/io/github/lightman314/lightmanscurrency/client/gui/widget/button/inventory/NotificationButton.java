package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.LCConfigClient;
import io.github.lightman314.lightmanscurrency.client.data.ClientNotificationData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
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
        return parentCorner.withOffset(isParentCreative ? LCConfigClient.INSTANCE.buttonGroupCreative.get() : LCConfigClient.INSTANCE.buttonGroup.get()).withOffset(OFFSET);
    }

    private static Pair<Integer,Integer> getNotificationResourcePosition() { return Pair.of(ClientNotificationData.GetNotifications().unseenNotification() ? 200 + SIZE : 200, 0); }

    public static void tryRenderTooltip(MatrixStack pose, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            lastButton.parent.renderTooltip(pose, EasyText.translatable("tooltip.button.notification"), mouseX, mouseY);
    }

}