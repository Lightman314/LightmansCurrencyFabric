package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public abstract class TeamTab implements ITab {

    public int getColor() { return 0xFFFFFF; }
	/* Obsolete as these are covered by the ITab interface
	public abstract IconData getIcon();
	public abstract MutableComponent getTooltip();
	*/

    private TeamManagerScreen screen;
    protected final TeamManagerScreen getScreen() { return this.screen; }
    protected final PlayerEntity getPlayer() { return this.screen.getPlayer(); }
    protected final TextRenderer getFont() { return this.screen.getFont(); }
    protected final Team getActiveTeam() { return this.screen.getActiveTeam(); }
    public final void setScreen(TeamManagerScreen screen) { this.screen = screen; }

    /**
     * Returns whether a player is allowed to view this tab.
     */
    public abstract boolean allowViewing(PlayerEntity player, Team team);

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