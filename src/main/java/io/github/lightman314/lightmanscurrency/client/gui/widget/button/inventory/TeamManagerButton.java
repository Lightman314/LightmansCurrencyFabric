package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
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
        return parentCorner.offset(isParentCreative ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get(): LCConfig.CLIENT.notificationAndTeamButtonPosition.get()).offset(OFFSET);
    }

    public static void tryRenderTooltip(MatrixStack pose, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            lastButton.screen.renderTooltip(pose, Text.translatable("tooltip.button.team_manager"), mouseX, mouseY);
    }

}