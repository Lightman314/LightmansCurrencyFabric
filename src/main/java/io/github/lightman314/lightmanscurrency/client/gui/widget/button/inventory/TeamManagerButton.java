package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.client.LCConfigClient;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TeamManagerButton extends InventoryButton {

    private static TeamManagerButton lastButton = null;

    public static final int SIZE = 9;

    public static final ScreenPosition OFFSET = ScreenPosition.of(0,0);

    private final HandledScreen<?> screen;

    public TeamManagerButton(HandledScreen<?> screen) {
        super(screen, SIZE, SIZE, b -> TeamManagerScreen.open(), TeamManagerScreen.GUI_TEXTURE, 200, 0);
        this.screen = screen;
        lastButton = this;
    }

    @Override
    @NotNull
    protected ScreenPosition getScreenPosition(ScreenPosition parentCorner, boolean isParentCreative) {
        return parentCorner.withOffset(isParentCreative ? LCConfigClient.INSTANCE.buttonGroupCreative.get() : LCConfigClient.INSTANCE.buttonGroup.get()).withOffset(OFFSET);
    }

    public static void tryRenderTooltip(MatrixStack pose, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            lastButton.screen.renderTooltip(pose, EasyText.translatable("tooltip.button.team_manager"), mouseX, mouseY);
    }

}