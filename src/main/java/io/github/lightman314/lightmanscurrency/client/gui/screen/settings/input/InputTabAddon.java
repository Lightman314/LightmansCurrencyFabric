package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.input;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import net.minecraft.client.gui.DrawContext;

public abstract class InputTabAddon {

    public abstract void onInit(TraderSettingsScreen screen);

    public abstract void preRender(TraderSettingsScreen screen, DrawContext gui, int mouseX, int mouseY, float partialTicks);
    public abstract void postRender(TraderSettingsScreen screen,  DrawContext gui, int mouseX, int mouseY, float partialTicks);

    public abstract void tick(TraderSettingsScreen screen);

    public abstract void onClose(TraderSettingsScreen screen);

}