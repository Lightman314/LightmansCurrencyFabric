package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.network.server.messages.emergencyejection.CMessageOpenRecoveryMenu;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraderRecoveryButton extends InventoryButton {

    public static final Identifier GUI_TEXTURE =  new Identifier(LightmansCurrency.MODID, "textures/gui/misc.png");

    private static TraderRecoveryButton lastButton = null;

    public static final int SIZE = 9;

    public static final ScreenPosition OFFSET = ScreenPosition.of(-10, 0);

    private final HandledScreen<?> screen;
    private PlayerEntity getPlayer() {
        MinecraftClient mc = Screens.getClient(this.screen);
        if(mc != null)
            return mc.player;
        return null;
    }

    public TraderRecoveryButton(HandledScreen<?> screen) {
        super(screen, SIZE, SIZE, b -> openTraderRecoveryMenu(), GUI_TEXTURE, 0, 0);
        this.screen = screen;
        lastButton = this;
    }

    @Override
    @NotNull
    protected ScreenPosition getScreenPosition(ScreenPosition parentCorner, boolean isParentCreative) {
        return parentCorner.offset(isParentCreative ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get(): LCConfig.CLIENT.notificationAndTeamButtonPosition.get()).offset(OFFSET);
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(EjectionSaveData.GetValidEjectionData(true, this.getPlayer()).size() > 0)
        {
            this.visible = true;
            super.render(gui, mouseX, mouseY, partialTicks);
        }
        else
            this.visible = false;
    }

    public static void tryRenderTooltip(DrawContext gui, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            gui.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("tooltip.button.team_manager"), mouseX, mouseY);
    }

    private static void openTraderRecoveryMenu() { new CMessageOpenRecoveryMenu().sendToServer(); }

}