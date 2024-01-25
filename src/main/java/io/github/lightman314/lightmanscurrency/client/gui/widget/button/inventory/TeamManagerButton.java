package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TeamManagerButton extends InventoryButton {

    private static TeamManagerButton lastButton = null;

    public static final int SIZE = 9;

    public static final ScreenPosition OFFSET = ScreenPosition.of(0,0);

    public TeamManagerButton(HandledScreen<?> screen) {
        super(screen, SIZE, SIZE, b -> TeamManagerScreen.open(), TeamManagerScreen.GUI_TEXTURE, 200, 0);
        lastButton = this;
    }

    @Override
    @NotNull
    protected ScreenPosition getScreenPosition(ScreenPosition parentCorner, boolean isParentCreative) {
        return parentCorner.offset(isParentCreative ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get() : LCConfig.CLIENT.notificationAndTeamButtonPosition.get()).offset(OFFSET);
    }

    public static void tryRenderTooltip(DrawContext gui, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            gui.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("tooltip.button.team_manager"), mouseX, mouseY);
    }

}