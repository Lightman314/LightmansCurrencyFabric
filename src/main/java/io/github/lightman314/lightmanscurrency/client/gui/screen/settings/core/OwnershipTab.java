package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class OwnershipTab extends SettingsTab {

    public static final OwnershipTab INSTANCE = new OwnershipTab();

    @Override
    public int getColor() {
        return 0xFFFFFF;
    }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ItemRenderUtil.getAlexHead()); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.settings.owner"); }

    private OwnershipTab() { }

    TextFieldWidget newOwnerInput;
    ButtonWidget buttonSetOwner;
    TeamSelectWidget teamSelection;
    ButtonWidget buttonSetTeamOwner;

    long selectedTeam = -1;
    List<Team> teamList = Lists.newArrayList();

    @Override
    public boolean canOpen() { return this.hasPermissions(Permissions.TRANSFER_OWNERSHIP); }

    @Override
    public void initTab() {

        TraderSettingsScreen screen = this.getScreen();

        this.newOwnerInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, Text.empty()));
        this.newOwnerInput.setMaxLength(16);

        this.buttonSetOwner = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 41, 160, 20, Text.translatable("gui.button.lightmanscurrency.set_owner"), this::setOwner));
        this.buttonSetOwner.active = false;

        this.teamSelection = screen.addRenderableTabWidget(new TeamSelectWidget(screen.guiLeft() + 10, screen.guiTop() + 65, 5, () -> this.teamList, this::getSelectedTeam, this::selectTeam));
        this.teamSelection.init(screen::addRenderableTabWidget, this.getFont());

        this.buttonSetTeamOwner = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 170, 160, 20, Text.translatable("gui.button.lightmanscurrency.set_owner"), this::setTeamOwner));
        this.buttonSetTeamOwner.active = false;

    }

    private Team getTeam(int teamIndex)
    {
        if(teamIndex < this.teamList.size())
            return this.teamList.get(teamIndex);
        return null;
    }

    private Team getSelectedTeam()
    {
        if(this.selectedTeam < 0)
            return null;
        return TeamSaveData.GetTeam(true, this.selectedTeam);
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

        TraderSettingsScreen screen = this.getScreen();

        gui.drawText(this.getFont(), Text.translatable("gui.button.lightmanscurrency.team.owner", this.getScreen().getTrader().getOwner().getOwnerName(true)), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        //Render button tooltips
        if(this.buttonSetOwner.isMouseOver(mouseX, mouseY) || this.buttonSetTeamOwner.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.getFont(), Text.translatable("tooltip.lightmanscurrency.warning").formatted(Formatting.BOLD, Formatting.YELLOW), mouseX, mouseY);
        }
    }

    @Override
    public void tick() {

        this.refreshTeamList();

        this.newOwnerInput.tick();

        this.buttonSetOwner.active = !this.newOwnerInput.getText().isBlank();
        this.buttonSetTeamOwner.active = this.getSelectedTeam() != null;

    }

    @Override
    public void closeTab() {
        //Reset the selected team & team list to save space
        this.selectedTeam = -1;
        this.teamList = Lists.newArrayList();
    }

    private void selectTeam(int teamIndex)
    {
        Team newTeam = this.getTeam(teamIndex);
        if(newTeam != null)
        {
            if(newTeam.getID() == this.selectedTeam)
                this.selectedTeam = -1;
            else
                this.selectedTeam = newTeam.getID();
        }
    }

    private void setOwner(ButtonWidget button)
    {
        if(this.newOwnerInput.getText().isBlank())
            return;
        NbtCompound message = new NbtCompound();
        message.putString("ChangePlayerOwner", this.newOwnerInput.getText());
        this.getScreen().getTrader().sendNetworkMessage(message);
        this.newOwnerInput.setText("");
    }

    private void setTeamOwner(ButtonWidget button)
    {
        if(this.getSelectedTeam() == null)
            return;
        NbtCompound message = new NbtCompound();
        message.putLong("ChangeTeamOwner", this.selectedTeam);
        this.getScreen().getTrader().sendNetworkMessage(message);
        this.selectedTeam = -1;
    }

}