package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class TeamMemberListTab extends TeamTab {

    public static final TeamMemberListTab INSTANCE = new TeamMemberListTab();

    private TeamMemberListTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.members"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return team != null; }

    ScrollTextDisplay memberDisplay;

    @Override
    public void initTab() {
        TeamManagerScreen screen = this.getScreen();

        this.memberDisplay = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 10, screen.guiTop() + 10, screen.xSize - 20, screen.ySize - 20, this.getFont(), this::getMemberList));
        this.memberDisplay.setColumnCount(2);

    }

    private List<Text> getMemberList()
    {
        List<Text> list = Lists.newArrayList();
        Team team = this.getActiveTeam();
        if(team != null)
        {
            //List Owner
            list.add(team.getOwner().getNameComponent(true).formatted(Formatting.GREEN));
            //List Admins
            team.getAdmins().forEach(admin -> list.add(admin.getNameComponent(true).formatted(Formatting.DARK_GREEN)));
            //List members
            team.getMembers().forEach(member -> list.add(member.getNameComponent(true)));
        }

        return list;
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() { }

    @Override
    public void closeTab() { }

}