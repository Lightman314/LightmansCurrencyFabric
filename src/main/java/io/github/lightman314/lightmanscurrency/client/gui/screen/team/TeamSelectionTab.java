package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageCreateTeam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TeamSelectionTab extends TeamTab {

    public static final TeamSelectionTab INSTANCE = new TeamSelectionTab();

    private TeamSelectionTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.selection"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return true; }

    TeamSelectWidget teamSelection;
    List<Team> teamList = Lists.newArrayList();

    TextFieldWidget newTeamName;
    ButtonWidget buttonCreateTeam;

    @Override
    public void initTab() {

        TeamManagerScreen screen = this.getScreen();

        this.refreshTeamList();

        this.teamSelection = screen.addRenderableTabWidget(new TeamSelectWidget(screen.guiLeft() + 10, screen.guiTop() + 20, 5, () -> this.teamList, this::getActiveTeam, this::selectTeamButton));
        this.teamSelection.init(screen::addRenderableTabWidget, this.getFont());

        this.newTeamName = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 140, 160, 20, Text.empty()));
        this.newTeamName.setMaxLength(32);

        this.buttonCreateTeam = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 120, screen.guiTop() + 165, 60, 20, Text.translatable("gui.button.lightmanscurrency.team.create"), this::createTeam));
        this.buttonCreateTeam.active = false;

    }

    private Team getTeam(int teamIndex)
    {
        if(teamIndex < this.teamList.size())
            return this.teamList.get(teamIndex);
        return null;
    }

    private void refreshTeamList()
    {
        this.teamList = Lists.newArrayList();
        List<Team> allTeams = TeamSaveData.GetAllTeams(true);
        allTeams.forEach(team ->{
            if(team.isMember(this.getPlayer()))
                this.teamList.add(team);
        });
        this.teamList.sort(Team.sorterFor(this.getPlayer()));
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        TeamManagerScreen screen = this.getScreen();

        //Render the text
        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.team.select"), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040, false);
        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.team.create"), screen.guiLeft() + 20, screen.guiTop() + 130, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() {

        //Refresh the team list
        this.refreshTeamList();

        this.buttonCreateTeam.active = !this.newTeamName.getText().isBlank();

    }

    @Override
    public void closeTab() { }

    private void selectTeamButton(int teamIndex)
    {
        Team team = this.getTeam(teamIndex);
        if(team != null)
        {
            if(this.getScreen().getActiveTeam() == team)
                this.getScreen().setActiveTeam(-1);
            else
                this.getScreen().setActiveTeam(team.getID());
        }
    }

    private void createTeam(ButtonWidget button)
    {
        if(this.newTeamName.getText().isEmpty())
            return;

        new CMessageCreateTeam(this.newTeamName.getText()).sendToServer();
        this.newTeamName.setText("");
    }

}