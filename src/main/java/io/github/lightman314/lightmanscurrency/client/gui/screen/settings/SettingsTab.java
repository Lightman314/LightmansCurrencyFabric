package io.github.lightman314.lightmanscurrency.client.gui.screen.settings;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public abstract class SettingsTab implements ITab {

    private TraderSettingsScreen screen;
    protected final TraderSettingsScreen getScreen() { return this.screen; }
    protected final PlayerEntity getPlayer() { return this.screen.getPlayer(); }
    protected final TextRenderer getFont() { return this.screen.getFont(); }
    protected final TraderData getTrader() { return this.screen != null ? this.screen.getTrader() : null; }
    protected final void sendNetworkMessage(NbtCompound message) { this.getTrader().sendNetworkMessage(message); }
    public final void setScreen(TraderSettingsScreen screen) { this.screen = screen; }

    public abstract boolean canOpen();

    protected final boolean hasPermissions(String... permissions) {
        for(String perm : permissions) {
            if(!this.screen.hasPermission(perm))
                return false;
        }
        return true;
    }

    /**
     * Called when the tab is opened.
     * Used to initialize any widgets being used, or other relevant local variables.
     */
    public abstract void initTab();

    /**
     * Called when the tab is being rendered.
     * Used to render any text, images, etc. Called before the buttons are rendered, so you don't have to worry about accidentally drawing over them.
     */
    public abstract void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks);

    /**
     * Called when the tab is being rendered.
     * Used to render any tooltips, etc. Called after the buttons are rendered so that tooltips will appear in front.
     */
    public abstract void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks);

    /**
     * Called every frame.
     * Used to re-determine if certain widgets should still be visible, etc.
     */
    public abstract void tick();

    /**
     * Called when the tab is changed to another tab.
     * Used to remove any widgets that were added.
     */
    public abstract void closeTab();

}