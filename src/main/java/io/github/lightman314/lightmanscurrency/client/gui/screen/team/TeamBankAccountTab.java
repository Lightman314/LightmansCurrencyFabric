package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageCreateTeamBankAccount;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageSetTeamBankLimit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TeamBankAccountTab extends TeamTab {

    public static final TeamBankAccountTab INSTANCE = new TeamBankAccountTab();

    private TeamBankAccountTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.bank"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return team != null && team.isOwner(player); }

    ButtonWidget buttonCreateBankAccount;
    ButtonWidget buttonToggleAccountLimit;

    //ScrollTextDisplay logWidget;

    @Override
    public void initTab() {

        TeamManagerScreen screen = this.getScreen();

        this.buttonCreateBankAccount = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, Text.translatable("gui.button.lightmanscurrency.team.bank.create"), this::createBankAccount));

        this.buttonToggleAccountLimit = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 60, 160, 20, Text.empty(), this::toggleBankLimit));
        this.updateBankLimitText();

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.getActiveTeam() == null)
            return;

        TeamManagerScreen screen = this.getScreen();
        if(this.getActiveTeam() != null && this.getActiveTeam().hasBankAccount())
            gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.bank.balance", this.getActiveTeam().getBankAccount().getCoinStorage().getString("0")), screen.guiLeft() + 20, screen.guiTop() + 46, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() {

        if(this.getActiveTeam() == null)
            return;

        this.buttonCreateBankAccount.active = !this.getActiveTeam().hasBankAccount();

    }

    @Override
    public void closeTab() {

    }

    private void createBankAccount(ButtonWidget button)
    {
        if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
            return;

        this.getActiveTeam().createBankAccount(this.getPlayer());
        new CMessageCreateTeamBankAccount(this.getActiveTeam().getID()).sendToServer();

    }

    private void toggleBankLimit(ButtonWidget button)
    {
        if(this.getActiveTeam() == null || !this.getActiveTeam().isOwner(this.getPlayer()))
            return;

        int newLimit = Team.NextBankLimit(this.getActiveTeam().getBankLimit());
        this.getActiveTeam().changeBankLimit(this.getPlayer(), newLimit);

        new CMessageSetTeamBankLimit(this.getActiveTeam().getID(), newLimit).sendToServer();

        this.updateBankLimitText();

    }

    private void updateBankLimitText()
    {
        Text message = Text.translatable("gui.button.lightmanscurrency.team.bank.limit", Text.translatable("gui.button.lightmanscurrency.team.bank.limit." + this.getActiveTeam().getBankLimit()));
        this.buttonToggleAccountLimit.setMessage(message);
    }

}