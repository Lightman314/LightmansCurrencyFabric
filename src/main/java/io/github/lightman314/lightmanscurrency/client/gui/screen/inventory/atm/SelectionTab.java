package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageATMSetPlayerAccount;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageSelectBankAccount;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SelectionTab extends ATMTab{

    public SelectionTab(ATMScreen screen) { super(screen); }

    ButtonWidget buttonPersonalAccount;
    TeamSelectWidget teamSelection;

    ButtonWidget buttonToggleAdminMode;

    TextFieldWidget playerAccountSelect;
    ButtonWidget buttonSelectPlayerAccount;
    Text responseMessage = Text.empty();

    boolean adminMode = false;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.selection"); }

    @Override
    public void init() {

        this.adminMode = false;
        this.responseMessage = Text.empty();

        SimpleSlot.SetInactive(this.screen.getScreenHandler());

        this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 79, this.screen.getGuiTop() + 15, 6, Size.NARROW, this::getTeamList, this::selectedTeam, this::SelectTeam));
        this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());

        this.buttonPersonalAccount = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, 70, 20, Text.translatable("gui.button.bank.playeraccount"), this::PressPersonalAccount));

        this.buttonToggleAdminMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getImageWidth(), this.screen.getGuiTop(), this::ToggleAdminMode, IconData.of(Items.COMMAND_BLOCK)));
        this.buttonToggleAdminMode.visible = CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().getPlayer());

        this.playerAccountSelect = this.screen.addRenderableTabWidget(new TextFieldWidget(this.screen.getFont(), this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 20, 162, 20, Text.empty()));
        this.playerAccountSelect.visible = false;

        this.buttonSelectPlayerAccount = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 45, 162, 20, Text.translatable("gui.button.bank.admin.playeraccount"), this::PressSelectPlayerAccount));
        this.buttonSelectPlayerAccount.visible = false;

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
            if(selectedTeam != null && team.getID() == selectedTeam.getID())
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

    private void ToggleAdminMode(ButtonWidget button) {
        this.adminMode = !this.adminMode;
        this.buttonPersonalAccount.visible = !this.adminMode;
        this.teamSelection.visible = !this.adminMode;

        this.buttonSelectPlayerAccount.visible = this.adminMode;
        this.playerAccountSelect.visible = this.adminMode;
    }

    private void PressSelectPlayerAccount(ButtonWidget button) {
        String playerName = this.playerAccountSelect.getText();
        this.playerAccountSelect.setText("");
        if(!playerName.isBlank())
            new CMessageATMSetPlayerAccount(playerName).sendToServer();
    }

    public void ReceiveSelectPlayerResponse(Text message) { this.responseMessage = message; }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.hideCoinSlots(gui);

        gui.drawText(this.screen.getFont(), this.getTooltip(), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);

        if(this.adminMode)
        {
            List<StringVisitable> lines = this.screen.getFont().getTextHandler().wrapLines(this.responseMessage, this.screen.getImageWidth() - 15, Style.EMPTY);
            for(int i = 0; i < lines.size(); ++i)
                gui.drawText(this.screen.getFont(), lines.get(i).getString(), this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 70 + (this.screen.getFont().fontHeight * i), 0x404040, false);
        }

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) {
        //Render text in front of selection background
        if(this.getTeamList().size() == 0 && !this.adminMode)
            TextRenderUtil.drawVerticallyCenteredMultilineText(gui, Text.translatable("gui.lightmanscurrency.bank.noteamsavailable"), this.teamSelection.getX() + 1, Size.NARROW.width - 2, this.teamSelection.getY() + 1, this.teamSelection.getHeight() - 2, 0xFFFFFF);
    }

    @Override
    public void tick() {
        this.buttonPersonalAccount.active = !this.isSelfSelected();
        this.buttonToggleAdminMode.visible = CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().getPlayer());
        if(this.adminMode)
            this.playerAccountSelect.tick();
    }

    @Override
    public void onClose() { SimpleSlot.SetActive(this.screen.getScreenHandler()); }

}