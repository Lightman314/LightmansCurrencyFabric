package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageDisbandTeam;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageEditTeam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class TeamOwnerTab extends TeamTab{

    public static final TeamOwnerTab INSTANCE = new TeamOwnerTab();

    private TeamOwnerTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.owner"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return team != null && team.isOwner(player); }

    TextFieldWidget newOwnerName;
    ButtonWidget buttonChangeOwner;

    ButtonWidget buttonDisbandTeam;

    @Override
    public void initTab() {

        if(this.getActiveTeam() == null)
        {
            this.getScreen().changeTab(0);
            return;
        }

        TeamManagerScreen screen = this.getScreen();

        this.newOwnerName = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, Text.empty()));
        this.newOwnerName.setMaxLength(16);

        this.buttonChangeOwner = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, Text.translatable("gui.button.lightmanscurrency.set_owner"), this::setNewOwner));
        this.buttonChangeOwner.active = false;

        this.buttonDisbandTeam = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 160, 160, 20, Text.translatable("gui.button.lightmanscurrency.team.disband"), this::disbandTeam));

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.getActiveTeam() == null)
            return;

        TeamManagerScreen screen = this.getScreen();

        gui.drawText(this.getFont(), Text.translatable("gui.button.lightmanscurrency.team.owner", this.getActiveTeam().getOwner().getName(true)), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.buttonChangeOwner.isMouseOver(mouseX, mouseY) || this.buttonDisbandTeam.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.getFont(), Text.translatable("tooltip.lightmanscurrency.warning").formatted(Formatting.BOLD, Formatting.YELLOW), mouseX, mouseY);
        }

    }

    @Override
    public void tick() {

        this.newOwnerName.tick();
        this.buttonChangeOwner.active = !this.newOwnerName.getText().isBlank();

    }

    @Override
    public void closeTab() {

    }

    private void setNewOwner(ButtonWidget button)
    {
        Team team = this.getActiveTeam();
        if(this.newOwnerName.getText().isBlank() || team == null)
            return;

        //team.changeOwner(this.getPlayer(), this.newOwnerName.getText());
        new CMessageEditTeam(team.getID(), this.newOwnerName.getText(), Team.CATEGORY_OWNER).sendToServer();
        this.newOwnerName.setText("");

    }

    private void disbandTeam(ButtonWidget button)
    {
        Team team = this.getActiveTeam();
        if(team == null)
            return;

        new CMessageDisbandTeam(team.getID()).sendToServer();
    }

}