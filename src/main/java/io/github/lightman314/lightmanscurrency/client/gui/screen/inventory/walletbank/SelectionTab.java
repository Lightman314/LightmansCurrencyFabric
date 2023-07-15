package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageSelectBankAccount;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SelectionTab extends WalletBankTab {

    public SelectionTab(WalletBankScreen screen) { super(screen); }

    ButtonWidget buttonPersonalAccount;
    TeamSelectWidget teamSelection;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.selection"); }

    @Override
    public void init() {

        this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 79, this.screen.getGuiTop() + 15, 5, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
        this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());

        this.buttonPersonalAccount = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, 70, 20, Text.translatable("gui.button.bank.playeraccount"), this::PressPersonalAccount));

        this.tick();

    }

    private boolean isTeamSelected() {
        return this.screen.getScreenHandler().getBankAccountReference().accountType == BankAccount.AccountType.Team;
    }

    private boolean isSelfSelected() {
        return this.screen.getScreenHandler().getBankAccount() == BankAccount.GenerateReference(this.screen.getScreenHandler().getPlayer()).get();
    }

    private List<Team> getTeamList()
    {
        List<Team> results = Lists.newArrayList();
        for(Team team : TeamSaveData.GetAllTeams(true))
        {
            if(team.hasBankAccount() && team.canAccessBankAccount(this.screen.getScreenHandler().getPlayer()))
                results.add(team);
        }
        return results;
    }

    public Team selectedTeam()
    {
        if(this.isTeamSelected())
            return TeamSaveData.GetTeam(true, this.screen.getScreenHandler().getBankAccountReference().teamID);
        return null;
    }

    public void SelectTeam(int teamIndex)
    {
        try {
            Team team = this.getTeamList().get(teamIndex);
            Team selectedTeam = this.selectedTeam();
            if(selectedTeam != null && team.getID() == this.selectedTeam().getID())
                return;
            BankAccount.AccountReference account = BankAccount.GenerateReference(true, team);
            new CMessageSelectBankAccount(account).sendToServer();
        } catch(Exception e) { }
    }

    private void PressPersonalAccount(ButtonWidget button)
    {
        BankAccount.AccountReference account = BankAccount.GenerateReference(this.screen.getScreenHandler().getPlayer());
        new CMessageSelectBankAccount(account).sendToServer();
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        gui.drawText(this.screen.getFont(), this.getTooltip(), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) {
        //Render text in front of selection background
        if(this.getTeamList().size() == 0)
            TextRenderUtil.drawVerticallyCenteredMultilineText(gui, Text.translatable("gui.lightmanscurrency.bank.noteamsavailable"), this.teamSelection.getX() + 1, Size.NARROW.width - 2, this.teamSelection.getY() + 1, this.teamSelection.getHeight() - 2, 0xFFFFFF);
    }

    @Override
    public void tick() {
        this.buttonPersonalAccount.active = !this.isSelfSelected();
    }

    @Override
    public void onClose() { }

}