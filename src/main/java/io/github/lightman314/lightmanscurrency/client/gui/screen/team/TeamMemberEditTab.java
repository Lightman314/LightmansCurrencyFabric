package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageEditTeam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class TeamMemberEditTab extends TeamTab {

    public static final TeamMemberEditTab INSTANCE = new TeamMemberEditTab();

    private TeamMemberEditTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ItemRenderUtil.getAlexHead()); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.member_edit"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return team != null; }

    ScrollTextDisplay memberDisplay;

    TextFieldWidget memberNameInput;
    ButtonWidget buttonAddMember;
    ButtonWidget buttonPromoteMember;
    ButtonWidget buttonRemoveMember;

    @Override
    public void initTab() {
        TeamManagerScreen screen = this.getScreen();

        this.memberNameInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 11, screen.guiTop() + 9, 178, 20, Text.empty()));
        this.memberNameInput.setMaxLength(16);

        this.buttonAddMember = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 30, 60, 20, Text.translatable("gui.button.lightmanscurrency.team.member.add"), this::addMember));
        this.buttonPromoteMember = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 70, screen.guiTop() + 30, 60, 20, Text.translatable("gui.button.lightmanscurrency.team.member.promote"), this::addAdmin));
        this.buttonRemoveMember = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 130, screen.guiTop() + 30, 60, 20, Text.translatable("gui.button.lightmanscurrency.team.member.remove"), this::removeMember));
        this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = false;

        this.memberDisplay = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 10, screen.guiTop() + 55, screen.xSize - 20, screen.ySize - 65, this.getFont(), this::getMemberList));
        this.memberDisplay.setColumnCount(2);

    }

    private List<Text> getMemberList()
    {
        List<Text> list = Lists.newArrayList();
        Team team = this.getActiveTeam();
        if(team != null)
        {
            //Do NOT List Owner
            //list.add(new TextComponent(team.getOwner().lastKnownName()).withStyle(ChatFormatting.GREEN));
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
    public void tick() {

        if(this.getActiveTeam().isAdmin(this.getPlayer()))
        {
            this.buttonAddMember.active = this.buttonPromoteMember.active = this.buttonRemoveMember.active = !this.memberNameInput.getText().isBlank();
        }
        else
        {
            this.buttonAddMember.active = this.buttonPromoteMember.active = false;
            this.buttonRemoveMember.active = this.getPlayer().getGameProfile().getName().equalsIgnoreCase(this.memberNameInput.getText().toLowerCase());
        }

    }

    @Override
    public void closeTab() {

    }

    private void addMember(ButtonWidget button)
    {
        if(this.memberNameInput.getText().isBlank() || this.getActiveTeam() == null)
            return;

        Team team = this.getActiveTeam();
        team.changeAddMember(this.getPlayer(), this.memberNameInput.getText());
        new CMessageEditTeam(team.getID(), this.memberNameInput.getText(), Team.CATEGORY_MEMBER).sendToServer();
        this.memberNameInput.setText("");

    }

    private void addAdmin(ButtonWidget button)
    {
        if(this.memberNameInput.getText().isBlank() || this.getActiveTeam() == null)
            return;

        Team team = this.getActiveTeam();
        team.changeAddAdmin(this.getPlayer(), this.memberNameInput.getText());
        new CMessageEditTeam(team.getID(), this.memberNameInput.getText(), Team.CATEGORY_ADMIN).sendToServer();
        this.memberNameInput.setText("");
    }

    private void removeMember(ButtonWidget button)
    {
        if(this.memberNameInput.getText().isBlank() || this.getActiveTeam() == null)
            return;

        Team team = this.getActiveTeam();
        team.changeRemoveMember(this.getPlayer(), this.memberNameInput.getText());
        new CMessageEditTeam(team.getID(), this.memberNameInput.getText(), Team.CATEGORY_REMOVE).sendToServer();
        this.memberNameInput.setText("");
    }

}